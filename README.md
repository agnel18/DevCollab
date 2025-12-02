# DevCollab

> **Real-time collaboration tool for college students** – Free, secure login required, works instantly in your browser.

DevCollab is a Kanban-based project tracker built specifically for student group projects, assignments, and lab work. Track tasks, focus with Pomodoro timers, chat with your team, and see everyone's cursors in real-time.

## 🚀 Features

### ✅ Currently Implemented
- **Real-time Kanban Board** – Drag tasks across TODO, DOING, DONE columns with live updates
- **MECE Pomodoro System** – Subtask-level Pomodoro estimation, cycle tracking, analytics, and team collaboration (see below)
- **Live Chat** – Text chat with @mentions and notifications
- **Live Cursor Tracking** – See where your teammates are working in real-time
- **Subtask Management** – Break down projects into smaller trackable tasks
- **Export/Import** – Save your project data as JSON
- **Browser Notifications** – Stay updated on team activity

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
- **Frontend:** HTMX + Thymeleaf templates
- **Real-time:** Spring WebSocket (STOMP)
- **Database:** H2 (in-memory for development)
- **Security:** Spring Security (form-based login only)
- **Build:** Maven

### 📋 Roadmap (What Needs to Be Done)
- [ ] **Drag & Drop** – Intuitive task reordering with SortableJS
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

## 🏗️ Getting Started

### Prerequisites
- Java 21 (LTS)
- Maven 3.6+

### Run Locally
```bash
git clone https://github.com/agnel18/DevCollab.git
cd DevCollab
mvn spring-boot:run
```

Access the app at `http://localhost:8080`

### Development Mode
The application runs with:
- H2 in-memory database (data resets on restart)
- H2 console enabled at `/h2-console`
- Thymeleaf template caching disabled for live reloading

## 📝 Recent Updates (v0.2.0 - Dec 2025)
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
