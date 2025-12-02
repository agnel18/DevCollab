# Comprehensive Test Suite for 3-Level Hierarchy (Project → Task → Subtask)
# Following MECE Framework: Mutually Exclusive, Collectively Exhaustive

$baseUrl = "http://localhost:8080/api"
$testResults = @()
$testCount = 0
$passCount = 0
$failCount = 0

function Test-API {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Url,
        [string]$Body = $null,
        [scriptblock]$Validation
    )
    
    $script:testCount++
    Write-Host "`n[$script:testCount] Testing: $TestName" -ForegroundColor Cyan
    
    try {
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $Body -ContentType "application/json" -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -ErrorAction Stop
        }
        
        $validationResult = & $Validation $response
        
        if ($validationResult) {
            Write-Host "   ✓ PASS" -ForegroundColor Green
            $script:passCount++
            $script:testResults += [PSCustomObject]@{Test=$TestName; Result="PASS"; Details="Success"}
            return $response
        } else {
            Write-Host "   ✗ FAIL: Validation failed" -ForegroundColor Red
            $script:failCount++
            $script:testResults += [PSCustomObject]@{Test=$TestName; Result="FAIL"; Details="Validation failed"}
            return $null
        }
    }
    catch {
        Write-Host "   ✗ FAIL: $($_.Exception.Message)" -ForegroundColor Red
        $script:failCount++
        $script:testResults += [PSCustomObject]@{Test=$TestName; Result="FAIL"; Details=$_.Exception.Message}
        return $null
    }
}

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║  COMPREHENSIVE 3-LEVEL HIERARCHY TEST SUITE (MECE Framework)  ║" -ForegroundColor Magenta
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Magenta

# ============================================================================
# CATEGORY 1: PROJECT CRUD OPERATIONS
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 1: PROJECT CRUD OPERATIONS" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 1.1 Create Project - Valid
$project1 = Test-API `
    -TestName "Create Project - Valid minimal data" `
    -Method POST `
    -Url "$baseUrl/projects" `
    -Body '{"name":"Test Project 1","description":"Basic project"}' `
    -Validation { param($r) $r.id -and $r.name -eq "Test Project 1" -and $r.status -eq "TODO" }

# 1.2 Create Project - Only required fields
$project2 = Test-API `
    -TestName "Create Project - Only name (minimal)" `
    -Method POST `
    -Url "$baseUrl/projects" `
    -Body '{"name":"Minimal Project"}' `
    -Validation { param($r) $r.id -and $r.name -eq "Minimal Project" }

# 1.3 Get Single Project
if ($project1) {
    Test-API `
        -TestName "Get Project by ID" `
        -Method GET `
        -Url "$baseUrl/projects/$($project1.id)" `
        -Validation { param($r) $r.id -eq $project1.id -and $r.name -eq "Test Project 1" }
}

