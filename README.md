# DevCollab

> **Real-time collaboration tool for college students** – Free, secure login required, works instantly in your browser.

DevCollab is a Kanban-based project tracker built specifically for student group projects, assignments, and lab work. Track tasks, focus with Pomodoro timers, chat with your team, and see everyone's cursors in real-time.

## 🚀 Features

### ✅ Currently Implemented
- **Real-time Kanban Board** – Drag tasks across TODO, DOING, DONE columns with live updates
- **Customizable Column Colors** – Change column colors with a 7-color picker (red, orange, yellow, green, blue, purple, indigo)
- **MECE Pomodoro System** – Subtask-level Pomodoro estimation, cycle tracking, analytics, and team collaboration (see below)
- **Live Chat** – Text chat with @mentions and notifications
- **Live Cursor Tracking** – See where your teammates are working in real-time
- **Subtask Management** – Break down projects into smaller trackable tasks
- **Export/Import** – Save your project data as JSON
- **Browser Notifications** – Stay updated on team activity

### 🎨 Customization Features (NEW)
- **Column Color Picker**: Click the 🎨 button on any column to select from 7 vibrant colors
- **Hex Color Support**: Backend stores colors as hex values for consistency
- **Visual Feedback**: Checkmark indicates currently selected color
- **Persistent Colors**: Column colors are saved to the database

### 🍅 MECE Pomodoro System (NEW)
- **Subtask Estimation**: Estimate 1-5 Pomodoros (25-min sessions) per subtask
- **Cycle Tracking**: 25/5 cycles, long break after 4 Pomodoros, auto-progress
- **Progress Bars**: Visualize completed vs estimated Pomodoros for each subtask
- **Pause/Distraction Logging**: Track interruptions and reasons
- **Team Collaboration**: See active Pomodoros and cycles for all teammates in real-time
- **Analytics Dashboard**: Weekly reports, efficiency score, daily breakdown, team activity
- **REST API & WebSocket**: For Pomodoro actions and real-time sync

### 🔐 Authentication
- **Login Required**: All features require secure login with email and password
- **No Guest Mode**: Guest/anonymous access has been removed for security and data integrity

### 🔧 Tech Stack
- **Backend:** Java 21 + Spring Boot 3.5.7
- **Frontend:** React 18 + TypeScript + Tailwind CSS
- **Real-time:** Spring WebSocket (STOMP)
- **Database:** H2 (in-memory for development)
- **Security:** Spring Security (form-based login only)
- **Build:** Maven (backend), Vite (frontend)
- **Drag & Drop:** @dnd-kit library for intuitive task reordering

### 📋 Roadmap (What Needs to Be Done)
- [ ] **Deadline Support** – Add due dates with urgency badges ("Due in 2 days")
- [ ] **Progress Badges** – Show subtask completion (e.g., "3/5 subtasks ✓")
- [ ] **Dark Mode** – Eye-friendly theme for late-night study sessions
- [ ] **Archive Functionality** – Move completed projects to archive
- [ ] **Project Labels/Tags** – Categorize projects (CS101, Math, Lab Work)
- [ ] **File Attachments** – Upload PDFs, images, documents to projects
- [ ] **Activity Timeline** – See who did what and when
- [ ] **AI Assistant Integration** – Embed ChatGPT, Grok, or Gemini for quick help
- [ ] **Calendar View** – Visualize project deadlines
- [ ] **Team Analytics** – Track total Pomodoro time, completion rates
- [ ] **Mobile Responsive** – Optimize for phone/tablet use
- [ ] **Persistent Storage** – PostgreSQL/MySQL for production deployment
- [ ] **Multi-board Management** – Create and manage multiple boards
- [ ] **Board Sharing** – Share boards with team members
- [ ] **Column Templates** – Pre-defined column layouts for different project types

## 🏗️ Getting Started

### Prerequisites
- Java 21 (LTS)
- Maven 3.6+
- Node.js 18+ (for frontend development)
- npm or yarn

### Project Structure
```
devcollab/
├── src/                          # Java Spring Boot backend
│   ├── main/java/com/agnel/devcollab/
│   │   ├── controller/           # REST API endpoints
│   │   ├── entity/               # JPA entities (Board, BoardColumn, Project, etc.)
│   │   ├── repository/           # Spring Data repositories
│   │   ├── service/              # Business logic
│   │   ├── config/               # Configuration classes (DataInitializer, SecurityConfig)
│   │   └── dto/                  # Data Transfer Objects (ProjectResponse, CreateProjectRequest)
│   └── main/resources/
│       └── application.properties # Database and Spring configuration
└── frontend/                      # React TypeScript frontend
    ├── src/
    │   ├── pages/                # Page components (Board.tsx)
    │   ├── components/           # Reusable components (BoardColumn, ProjectCard, modals)
    │   ├── services/             # API client (api.ts)
    │   ├── types/                # TypeScript interfaces
    │   └── styles/               # Tailwind CSS
    ├── vite.config.ts            # Vite build configuration
    ├── tsconfig.json             # TypeScript configuration
    └── tailwind.config.js        # Tailwind CSS configuration
```

### Run Locally

#### Backend
```bash
cd DevCollab
mvn spring-boot:run
# Runs on http://localhost:8080
```

#### Frontend (Development)
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173 (or specified by Vite)
```

#### Frontend (Production Build)
```bash
cd frontend
npm run build
# Creates optimized dist/ folder
```

### Database
The application uses H2 in-memory database for development:
- **Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:devcollab`
- **Username**: `sa`
- **Password**: (leave empty)

### Development Setup
1. Clone the repository
2. Build backend: `mvn clean install`
3. Start backend: `mvn spring-boot:run`
4. In another terminal, go to `frontend/` directory
5. Install dependencies: `npm install`
6. Start dev server: `npm run dev`
7. Open browser to the frontend URL (usually `http://localhost:5173`)
8. Login with test credentials (created automatically)

### Default Test User
- **Email**: `test@example.com`
- **Password**: `password`
- **Default Board**: Automatically created on first login

## 📝 Recent Updates (v0.3.0 - Dec 2025)
### Major Changes
- ✅ **Column Color Picker**: Added 🎨 button to customize board column colors with 7 vibrant options
- ✅ **Frontend Modernization**: Migrated from HTMX/Thymeleaf to React 18 + TypeScript + Tailwind CSS
- ✅ **Improved UI/UX**: Better visual design with Tailwind CSS styling and responsive layout
- ✅ **Fixed Bugs**: 
  - Resolved duplicate columns issue when creating new boards
  - Fixed default column colors (red, orange, green) based on position
  - Fixed TypeScript type errors with undefined values
- ✅ **Code Quality**: Added proper DTOs, removed unused imports, improved type safety
- ✅ **Tooling**: 
  - Added Vite as the frontend build tool
  - Configured Tailwind CSS for utility-first styling
  - Added @dnd-kit for improved drag-and-drop functionality
  - Set up TypeScript strict mode

### Previous Updates (v0.2.0 - Dec 2025)
- ✅ **MECE Pomodoro System**: Subtask estimation, cycle tracking, analytics dashboard, team collaboration
- ✅ **Authentication Overhaul**: Guest mode removed, login required for all features
- ✅ **UI Redesign**: Cleaner DOING column, integrated timers, Pomodoro progress bars
- ✅ **Bug Fixes**: HTMX forms, settings dropdown, timer pause logic

## 🤝 Contributing
This is a student project under active development. Contributions, suggestions, and bug reports are welcome!

## 📄 License
MIT License - Free for educational and personal use

---

Built with ❤️ for students, by students
