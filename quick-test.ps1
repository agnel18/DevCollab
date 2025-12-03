# Quick test - run AFTER restarting server
Write-Host "`n=== QUICK API TEST ===" -ForegroundColor Cyan

# Create Project
Write-Host "`n[1] Creating Project..." -ForegroundColor Yellow
$proj = Invoke-RestMethod -Uri "http://localhost:8080/api/projects" -Method POST -Body '{"name":"QuickTest","estimatedPomodoros":5}' -ContentType "application/json"
Write-Host "Project created: ID=$($proj.id), Name=$($proj.name)" -ForegroundColor Green

# Create Task
Write-Host "`n[2] Creating Task..." -ForegroundColor Yellow
$taskBody = @{ project = @{ id = $proj.id }; name = "QuickTask"; estimatedPomodoros = 3 } | ConvertTo-Json
$task = Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" -Method POST -Body $taskBody -ContentType "application/json"
Write-Host "Task created: ID=$($task.id), Name=$($task.name)" -ForegroundColor Green

# Create Subtask
Write-Host "`n[3] Creating Subtask..." -ForegroundColor Yellow
$subtaskBody = @{ task = @{ id = $task.id }; name = "QuickSubtask"; estimatedPomodoros = 2 } | ConvertTo-Json
$subtask = Invoke-RestMethod -Uri "http://localhost:8080/api/subtasks" -Method POST -Body $subtaskBody -ContentType "application/json"
Write-Host "Subtask created: ID=$($subtask.id), Name=$($subtask.name)" -ForegroundColor Green

Write-Host "`n=== SUCCESS! 3-level hierarchy works ===" -ForegroundColor Green
Write-Host "Project ($($proj.id)) > Task ($($task.id)) > Subtask ($($subtask.id))" -ForegroundColor Cyan