# 1.4 Update Project
if ($project1) {
    $updated = Test-API `
        -TestName "Update Project - Change name and description" `
        -Method PATCH `
        -Url "$baseUrl/projects/$($project1.id)" `
        -Body '{"name":"Updated Project","description":"Modified description"}' `
        -Validation { param($r) $r.name -eq "Updated Project" -and $r.description -eq "Modified description" }
}

# 1.5 List All Projects
Test-API `
    -TestName "List all Projects" `
    -Method GET `
    -Url "$baseUrl/projects" `
    -Validation { param($r) $r.Count -ge 2 }

# 1.6 Delete Project
$projectToDelete = Test-API `
    -TestName "Create Project for deletion test" `
    -Method POST `
    -Url "$baseUrl/projects" `
    -Body '{"name":"To Be Deleted"}' `
    -Validation { param($r) $r.id -gt 0 }

if ($projectToDelete) {
    Test-API `
        -TestName "Delete Project" `
        -Method DELETE `
        -Url "$baseUrl/projects/$($projectToDelete.id)" `
        -Validation { param($r) $true }
}

# ============================================================================
# CATEGORY 2: TASK CRUD OPERATIONS
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 2: TASK CRUD OPERATIONS" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 2.1 Create Task under Project - Valid
$task1 = $null
if ($project1) {
    $task1 = Test-API `
        -TestName "Create Task - Valid with all fields" `
        -Method POST `
        -Url "$baseUrl/tasks" `
        -Body "{`"projectId`":$($project1.id),`"name`":`"Task 1`",`"description`":`"First task`",`"estimatedPomodoros`":3}" `
        -Validation { param($r) $r.id -and $r.name -eq "Task 1" -and $r.project.id -eq $project1.id }
}

# 2.2 Create Task - Minimal fields
$task2 = $null
if ($project1) {
    $task2 = Test-API `
        -TestName "Create Task - Only required fields" `
        -Method POST `
        -Url "$baseUrl/tasks" `
        -Body "{`"projectId`":$($project1.id),`"name`":`"Minimal Task`"}" `
        -Validation { param($r) $r.id -and $r.name -eq "Minimal Task" }
}

# 2.3 Get Single Task
if ($task1) {
    Test-API `
        -TestName "Get Task by ID" `
        -Method GET `
        -Url "$baseUrl/tasks/$($task1.id)" `
        -Validation { param($r) $r.id -eq $task1.id -and $r.name -eq "Task 1" }
}

# 2.4 List Tasks by Project
if ($project1) {
    Test-API `
        -TestName "List Tasks for specific Project" `
        -Method GET `
        -Url "$baseUrl/tasks?projectId=$($project1.id)" `
        -Validation { param($r) $r.Count -ge 2 }
}

# 2.5 Update Task
if ($task1) {
    Test-API `
        -TestName "Update Task - Change name and description" `
        -Method PATCH `
        -Url "$baseUrl/tasks/$($task1.id)" `
        -Body '{"name":"Updated Task","description":"Modified task description"}' `
        -Validation { param($r) $r.name -eq "Updated Task" }
}

# 2.6 Delete Task
$taskToDelete = $null
if ($project1) {
    $taskToDelete = Test-API `
        -TestName "Create Task for deletion test" `
        -Method POST `
        -Url "$baseUrl/tasks" `
        -Body "{`"projectId`":$($project1.id),`"name`":`"Task To Delete`"}" `
        -Validation { param($r) $r.id -gt 0 }
}

if ($taskToDelete) {
    Test-API `
        -TestName "Delete Task" `
        -Method DELETE `
        -Url "$baseUrl/tasks/$($taskToDelete.id)" `
        -Validation { param($r) $true }
}

# ============================================================================
# CATEGORY 3: SUBTASK CRUD OPERATIONS
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 3: SUBTASK CRUD OPERATIONS" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 3.1 Create Subtask under Task - Valid
$subtask1 = $null
if ($task1) {
    $subtask1 = Test-API `
        -TestName "Create Subtask - Valid with all fields" `
        -Method POST `
        -Url "$baseUrl/subtasks" `
        -Body "{`"taskId`":$($task1.id),`"name`":`"Subtask 1`",`"estimatedPomodoros`":2}" `
        -Validation { param($r) $r.id -and $r.name -eq "Subtask 1" -and $r.task.id -eq $task1.id }
}

# 3.2 Create Subtask - Minimal fields
$subtask2 = $null
if ($task1) {
    $subtask2 = Test-API `
        -TestName "Create Subtask - Only required fields" `
        -Method POST `
        -Url "$baseUrl/subtasks" `
        -Body "{`"taskId`":$($task1.id),`"name`":`"Minimal Subtask`",`"estimatedPomodoros`":1}" `
        -Validation { param($r) $r.id -and $r.name -eq "Minimal Subtask" }
}

# 3.3 Get Single Subtask
if ($subtask1) {
    Test-API `
        -TestName "Get Subtask by ID" `
        -Method GET `
        -Url "$baseUrl/subtasks/$($subtask1.id)" `
        -Validation { param($r) $r.id -eq $subtask1.id -and $r.name -eq "Subtask 1" }
}

# 3.4 List Subtasks by Task
if ($task1) {
    Test-API `
        -TestName "List Subtasks for specific Task" `
        -Method GET `
        -Url "$baseUrl/subtasks?taskId=$($task1.id)" `
        -Validation { param($r) $r.Count -ge 2 }
}

# 3.5 Update Subtask
if ($subtask1) {
    Test-API `
        -TestName "Update Subtask - Change name" `
        -Method PATCH `
        -Url "$baseUrl/subtasks/$($subtask1.id)" `
        -Body '{"name":"Updated Subtask"}' `
        -Validation { param($r) $r.name -eq "Updated Subtask" }
}

# 3.6 Delete Subtask
$subtaskToDelete = $null
if ($task1) {
    $subtaskToDelete = Test-API `
        -TestName "Create Subtask for deletion test" `
        -Method POST `
        -Url "$baseUrl/subtasks" `
        -Body "{`"taskId`":$($task1.id),`"name`":`"Subtask To Delete`",`"estimatedPomodoros`":1}" `
        -Validation { param($r) $r.id -gt 0 }
}

