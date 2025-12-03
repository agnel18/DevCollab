# DevCollab React Frontend

Modern React + TypeScript frontend for DevCollab with Trello-style Kanban board and compact Pomodoro timers.

## Features

- 🎯 Trello-style drag-and-drop Kanban board
- ⏱️ Compact Pomodoro widget on every card (140px × 56px max)
- 🎨 Beautiful sunset gradient background
- 🌙 Dark mode support
- ⚡ Real-time updates via WebSocket
- 📱 Responsive design

## Tech Stack

- React 18 + TypeScript
- Vite (dev server & build tool)
- Tailwind CSS (styling)
- @dnd-kit (drag and drop)
- SockJS + STOMP (WebSocket)

## Getting Started

### Install Dependencies

```powershell
cd frontend
npm install
```

### Run Development Server

```powershell
npm run dev
```

The app will run on `http://localhost:3000` and proxy API requests to `http://localhost:8080` (Spring Boot backend).

### Build for Production

```powershell
npm run build
```

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── PomodoroWidget.tsx    # Compact timer widget (collapsed/expanded states)
│   │   ├── ProjectCard.tsx        # Individual card with timer at bottom
│   │   └── BoardColumn.tsx        # Droppable column (To Do/Doing/Done)
│   ├── pages/
│   │   └── Board.tsx              # Main board page with DnD context
│   ├── services/
│   │   └── api.ts                 # REST API client
│   ├── types/
│   │   └── index.ts               # TypeScript interfaces
│   ├── utils/
│   │   └── helpers.ts             # Helper functions
│   ├── index.css                  # Tailwind imports
│   └── main.tsx                   # Entry point
├── index.html
├── vite.config.ts
├── tailwind.config.js
└── package.json
```

## Pomodoro Widget Behavior

### Collapsed State (Default)
- Shows: ⏱ icon + total time (e.g., "2h 15m")
- Click to expand

### Expanded State
- Shows: Circular progress timer + controls
- Controls: ▶ (Play) | ❚❚ (Pause) | ⏹ (Stop) | ⚙ (Settings)
- Settings dropdown: work duration, breaks, sound, auto-start
- Maximum size: 140px wide × 56px tall

### Rules
- Only ONE timer can run at a time across entire app
- Starting a timer auto-moves card to "Doing" column
- Timer stays visible in all columns (To Do, Doing, Done)
- In Done column, keeps full functionality (can restart if needed)

## Backend Integration

Connects to Spring Boot REST API at `http://localhost:8080`:

- `GET /api/projects` - Fetch all projects
- `POST /api/projects` - Create project
- `PATCH /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project
- `POST /api/projects/{id}/pomodoro/start` - Start timer
- `POST /api/projects/{id}/pomodoro/stop` - Stop/pause timer

WebSocket endpoint: `/ws`

## Development Notes

- Spring Boot backend must be running on port 8080
- Vite dev server proxies `/api` and `/ws` to backend
- Hot reload enabled for fast development
- TypeScript strict mode enabled

## Next Steps

- [ ] Add WebSocket real-time sync
- [ ] Implement @mentions in comments
- [ ] Add cursor presence tracking
- [ ] Build subtask checklist UI
- [ ] Add keyboard shortcuts
- [ ] Implement notifications

## License

MIT
