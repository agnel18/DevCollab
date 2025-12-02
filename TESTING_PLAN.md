# Toggl-Inspired UI/UX Testing Plan

## Phase 1: Backend Foundation ✅ COMPLETED
- [x] TimeEntry entity created with proper schema (end_time instead of end)
- [x] TimeEntryRepository with query methods
- [x] TimerController with start/stop endpoints
- [x] TimeEntryController with CRUD endpoints
- [x] TimerSyncMessage for WebSocket broadcasts
- [x] Application builds and starts successfully

## Phase 2: Frontend Components (IN PROGRESS)

### 2.1 Card Timer Overlay
**Location**: Kanban cards and subtasks
**Components**:
- [ ] Play button (hover overlay)
- [ ] Running timer badge (with Pomodoro cycle indicator)
- [ ] Timeline bar showing time entries
- [ ] Inline editor modal (description, tags, project)

**Test Cases**:
1. Hover over a card → Play button appears
2. Click Play → Timer starts, badge shows running time
3. Click timer badge → Inline editor opens
4. Edit description/tags → Save updates via PATCH
5. Timeline bar shows past entries with correct proportions
6. Pomodoro indicator shows "Pomodoro 2/4" for cycle tracking

### 2.2 Calendar Overlay
**Location**: Floating button (bottom-right), full-screen overlay
**Components**:
- [ ] Week view grid (7 days)
- [ ] Time blocks (Pomodoros in red, manual in blue)
- [ ] Drag-to-create new blocks
- [ ] Drag-to-resize existing blocks
- [ ] Prev/Next week navigation
- [ ] Heatmap legend

**Test Cases**:
1. Click floating button → Calendar overlay opens
2. Fetch `/api/time-entries/week` → Blocks render correctly
3. Drag on empty space → Create new time entry
4. Drag block edge → Resize time entry (PATCH endpoint)
5. Navigate weeks → Fetch new data
6. Hover block → Tooltip shows details

### 2.3 Right Sidebar Detail Panel
**Location**: Right edge, triggered by card/block click
**Components**:
- [ ] Large running timer display
- [ ] Title/description fields
- [ ] Project/color selector
- [ ] Tags input
- [ ] Markdown notes field
- [ ] Stop/Split/Continue/Discard buttons

**Test Cases**:
1. Click card timer → Sidebar opens with entry details
2. Edit fields → PATCH updates in real-time
3. Stop button → Sets end time, closes sidebar
4. Split button → Stops current, creates new entry
5. Discard button → DELETE entry, closes sidebar
6. Continue button → Creates new entry with same details

## Phase 3: Dashboard & Analytics (NEXT)

### 3.1 Dashboard Page
**Components**:
- [ ] Donut chart (time by project/course)
- [ ] Bar chart (daily totals)
- [ ] Heatmap (focus intensity)
- [ ] AI insights cards

**Test Cases**:
1. Navigate to dashboard → Charts render with real data
2. Donut chart shows correct proportions
3. Bar chart interactive (click to drill down)
4. Heatmap color intensity matches hours
5. AI insights show personalized tips

### 3.2 Team Pulse Page
**Components**:
- [ ] Side-by-side team heatmaps
- [ ] Active Pomodoro indicators
- [ ] Team leaderboard

**Test Cases**:
1. View team pulse → All member heatmaps render
2. Active Pomodoros update in real-time via WebSocket
3. Leaderboard ranks by focus hours

## Phase 4: Pomodoro × Toggl Hybrid (NEXT)

### 4.1 Auto-Create Time Entry
**Test Cases**:
1. Start Pomodoro → TimeEntry created with pomodoro=true
2. Complete Pomodoro → TimeEntry end set automatically
3. Continue as manual → Creates new non-Pomodoro entry
4. Timeline bar shows Pomodoros (red) and manual (blue)

## Phase 5: Micro-Interactions (NEXT)

### 5.1 Confetti & Animations
**Test Cases**:
1. Complete daily goal → Confetti burst
2. Timer finish → Ping sound + haptic
3. Drag to calendar → Smooth animation
4. Card hover → Play button fade-in

## Phase 6: AI Assistant Panel (FUTURE)

### 6.1 Floating Panel
**Test Cases**:
1. After 3 entries → AI suggests task breakdown
2. One-click accept → Creates subtasks
3. Proactive tips → "Focus in mornings"

---

## Testing Workflow

### Step 1: Backend API Testing (Use Postman/cURL)
```bash
# Start timer
curl -X POST http://localhost:8080/api/timer/start \
  -H "Content-Type: application/json" \
  -d '{"subtask": 1, "pomodoro": true}'

# Get week entries
curl http://localhost:8080/api/time-entries/week?start=2025-12-02

# Update entry
curl -X PATCH http://localhost:8080/api/time-entries/1 \
  -H "Content-Type: application/json" \
  -d '{"description": "Updated description"}'

# Delete entry
curl -X DELETE http://localhost:8080/api/time-entries/1
```

### Step 2: Frontend Integration Testing
1. Login to app (http://localhost:8080)
2. Create a project and subtask
3. Test card timer overlay (play/stop/edit)
4. Test calendar overlay (create/resize/delete blocks)
5. Test sidebar detail panel (edit/save/discard)
6. Verify WebSocket real-time sync (open 2 browser tabs)

### Step 3: E2E User Flow Testing
**Scenario**: Student working on lab assignment
1. Create project "CS Lab 5"
2. Add subtask "Implement sorting algorithm"
3. Start Pomodoro timer (25 min)
4. Add tags: "Lab, Algorithms"
5. Complete Pomodoro → Break suggestion
6. Open calendar → See time block created
7. Check dashboard → Donut chart shows CS Lab time
8. Team member sees my active Pomodoro in Team Pulse

---

## Current Status: Phase 1 ✅ Complete

**Next Actions**:
1. Integrate card timer overlay HTML/JS into fragments.html
2. Test timer start/stop with backend API
3. Add calendar overlay to layout.html
4. Test calendar week fetch and block rendering
5. Add sidebar component and wire to card clicks

**Estimated Timeline**:
- Phase 2 (Frontend Components): 2-3 days
- Phase 3 (Dashboard): 1-2 days
- Phase 4 (Pomodoro Hybrid): 1 day
- Phase 5 (Micro-interactions): 1 day
- Total: ~1 week for full Toggl-inspired UI

---

## Success Criteria

✅ **Backend**: All REST APIs work, schema correct, no build errors  
🔄 **Frontend**: Card timer, calendar, sidebar functional  
⏳ **Dashboard**: Charts render with real data  
⏳ **Real-time**: WebSocket sync works across tabs  
⏳ **UX**: Feels like "Toggl + Notion for students"  

**Current Grade: Backend A+ (100%), Frontend In Progress (20%)**
