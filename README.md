# DevCollab

> **Real-time collaboration tool for college students** – Free, no login required, works instantly in your browser.

DevCollab is a Kanban-based project tracker built specifically for student group projects, assignments, and lab work. Track tasks, focus with Pomodoro timers, chat with your team, and see everyone's cursors in real-time.

## 🚀 Features

### ✅ Currently Implemented
- **Real-time Kanban Board** – Drag tasks across TODO, DOING, DONE columns with live updates
- **Pomodoro Timer** – Built into each project and subtask for focused work sessions
- **Live Chat** – Text chat with @mentions and notifications
- **Guest Mode** – Start collaborating instantly without creating an account
- **Live Cursor Tracking** – See where your teammates are working in real-time
- **Subtask Management** – Break down projects into smaller trackable tasks
- **Export/Import** – Save your project data as JSON
- **Browser Notifications** – Stay updated on team activity

### 🔧 Tech Stack
- **Backend:** Java 21 + Spring Boot 3.5.7
- **Frontend:** HTMX + Thymeleaf templates
- **Real-time:** Spring WebSocket (STOMP)
- **Database:** H2 (in-memory for development)
- **Security:** Spring Security (configurable guest/auth modes)
- **Build:** Maven

### 📋 Roadmap (Remaining Features)

#### High Priority
- [ ] **Drag & Drop** – Intuitive task reordering with SortableJS
- [ ] **Deadline Support** – Add due dates with urgency badges ("Due in 2 days")
- [ ] **Progress Badges** – Show subtask completion (e.g., "3/5 subtasks ✓")
- [ ] **Dark Mode** – Eye-friendly theme for late-night study sessions

#### Medium Priority
- [ ] **Archive Functionality** – Move completed projects to archive
- [ ] **Project Labels/Tags** – Categorize projects (CS101, Math, Lab Work)
- [ ] **File Attachments** – Upload PDFs, images, documents to projects
- [ ] **Activity Timeline** – See who did what and when

#### Future Enhancements
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

## 📝 Recent Updates (v0.1.0 - Dec 2024)
- ✅ Upgraded to Java 21 LTS
- ✅ Fixed duplicate project rendering bug
- ✅ Improved Thymeleaf fragment parameter passing
- ✅ Optimized development environment (disabled caching)
- ✅ Established GitHub repository with version control

## 🤝 Contributing
This is a student project under active development. Contributions, suggestions, and bug reports are welcome!

## 📄 License
MIT License - Free for educational and personal use

---

Built with ❤️ for students, by students 
