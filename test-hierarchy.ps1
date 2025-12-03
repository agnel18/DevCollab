# Comprehensive test for 3-level hierarchy (Project > Task > Subtask)
$baseUrl = "http://localhost:8080/api"
$passCount = 0
$failCount = 0

Write-Host "`n=== DEVCOLLAB 3-LEVEL HIERARCHY TEST SUITE ===" -ForegroundColor Yellow
Write-Host "Testing: Project > Task > Subtask`n" -ForegroundColor Yellow

# Test 1: Create Project
Write-Host "[1] Creating Project..." -ForegroundColor Cyan
try {
    $projectBody = @{
        name = "Test Project"
        description = "Testing hierarchy"
        estimatedPomodoros = 10
    } | ConvertTo-Json
    
    $project = Invoke-RestMethod -Uri "$baseUrl/projects" -Method POST -Body $projectBody -ContentType "application/json"
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
    $taskBody = @{
        project = @{ id = $project.id }
        name = "Test Task"
        description = "Task description"
        estimatedPomodoros = 5
    } | ConvertTo-Json
    
    $task = Invoke-RestMethod -Uri "$baseUrl/tasks" -Method POST -Body $taskBody -ContentType "application/json"
    Write-Host "    PASS: Task created (ID: $($task.id))" -ForegroundColor Green
    $passCount++
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 3: Create Subtask under Task
Write-Host "`n[3] Creating Subtask under Task..." -ForegroundColor Cyan
try {
    $subtaskBody = @{
        task = @{ id = $task.id }
        name = "Test Subtask"
        estimatedPomodoros = 3
    } | ConvertTo-Json
    
    $subtask = Invoke-RestMethod -Uri "$baseUrl/subtasks" -Method POST -Body $subtaskBody -ContentType "application/json"
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
        Write-Host "    PASS: Subtask Pomodoro tracked time ($($pomStop.totalSecondsSpent)s)" -ForegroundColor Green
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
        Write-Host "    PASS: Task Pomodoro tracked time ($($pomStop.totalSecondsSpent)s)" -ForegroundColor Green
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
        Write-Host "    PASS: Project Pomodoro tracked time ($($pomStop.totalSecondsSpent)s)" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Time not tracked correctly" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 9: Verify Task time aggregation (includes Subtask time)
Write-Host "`n[9] Verifying Task time aggregation (includes Subtask time)..." -ForegroundColor Cyan
try {
    $taskWithTime = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method GET
    $expectedMin = 6 # Task: 3s + Subtask: 3s
    if ($taskWithTime.combinedSecondsSpent -ge $expectedMin) {
        Write-Host "    PASS: Task aggregates time ($($taskWithTime.combinedSecondsSpent)s >= ${expectedMin}s)" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Task time aggregation incorrect ($($taskWithTime.combinedSecondsSpent)s)" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 10: Verify Project time aggregation (includes Task + Subtask time)
Write-Host "`n[10] Verifying Project time aggregation (includes Task + Subtask time)..." -ForegroundColor Cyan
try {
    $projectWithTime = Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)" -Method GET
    $expectedMin = 9 # Project: 3s + Task: 3s + Subtask: 3s
    if ($projectWithTime.combinedSecondsSpent -ge $expectedMin) {
        Write-Host "    PASS: Project aggregates all time ($($projectWithTime.combinedSecondsSpent)s >= ${expectedMin}s)" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Project time aggregation incorrect ($($projectWithTime.combinedSecondsSpent)s)" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 11: Set all items to DONE status
Write-Host "`n[11] Setting all items to DONE status..." -ForegroundColor Cyan
try {
    $subtaskDone = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)" -Method PATCH -Body '{"completed":true}' -ContentType "application/json"
    $taskDone = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method PATCH -Body '{"status":"DONE"}' -ContentType "application/json"
    $projectDone = Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)" -Method PATCH -Body '{"status":"DONE"}' -ContentType "application/json"
    
    if ($subtaskDone.completed -and $taskDone.status -eq "DONE" -and $projectDone.status -eq "DONE") {
        Write-Host "    PASS: All items marked as DONE" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: Items not properly marked as DONE" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 12: Test if DONE items are editable
Write-Host "`n[12] Testing if DONE items are editable..." -ForegroundColor Cyan
try {
    $subtaskEdit = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)" -Method PATCH -Body '{"name":"Edited Subtask"}' -ContentType "application/json"
    $taskEdit = Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method PATCH -Body '{"name":"Edited Task"}' -ContentType "application/json"
    $projectEdit = Invoke-RestMethod -Uri "$baseUrl/projects/$($project.id)" -Method PATCH -Body '{"name":"Edited Project"}' -ContentType "application/json"
    
    if ($subtaskEdit.name -eq "Edited Subtask" -and $taskEdit.name -eq "Edited Task" -and $projectEdit.name -eq "Edited Project") {
        Write-Host "    PASS: DONE items are editable" -ForegroundColor Green
        $passCount++
    } else {
        Write-Host "    FAIL: DONE items not editable" -ForegroundColor Red
        $failCount++
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Test 13: Create orphan Task (no projectId) - should fail
Write-Host "`n[13] Creating orphan Task (no projectId) - should fail..." -ForegroundColor Cyan
try {
    $orphanTask = Invoke-RestMethod -Uri "$baseUrl/tasks" -Method POST -Body '{"name":"Orphan Task"}' -ContentType "application/json"
    Write-Host "    FAIL: Orphan Task was created (should have been rejected)" -ForegroundColor Red
    $failCount++
} catch {
    Write-Host "    PASS: Correctly rejected orphan Task" -ForegroundColor Green
    $passCount++
}

# Test 14: Create orphan Subtask (no taskId) - should fail
Write-Host "`n[14] Creating orphan Subtask (no taskId) - should fail..." -ForegroundColor Cyan
try {
    $orphanSubtask = Invoke-RestMethod -Uri "$baseUrl/subtasks" -Method POST -Body '{"name":"Orphan Subtask"}' -ContentType "application/json"
    Write-Host "    FAIL: Orphan Subtask was created (should have been rejected)" -ForegroundColor Red
    $failCount++
} catch {
    Write-Host "    PASS: Correctly rejected orphan Subtask" -ForegroundColor Green
    $passCount++
}

# Test 15: Test cascade delete (Task deletion removes Subtasks)
Write-Host "`n[15] Testing cascade delete (Task deletion removes Subtasks)..." -ForegroundColor Cyan
try {
    # First verify subtask exists
    $subtaskExists = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)" -Method GET
    
    # Delete the task (should cascade to subtask)
    Invoke-RestMethod -Uri "$baseUrl/tasks/$($task.id)" -Method DELETE
    
    # Try to get the subtask - should fail with 404
    try {
        $subtaskCheck = Invoke-RestMethod -Uri "$baseUrl/subtasks/$($subtask.id)" -Method GET
        Write-Host "    FAIL: Subtask still exists after Task deletion" -ForegroundColor Red
        $failCount++
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host "    PASS: Cascade delete worked (Subtask removed with Task)" -ForegroundColor Green
            $passCount++
        } else {
            Write-Host "    FAIL: Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
            $failCount++
        }
    }
} catch {
    Write-Host "    FAIL: $($_.Exception.Message)" -ForegroundColor Red
    $failCount++
}

# Final summary
Write-Host "`n========================================"  -ForegroundColor Yellow
Write-Host "FINAL TEST RESULTS" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "Total Tests: $($passCount + $failCount)" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
$passRate = [math]::Round(($passCount / ($passCount + $failCount)) * 100, 0)
Write-Host "Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -ge 80) { "Green" } elseif ($passRate -ge 60) { "Yellow" } else { "Red" })
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "All tests passed!" -ForegroundColor Green
} else {
    Write-Host "Some tests failed. Review errors above." -ForegroundColor Red
}
