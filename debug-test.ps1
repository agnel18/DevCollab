# Debug script to test API endpoints
$ErrorActionPreference = "Continue"

Write-Output "===== DEBUG TEST ====="

# Test 1: Create Project
Write-Output "`n1. Creating Project..."
$projectBody = '{"name":"DebugProject","estimatedPomodoros":10}'
Write-Output "Body: $projectBody"

try {
    $project = Invoke-RestMethod -Uri "http://localhost:8080/api/projects" -Method POST -Body $projectBody -ContentType "application/json"
    Write-Output "SUCCESS - Project ID: $($project.id)"
    Write-Output "Full response: $($project | ConvertTo-Json -Compress)"
    
    # Test 2: Create Task
    Write-Output "`n2. Creating Task for Project ID: $($project.id)..."
    $taskBody = @{
        project = @{ id = $project.id }
        name = "Debug Task"
        estimatedPomodoros = 5
    } | ConvertTo-Json
    Write-Output "Body: $taskBody"
    
    $task = Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" -Method POST -Body $taskBody -ContentType "application/json"
    Write-Output "SUCCESS - Task ID: $($task.id)"
    Write-Output "Full response: $($task | ConvertTo-Json -Compress)"
    
    # Test 3: Create Subtask
    Write-Output "`n3. Creating Subtask for Task ID: $($task.id)..."
    $subtaskBody = @{
        task = @{ id = $task.id }
        name = "Debug Subtask"
        estimatedPomodoros = 2
    } | ConvertTo-Json
    Write-Output "Body: $subtaskBody"
    
    $subtask = Invoke-RestMethod -Uri "http://localhost:8080/api/subtasks" -Method POST -Body $subtaskBody -ContentType "application/json"
    Write-Output "SUCCESS - Subtask ID: $($subtask.id)"
    Write-Output "Full response: $($subtask | ConvertTo-Json -Compress)"
    
} catch {
    Write-Output "ERROR: $($_.Exception.Message)"
    Write-Output "Status: $($_.Exception.Response.StatusCode.value__)"
    Write-Output "Details: $($_.ErrorDetails.Message)"
}

Write-Output "`n===== END DEBUG TEST ====="