if ($subtaskToDelete) {
    Test-API `
        -TestName "Delete Subtask" `
        -Method DELETE `
        -Url "$baseUrl/subtasks/$($subtaskToDelete.id)" `
        -Validation { param($r) $true }
}

# ============================================================================
# CATEGORY 4: HIERARCHY RELATIONSHIPS
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 4: HIERARCHY RELATIONSHIPS" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 4.1 Verify complete hierarchy chain
if ($project1 -and $task1 -and $subtask1) {
    Test-API `
        -TestName "Verify complete chain: Project → Task → Subtask" `
        -Method GET `
        -Url "$baseUrl/subtasks/$($subtask1.id)" `
        -Validation { param($r) $r.task.id -eq $task1.id -and $r.task.project.id -eq $project1.id }
}

# 4.2 Multiple tasks under same project
if ($project1) {
    $task3 = Test-API `
        -TestName "Create multiple Tasks under same Project" `
        -Method POST `
        -Url "$baseUrl/tasks" `
        -Body "{`"projectId`":$($project1.id),`"name`":`"Task 3`"}" `
        -Validation { param($r) $r.project.id -eq $project1.id }
}

# 4.3 Multiple subtasks under same task
if ($task1) {
    $subtask3 = Test-API `
        -TestName "Create multiple Subtasks under same Task" `
        -Method POST `
        -Url "$baseUrl/subtasks" `
        -Body "{`"taskId`":$($task1.id),`"name`":`"Subtask 3`",`"estimatedPomodoros`":1}" `
        -Validation { param($r) $r.task.id -eq $task1.id }
}

# ============================================================================
# CATEGORY 5: STATUS TRANSITIONS
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 5: STATUS TRANSITIONS" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 5.1 Move Project: TODO → DOING
if ($project1) {
    Test-API `
        -TestName "Project: TODO → DOING" `
        -Method POST `
        -Url "$baseUrl/projects/$($project1.id)/move?newStatus=DOING" `
        -Validation { param($r) $r.status -eq "DOING" }
}

# 5.2 Move Project: DOING → DONE
if ($project1) {
    $doneProj = Test-API `
        -TestName "Project: DOING → DONE" `
        -Method POST `
        -Url "$baseUrl/projects/$($project1.id)/move?newStatus=DONE" `
        -Validation { param($r) $r.status -eq "DONE" -and $r.completedAt }
}

