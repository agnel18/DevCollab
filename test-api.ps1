# DevCollab Backend API Testing Script
# Tests the Toggl-inspired time tracking endpoints

Write-Host "`n=== DevCollab Backend API Tests ===`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"

# Test 1: POST /api/timer/start
Write-Host "Test 1: Starting a timer..." -ForegroundColor Yellow
try {
    $startBody = @{
        userId = 1
        subtaskId = 1
        description = "Testing backend API endpoints"
        pomodoro = $false
        billable = $false
    } | ConvertTo-Json
    
    $startResponse = Invoke-RestMethod -Uri "$baseUrl/api/timer/start" `
        -Method POST `
        -Body $startBody `
        -ContentType "application/json"
    
    Write-Host "✓ Timer started successfully!" -ForegroundColor Green
    Write-Host "Entry ID: $($startResponse.id)" -ForegroundColor Gray
    Write-Host "Start Time: $($startResponse.start)" -ForegroundColor Gray
    $entryId = $startResponse.id
} catch {
    Write-Host "✗ Failed to start timer" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Test 2: GET /api/timer/active
Write-Host "`nTest 2: Fetching active timers..." -ForegroundColor Yellow
try {
    $activeResponse = Invoke-RestMethod -Uri "$baseUrl/api/timer/active?userId=1" `
        -Method GET
    
    Write-Host "✓ Active timers retrieved!" -ForegroundColor Green
    Write-Host "Count: $($activeResponse.Count)" -ForegroundColor Gray
    if ($activeResponse.Count -gt 0) {
        Write-Host "Latest: $($activeResponse[0].description)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ Failed to fetch active timers" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Test 3: POST /api/timer/stop (if we got an entry ID)
if ($entryId) {
    Write-Host "`nTest 3: Stopping the timer..." -ForegroundColor Yellow
    try {
        $stopBody = @{
            entryId = $entryId
        } | ConvertTo-Json
        
        $stopResponse = Invoke-RestMethod -Uri "$baseUrl/api/timer/stop" `
            -Method POST `
            -Body $stopBody `
            -ContentType "application/json"
        
        Write-Host "✓ Timer stopped successfully!" -ForegroundColor Green
        Write-Host "End Time: $($stopResponse.end)" -ForegroundColor Gray
        Write-Host "Duration: Started at $($stopResponse.start), ended at $($stopResponse.end)" -ForegroundColor Gray
    } catch {
        Write-Host "✗ Failed to stop timer" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

# Test 4: GET /api/time-entries/week
Write-Host "`nTest 4: Fetching week time entries..." -ForegroundColor Yellow
try {
    $today = Get-Date -Format "yyyy-MM-dd"
    $weekResponse = Invoke-RestMethod -Uri "$baseUrl/api/time-entries/week?start=$today" `
        -Method GET
    
    Write-Host "✓ Week entries retrieved!" -ForegroundColor Green
    Write-Host "Count: $($weekResponse.Count)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to fetch week entries" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Test 5: POST /api/time-entries (manual entry)
Write-Host "`nTest 5: Creating a manual time entry..." -ForegroundColor Yellow
try {
    $manualBody = @{
        userId = 1
        projectId = 1
        description = "Manual time entry test"
        start = (Get-Date).AddHours(-2).ToString("yyyy-MM-ddTHH:mm:ss")
        end = (Get-Date).AddHours(-1).ToString("yyyy-MM-ddTHH:mm:ss")
        tags = @("testing", "manual")
        pomodoro = $false
        billable = $true
    } | ConvertTo-Json
    
    $manualResponse = Invoke-RestMethod -Uri "$baseUrl/api/time-entries" `
        -Method POST `
        -Body $manualBody `
        -ContentType "application/json"
    
    Write-Host "✓ Manual entry created!" -ForegroundColor Green
    Write-Host "Entry ID: $($manualResponse.id)" -ForegroundColor Gray
    $manualEntryId = $manualResponse.id
} catch {
    Write-Host "✗ Failed to create manual entry" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Test 6: PATCH /api/time-entries/{id} (update entry)
if ($manualEntryId) {
    Write-Host "`nTest 6: Updating the manual entry..." -ForegroundColor Yellow
    try {
        $updateBody = @{
            description = "Updated manual time entry"
            tags = @("testing", "manual", "updated")
        } | ConvertTo-Json
        
        $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/time-entries/$manualEntryId" `
            -Method PATCH `
            -Body $updateBody `
            -ContentType "application/json"
        
        Write-Host "✓ Entry updated successfully!" -ForegroundColor Green
        Write-Host "New description: $($updateResponse.description)" -ForegroundColor Gray
    } catch {
        Write-Host "✗ Failed to update entry" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
    
    # Test 7: DELETE /api/time-entries/{id}
    Write-Host "`nTest 7: Deleting the manual entry..." -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/time-entries/$manualEntryId" `
            -Method DELETE
        
        Write-Host "✓ Entry deleted successfully!" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to delete entry" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

Write-Host "`n=== Testing Complete! ===`n" -ForegroundColor Cyan
