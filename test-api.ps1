# Test 3-Level Hierarchy API
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Testing 3-Level Hierarchy (Project → Task → Subtask)" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Create Project
Write-Host "1. Creating Project..." -ForegroundColor Yellow
$proj = Invoke-RestMethod -Uri "http://localhost:8080/api/projects" -Method POST -Body '{"name":"Backend Refactor","description":"Test 3-level hierarchy"}' -ContentType "application/json"
Write-Host "   ✓ Project ID: $($proj.id), Name: $($proj.name), Status: $($proj.status)" -ForegroundColor Green
Write-Host ""

# Test 2: Create Task under Project
Write-Host "2. Creating Task under Project..." -ForegroundColor Yellow
$task = Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" -Method POST -Body "{`"projectId`":$($proj.id),`"name`":`"Implement Task Entity`",`"estimatedPomodoros`":3}" -ContentType "application/json"
Write-Host "   ✓ Task ID: $($task.id), Name: $($task.name), Project ID: $($task.project.id)" -ForegroundColor Green
Write-Host ""

# Test 3: Create Subtask under Task
Write-Host "3. Creating Subtask under Task..." -ForegroundColor Yellow
$subtask = Invoke-RestMethod -Uri "http://localhost:8080/api/subtasks" -Method POST -Body "{`"taskId`":$($task.id),`"name`":`"Update Subtask entity`",`"estimatedPomodoros`":1}" -ContentType "application/json"
Write-Host "   ✓ Subtask ID: $($subtask.id), Name: $($subtask.name), Task ID: $($subtask.task.id)" -ForegroundColor Green
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "✓ 3-Level Hierarchy Test Complete!" -ForegroundColor Green
Write-Host "Hierarchy: Project($($proj.id)) → Task($($task.id)) → Subtask($($subtask.id))" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
