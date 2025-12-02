# Simple comprehensive test for 3-level hierarchy
$baseUrl = "http://localhost:8080"
$passCount = 0
$failCount = 0

Write-Host "`n=== DEVCOLLAB 3-LEVEL HIERARCHY TEST SUITE ===" -ForegroundColor Yellow
Write-Host "Testing: Project > Task > Subtask`n" -ForegroundColor Yellow

# Test 1: Create Project
Write-Host "[1] Creating Project..." -ForegroundColor Cyan
try {
    $project = Invoke-RestMethod -Uri "$baseUrl/projects" -Method POST -Body '{"name":"Test Project","description":"Testing hierarchy","estimatedPomodoros":10}' -ContentType "application/json"
    Write-Host "    PASS: Project created (ID: $($project.id))" -ForegroundColor Green
    $passCount++
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
    exit 1
}

# Test 2: Create Task under Project
Write-Host "`n[2] Creating Task under Project..." -ForegroundColor Cyan
try {
    $task = Invoke-RestMethod -Uri "$baseUrl/tasks" -Method POST -Body "{`"projectId`":$($project.id),`"name`":`"Test Task`",`"description`":`"Task desc`",`"estimatedPomodoros`":5}" -ContentType "application/json"
    Write-Host "    PASS: Task created (ID: $($task.id))" -ForegroundColor Green
    $passCount++
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 3: Create Subtask under Task
Write-Host "`n[3] Creating Subtask under Task..." -ForegroundColor Cyan
try {
    $subtask = Invoke-RestMethod -Uri "$baseUrl/subtasks" -Method POST -Body "{`"taskId`":$($task.id),`"name`":`"Test Subtask`",`"estimatedPomodoros`":3}" -ContentType "application/json"
    Write-Host "    PASS: Subtask created (ID: $($subtask.id))" -ForegroundColor Green
    $passCount++
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 4: Verify hierarchy - Get Task with Project
Write-Host "`n[4] Verifying Task has Project reference..." -ForegroundColor Cyan
try {
    $taskCheck = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method GET
    if ($taskCheck.project.id -eq $project.id) {
        Write-Host "    PASS: Task correctly references Project" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Task doesn't reference correct Project" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 5: Verify hierarchy - Get Subtask with Task
Write-Host "`n[5] Verifying Subtask has Task reference..." -ForegroundColor Cyan
try {
    $subtaskCheck = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)" -Method GET
    if ($subtaskCheck.task.id -eq $task.id) {
        Write-Host "    PASS: Subtask correctly references Task" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Subtask doesn't reference correct Task" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 6: Start/Stop Pomodoro at Subtask level
Write-Host "`n[6] Testing Pomodoro timer at Subtask level..." -ForegroundColor Cyan
try {
    $pomStart = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)/pomodoro/start" -Method POST
    Write-Host "    Pomodoro started, waiting 3 seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    $pomStop = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)/pomodoro/stop" -Method POST
    if ($pomStop.totalSecondsSpent -ge 3) {
        Write-Host "    PASS: Pomodoro tracked time (${pomStop.totalSecondsSpent}s)" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Time not tracked correctly" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 7: Start/Stop Pomodoro at Task level
Write-Host "`n[7] Testing Pomodoro timer at Task level..." -ForegroundColor Cyan
try {
    $pomStart = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)/pomodoro/start" -Method POST
    Write-Host "    Pomodoro started, waiting 3 seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    $pomStop = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)/pomodoro/stop" -Method POST
    if ($pomStop.totalSecondsSpent -ge 3) {
        Write-Host "    PASS: Pomodoro tracked time (${pomStop.totalSecondsSpent}s)" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Time not tracked correctly" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 8: Start/Stop Pomodoro at Project level
Write-Host "`n[8] Testing Pomodoro timer at Project level..." -ForegroundColor Cyan
try {
    $pomStart = Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)/pomodoro/start" -Method POST
    Write-Host "    Pomodoro started, waiting 3 seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    $pomStop = Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)/pomodoro/stop" -Method POST
    if ($pomStop.totalSecondsSpent -ge 3) {
        Write-Host "    PASS: Pomodoro tracked time (${pomStop.totalSecondsSpent}s)" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Time not tracked correctly" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 9: Verify time aggregation at Task level
Write-Host "`n[9] Verifying Task time aggregation (includes Subtask time)..." -ForegroundColor Cyan
try {
    $taskWithTime = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method GET
    if ($taskWithTime.combinedSecondsSpent -gt 0) {
        Write-Host "    PASS: Task shows combined time: $($taskWithTime.combinedSecondsSpent)s" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Task combined time is 0" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 10: Verify time aggregation at Project level
Write-Host "`n[10] Verifying Project time aggregation (includes Task + Subtask time)..." -ForegroundColor Cyan
try {
    $projectWithTime = Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)" -Method GET
    if ($projectWithTime.combinedSecondsSpent -gt 0) {
        Write-Host "    PASS: Project shows combined time: $($projectWithTime.combinedSecondsSpent)s" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Project combined time is 0" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 11: Update status to DONE at all levels
Write-Host "`n[11] Setting all items to DONE status..." -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)" -Method PATCH -Body '{"completed":true}' -ContentType "application/json" | Out-Null
    Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method PATCH -Body '{"status":"DONE"}' -ContentType "application/json" | Out-Null
    Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)" -Method PATCH -Body '{"status":"DONE"}' -ContentType "application/json" | Out-Null
    Write-Host "    PASS: All items marked DONE" -ForegroundColor Green
    $passCount++
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 12: Edit DONE items (verify isEditable = true)
Write-Host "`n[12] Testing if DONE items are editable..." -ForegroundColor Cyan
try {
    $editedProj = Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)" -Method PATCH -Body '{"name":"Edited DONE Project"}' -ContentType "application/json"
    $editedTask = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method PATCH -Body '{"name":"Edited DONE Task"}' -ContentType "application/json"
    $editedSubtask = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)" -Method PATCH -Body '{"name":"Edited DONE Subtask"}' -ContentType "application/json"
    
    if ($editedProj.isEditable -and $editedTask.isEditable -and $editedSubtask.isEditable) {
        Write-Host "    PASS: All DONE items editable (isEditable=true)" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Some items not editable" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 13: Test orphan Task (should fail)
Write-Host "`n[13] Creating orphan Task (no projectId) - should fail..." -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$baseUrl/tasks" -Method POST -Body '{"name":"Orphan Task"}' -ContentType "application/json" -ErrorAction Stop
    Write-Host "    FAIL: Should have rejected orphan Task" -ForegroundColor Red
    $failCount++
} catch {
    Write-Host "    PASS: Correctly rejected orphan Task" -ForegroundColor Green
    $passCount++
}

# Test 14: Test orphan Subtask (should fail)
Write-Host "`n[14] Creating orphan Subtask (no taskId) - should fail..." -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$baseUrl/subtasks" -Method POST -Body '{"name":"Orphan Subtask","estimatedPomodoros":1}' -ContentType "application/json" -ErrorAction Stop
    Write-Host "    FAIL: Should have rejected orphan Subtask" -ForegroundColor Red
    $failCount++
} catch {
    Write-Host "    PASS: Correctly rejected orphan Subtask" -ForegroundColor Green
    $passCount++
}

# Test 15: Cascade delete - Delete Task should delete Subtasks
Write-Host "`n[15] Testing cascade delete (Task deletion removes Subtasks)..." -ForegroundColor Cyan
try {
    # Create new task and subtask for cascade test
    $cascadeTask = Invoke-RestMethod -Uri "$baseUrl/tasks" -Method POST -Body "{`"projectId`":$($project.id),`"name`":`"Cascade Task`",`"estimatedPomodoros`":3}" -ContentType "application/json"
    $cascadeSubtask = Invoke-RestMethod -Uri "$baseUrl/subtasks" -Method POST -Body "{`"taskId`":$($cascadeTask.id),`"name`":`"Cascade Subtask`",`"estimatedPomodoros`":1}" -ContentType "application/json"
    
    # Delete task
    Invoke-RestMethod -Uri "$baseUrl/tasks/$($cascadeTask.id)" -Method DELETE
    
    # Try to get subtask (should fail)
    try {
        Invoke-RestMethod -Uri "$baseUrl/subtasks/$($cascadeSubtask.id)" -Method GET -ErrorAction Stop
        Write-Host "    FAIL: Subtask still exists after Task deletion" -ForegroundColor Red
        $failCount++
    } catch {
        Write-Host "    PASS: Subtask correctly deleted with Task (cascade)" -ForegroundColor Green
        $passCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Final report
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "FINAL TEST RESULTS" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Total Tests: $($passCount + $failCount)" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
$passRate = [math]::Round(($passCount / ($passCount + $failCount)) * 100, 2)
Write-Host "Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -eq 100) { "Green" } else { "Yellow" })

if ($failCount -eq 0) {
    Write-Host "`nALL TESTS PASSED! 3-level hierarchy working correctly." -ForegroundColor Green
} else {
    Write-Host "`nSome tests failed. Review errors above." -ForegroundColor Red
}