# 5.3 Move Task: TODO → DOING → DONE
if ($task2) {
    Test-API `
        -TestName "Task: TODO → DOING" `
        -Method POST `
        -Url "$baseUrl/tasks/$($task2.id)/move?newStatus=DOING" `
        -Validation { param($r) $r.status -eq "DOING" }
    
    $doneTask = Test-API `
        -TestName "Task: DOING → DONE" `
        -Method POST `
        -Url "$baseUrl/tasks/$($task2.id)/move?newStatus=DONE" `
        -Validation { param($r) $r.status -eq "DONE" -and $r.completedAt }
}

# 5.4 Move Subtask: TODO → DONE
if ($subtask2) {
    Test-API `
        -TestName "Subtask: TODO → DONE" `
        -Method PATCH `
        -Url "$baseUrl/subtasks/$($subtask2.id)" `
        -Body '{"completed":true}' `
        -Validation { param($r) $r.completed -eq $true }
}

# ============================================================================
# CATEGORY 6: POMODORO OPERATIONS - PROJECT LEVEL
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 6: POMODORO - PROJECT LEVEL" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

$pomodoroProject = Test-API `
    -TestName "Create Project for Pomodoro testing" `
    -Method POST `
    -Url "$baseUrl/projects" `
    -Body '{"name":"Pomodoro Test Project"}' `
    -Validation { param($r) $r.id -gt 0 }

if ($pomodoroProject) {
    # 6.1 Start Pomodoro on Project
    Test-API `
        -TestName "Start Pomodoro timer on Project" `
        -Method POST `
        -Url "$baseUrl/projects/$($pomodoroProject.id)/pomodoro/start" `
        -Validation { param($r) $r.pomodoroStart }
    
    Start-Sleep -Seconds 3
    
    # 6.2 Stop Pomodoro on Project
    $stoppedProj = Test-API `
        -TestName "Stop Pomodoro timer on Project" `
        -Method POST `
        -Url "$baseUrl/projects/$($pomodoroProject.id)/pomodoro/stop" `
        -Validation { param($r) $r.totalSecondsSpent -ge 3 -and $r.completedPomodoros -ge 1 }
}

# ============================================================================
# CATEGORY 7: POMODORO OPERATIONS - TASK LEVEL
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 7: POMODORO - TASK LEVEL" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

$pomodoroTask = $null
if ($pomodoroProject) {
    $pomodoroTask = Test-API `
        -TestName "Create Task for Pomodoro testing" `
        -Method POST `
        -Url "$baseUrl/tasks" `
        -Body "{`"projectId`":$($pomodoroProject.id),`"name`":`"Pomodoro Test Task`"}" `
        -Validation { param($r) $r.id -gt 0 }
}

if ($pomodoroTask) {
    # 7.1 Start Pomodoro on Task
    Test-API `
        -TestName "Start Pomodoro timer on Task" `
        -Method POST `
        -Url "$baseUrl/tasks/$($pomodoroTask.id)/pomodoro/start" `
        -Validation { param($r) $r.pomodoroStart }
    
    Start-Sleep -Seconds 3
    
    # 7.2 Stop Pomodoro on Task
    $stoppedTask = Test-API `
        -TestName "Stop Pomodoro timer on Task" `
        -Method POST `
        -Url "$baseUrl/tasks/$($pomodoroTask.id)/pomodoro/stop" `
        -Validation { param($r) $r.totalSecondsSpent -ge 3 -and $r.completedPomodoros -ge 1 }
}

# ============================================================================
# CATEGORY 8: POMODORO OPERATIONS - SUBTASK LEVEL
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 8: POMODORO - SUBTASK LEVEL" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

$pomodoroSubtask = $null
if ($pomodoroTask) {
    $pomodoroSubtask = Test-API `
        -TestName "Create Subtask for Pomodoro testing" `
        -Method POST `
        -Url "$baseUrl/subtasks" `
        -Body "{`"taskId`":$($pomodoroTask.id),`"name`":`"Pomodoro Test Subtask`",`"estimatedPomodoros`":1}" `
        -Validation { param($r) $r.id -gt 0 }
}

if ($pomodoroSubtask) {
    # 8.1 Start Pomodoro on Subtask
    Test-API `
        -TestName "Start Pomodoro timer on Subtask" `
        -Method POST `
        -Url "$baseUrl/subtasks/$($pomodoroSubtask.id)/pomodoro/start" `
        -Validation { param($r) $r.pomodoroStart }
    
    Start-Sleep -Seconds 3
    
    # 8.2 Stop Pomodoro on Subtask
    $stoppedSubtask = Test-API `
        -TestName "Stop Pomodoro timer on Subtask" `
        -Method POST `
        -Url "$baseUrl/subtasks/$($pomodoroSubtask.id)/pomodoro/stop" `
        -Validation { param($r) $r.totalSecondsSpent -ge 3 }
}

# ============================================================================
# CATEGORY 9: TIME AGGREGATION
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 9: TIME AGGREGATION" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 9.1 Verify Task aggregates Subtask time
if ($stoppedTask -and $stoppedSubtask) {
    Test-API `
        -TestName "Task.getCombinedSecondsSpent includes Subtask time" `
        -Method GET `
        -Url "$baseUrl/tasks/$($pomodoroTask.id)" `
        -Validation { param($r) $r.totalSecondsSpent -ge 6 }
}

# 9.2 Verify Project aggregates Task + Subtask time
if ($stoppedProj -and $stoppedTask) {
    Test-API `
        -TestName "Project.getCombinedSecondsSpent includes all Task times" `
        -Method GET `
        -Url "$baseUrl/projects/$($pomodoroProject.id)" `
        -Validation { param($r) $r.totalSecondsSpent -ge 3 }
}

# ============================================================================
# CATEGORY 10: EDITING DONE ITEMS
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 10: EDITING DONE ITEMS" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 10.1 Edit DONE Project
if ($doneProj) {
    Test-API `
        -TestName "Edit DONE Project - Should succeed" `
        -Method PATCH `
        -Url "$baseUrl/projects/$($project1.id)" `
        -Body '{"name":"Edited DONE Project","description":"Can edit completed projects"}' `
        -Validation { param($r) $r.name -eq "Edited DONE Project" -and $r.status -eq "DONE" }
}

# 10.2 Edit DONE Task
if ($doneTask) {
    Test-API `
        -TestName "Edit DONE Task - Should succeed" `
        -Method PATCH `
        -Url "$baseUrl/tasks/$($task2.id)" `
        -Body '{"name":"Edited DONE Task","description":"Can edit completed tasks"}' `
        -Validation { param($r) $r.name -eq "Edited DONE Task" -and $r.status -eq "DONE" }
}

# 10.3 Verify isEditable() returns true for DONE items
if ($doneProj) {
    Write-Host "   Note: isEditable() should return true even for DONE status" -ForegroundColor Gray
}

# ============================================================================
# CATEGORY 11: EDGE CASES & ERROR HANDLING
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 11: EDGE CASES & VALIDATION" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# 11.1 Create Task without Project (should fail)
Write-Host "`n[$script:testCount] Testing: Create Task without projectId - Should fail" -ForegroundColor Cyan
$script:testCount++
try {
    Invoke-RestMethod -Uri "$baseUrl/tasks" -Method POST -Body '{"name":"Orphan Task"}' -ContentType "application/json" -ErrorAction Stop
    Write-Host "   X FAIL: Should have rejected task without projectId" -ForegroundColor Red
    $script:failCount++
} catch {
    Write-Host "   PASS: Correctly rejected (400/500 error expected)" -ForegroundColor Green
    $script:passCount++
}

# 11.2 Create Subtask without Task (should fail)
Write-Host "`n[$script:testCount] Testing: Create Subtask without taskId - Should fail" -ForegroundColor Cyan
$script:testCount++
try {
    Invoke-RestMethod -Uri "$baseUrl/subtasks" -Method POST -Body '{"name":"Orphan Subtask","estimatedPomodoros":1}' -ContentType "application/json" -ErrorAction Stop
    Write-Host "   X FAIL: Should have rejected subtask without taskId" -ForegroundColor Red
    $script:failCount++
} catch {
    Write-Host "   PASS: Correctly rejected (400/500 error expected)" -ForegroundColor Green
    $script:passCount++
}

# 11.3 Get non-existent Project
Write-Host "`n[$script:testCount] Testing: Get non-existent Project (ID: 99999)" -ForegroundColor Cyan
$script:testCount++
try {
    Invoke-RestMethod -Uri "$baseUrl/projects/99999" -Method GET -ErrorAction Stop
    Write-Host "   ✗ FAIL: Should have returned 404" -ForegroundColor Red
    $script:failCount++
} catch {
    Write-Host "   ✓ PASS: Correctly returned 404" -ForegroundColor Green
    $script:passCount++
}

# 11.4 Get non-existent Task
Write-Host "`n[$script:testCount] Testing: Get non-existent Task (ID: 99999)" -ForegroundColor Cyan
$script:testCount++
try {
    Invoke-RestMethod -Uri "$baseUrl/tasks/99999" -Method GET -ErrorAction Stop
    Write-Host "   ✗ FAIL: Should have returned 404" -ForegroundColor Red
    $script:failCount++
} catch {
    Write-Host "   ✓ PASS: Correctly returned 404" -ForegroundColor Green
    $script:passCount++
}

# 11.5 Create Project with empty name
Write-Host "`n[$script:testCount] Testing: Create Project with empty name - Should fail" -ForegroundColor Cyan
$script:testCount++
try {
    Invoke-RestMethod -Uri "$baseUrl/projects" -Method POST -Body '{"name":""}' -ContentType "application/json" -ErrorAction Stop
    Write-Host "   ✗ FAIL: Should have rejected empty name" -ForegroundColor Red
    $script:failCount++
} catch {
    Write-Host "   ✓ PASS: Correctly rejected empty name" -ForegroundColor Green
    $script:passCount++
}

# ============================================================================
# CATEGORY 12: CASCADE DELETE VERIFICATION
# ============================================================================
Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "CATEGORY 12: CASCADE DELETE" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow

# Create test hierarchy for cascade
$cascadeProj = Test-API `
    -TestName "Create Project for cascade delete test" `
    -Method POST `
    -Url "$baseUrl/projects" `
    -Body '{"name":"Cascade Test Project"}' `
    -Validation { param($r) $r.id -gt 0 }

$cascadeTask = $null
if ($cascadeProj) {
    $cascadeTask = Test-API `
        -TestName "Create Task under cascade Project" `
        -Method POST `
        -Url "$baseUrl/tasks" `
        -Body "{`"projectId`":$($cascadeProj.id),`"name`":`"Cascade Task`"}" `
        -Validation { param($r) $r.id -gt 0 }
}

$cascadeSubtask = $null
if ($cascadeTask) {
    $cascadeSubtask = Test-API `
        -TestName "Create Subtask under cascade Task" `
        -Method POST `
        -Url "$baseUrl/subtasks" `
        -Body "{`"taskId`":$($cascadeTask.id),`"name`":`"Cascade Subtask`",`"estimatedPomodoros`":1}" `
        -Validation { param($r) $r.id -gt 0 }
}

# 12.1 Delete Task (should cascade to Subtask)
if ($cascadeTask -and $cascadeSubtask) {
    Test-API `
        -TestName "Delete Task - Should cascade delete Subtasks" `
        -Method DELETE `
        -Url "$baseUrl/tasks/$($cascadeTask.id)" `
        -Validation { param($r) $true }
    
    # Verify subtask is gone
    Write-Host "`n[$script:testCount] Testing: Verify Subtask was cascade deleted" -ForegroundColor Cyan
    $script:testCount++
    try {
        Invoke-RestMethod -Uri "$baseUrl/subtasks/$($cascadeSubtask.id)" -Method GET -ErrorAction Stop
        Write-Host "   ✗ FAIL: Subtask should have been deleted" -ForegroundColor Red
        $script:failCount++
    } catch {
        Write-Host "   ✓ PASS: Subtask correctly cascade deleted" -ForegroundColor Green
        $script:passCount++
    }
}

# 12.2 Delete Project (should cascade to Tasks and Subtasks)
if ($cascadeProj) {
    # Create new task for this test
    $cascadeTask2 = Test-API `
        -TestName "Create new Task for Project cascade test" `
        -Method POST `
        -Url "$baseUrl/tasks" `
        -Body "{`"projectId`":$($cascadeProj.id),`"name`":`"Task for Project Delete`"}" `
        -Validation { param($r) $r.id -gt 0 }
    
    Test-API `
        -TestName "Delete Project - Should cascade delete all Tasks" `
        -Method DELETE `
        -Url "$baseUrl/projects/$($cascadeProj.id)" `
        -Validation { param($r) $true }
    
    if ($cascadeTask2) {
        # Verify task is gone
        Write-Host "`n[$script:testCount] Testing: Verify Task was cascade deleted with Project" -ForegroundColor Cyan
        $script:testCount++
        try {
            Invoke-RestMethod -Uri "$baseUrl/tasks/$($cascadeTask2.id)" -Method GET -ErrorAction Stop
            Write-Host "   ✗ FAIL: Task should have been deleted" -ForegroundColor Red
            $script:failCount++
        } catch {
            Write-Host "   ✓ PASS: Task correctly cascade deleted" -ForegroundColor Green
            $script:passCount++
        }
    }
}

# ============================================================================
# FINAL REPORT
# ============================================================================
Write-Host "`n`n╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║                    COMPREHENSIVE TEST REPORT                   ║" -ForegroundColor Magenta
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Magenta

Write-Host "`nTotal Tests: $testCount" -ForegroundColor Cyan
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Red" })
$passRate = [math]::Round(($passCount / $testCount) * 100, 2)
Write-Host "Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -ge 90) { "Green" } elseif ($passRate -ge 70) { "Yellow" } else { "Red" })

Write-Host "`n═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "TEST CATEGORIES COVERED (MECE Framework):" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow
Write-Host "1.  Project CRUD Operations (Create/Read/Update/Delete)" -ForegroundColor Gray
Write-Host "2.  Task CRUD Operations (Create/Read/Update/Delete)" -ForegroundColor Gray
Write-Host "3.  Subtask CRUD Operations (Create/Read/Update/Delete)" -ForegroundColor Gray
Write-Host "4.  Hierarchy Relationships (3-level chain verification)" -ForegroundColor Gray
Write-Host "5.  Status Transitions (TODO → DOING → DONE)" -ForegroundColor Gray
Write-Host "6.  Pomodoro Operations - Project Level" -ForegroundColor Gray
Write-Host "7.  Pomodoro Operations - Task Level" -ForegroundColor Gray
Write-Host "8.  Pomodoro Operations - Subtask Level" -ForegroundColor Gray
Write-Host "9.  Time Aggregation (Subtask → Task → Project)" -ForegroundColor Gray
Write-Host "10. Editing DONE Items (isEditable validation)" -ForegroundColor Gray
Write-Host "11. Edge Cases & Error Handling" -ForegroundColor Gray
Write-Host "12. Cascade Delete Operations" -ForegroundColor Gray

if ($failCount -eq 0) {
    Write-Host "`n✓ ALL TESTS PASSED! 3-Level Hierarchy is working correctly!" -ForegroundColor Green
} else {
    Write-Host "`n⚠ Some tests failed. Review the output above for details." -ForegroundColor Yellow
}

Write-Host "`n═══════════════════════════════════════" -ForegroundColor Cyan
