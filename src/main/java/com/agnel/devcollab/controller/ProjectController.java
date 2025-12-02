package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.Project.Status;
import org.springframework.security.access.prepost.PreAuthorize;
import com.agnel.devcollab.entity.Project;
import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.entity.Task;
import com.agnel.devcollab.entity.Subtask; 
import com.agnel.devcollab.repository.ProjectRepository;
import com.agnel.devcollab.repository.UserRepository;
import com.agnel.devcollab.repository.SubtaskRepository; 
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.agnel.devcollab.dto.ProjectUpdate;

@Controller
public class ProjectController {

    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SubtaskRepository subtaskRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/projects")
    public String listProjects(Model model, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Project> allProjects = projectRepository.findByOwnerId(user.getId());

        // PRE-FILTER IN JAVA
        List<Project> todo = allProjects.stream()
                .filter(p -> p.getStatus() == Project.Status.TODO)
                .toList();
        List<Project> doing = allProjects.stream()
                .filter(p -> p.getStatus() == Project.Status.DOING)
                .toList();
        List<Project> done = allProjects.stream()
                .filter(p -> p.getStatus() == Project.Status.DONE)
                .toList();

        model.addAttribute("todo", todo);
        model.addAttribute("doing", doing);
        model.addAttribute("done", done);
        model.addAttribute("pageTitle", "My Projects");

        return "projects/list";
    }

    @GetMapping("/projects/column/{status}")
    public String getColumnFragment(@PathVariable String status, 
                                   Model model, 
                                   Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Project> allProjects = projectRepository.findByOwnerId(user.getId());

        // Filter by requested status
        List<Project> projects;
        switch (status.toUpperCase()) {
            case "TODO":
                projects = allProjects.stream()
                    .filter(p -> p.getStatus() == Project.Status.TODO)
                    .toList();
                break;
            case "DOING":
                projects = allProjects.stream()
                    .filter(p -> p.getStatus() == Project.Status.DOING)
                    .toList();
                break;
            case "DONE":
                projects = allProjects.stream()
                    .filter(p -> p.getStatus() == Project.Status.DONE)
                    .toList();
                break;
            default:
                projects = new ArrayList<>();
        }

        model.addAttribute("projects", projects);
        return "projects/fragments :: column";
    }

    @GetMapping("/projects/new")
    public String newProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "projects/form";
    }

    @GetMapping("/projects/{projectId}/subtasks/new")
    public String newSubtaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("subtask", new Subtask());
        model.addAttribute("projectId", projectId);
        return "projects/subtask-form";
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            String projectName = project.getName();
            projectRepository.deleteById(id);
            
            // Broadcast update
            String userName = getOrCreateUserName(session);
            String userColor = getOrCreateUserColor(session);
            ProjectUpdate update = new ProjectUpdate(
                id,
                projectName,
                null,
                null,
                "DELETED",
                userName,
                userColor
            );
            messagingTemplate.convertAndSend("/topic/project-updates", update);
        }

        redirectAttributes.addFlashAttribute("message", "Project deleted!");
        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/status")
    public String updateStatus(@PathVariable Long id,
                            @RequestParam Status status,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setStatus(status);
        projectRepository.save(project);

        // Broadcast update
        String userName = getOrCreateUserName(session);
        String userColor = getOrCreateUserColor(session);
        ProjectUpdate update = new ProjectUpdate(
            project.getId(),
            project.getName(),
            project.getStatus(),
            project.getPomodoroStart(),
            "MOVED",
            userName,
            userColor
        );
        messagingTemplate.convertAndSend("/topic/project-updates", update);

        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/status/ajax")
    @ResponseBody
    public ResponseEntity<String> updateStatusAjax(@PathVariable Long id,
                                                   @RequestParam Status status,
                                                   HttpSession session,
                                                   @RequestHeader(value = "X-User-Name", required = false) String userName,
                                                   @RequestHeader(value = "X-User-Color", required = false) String userColor) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        
        project.setStatus(status);
        projectRepository.save(project);

        // Use headers or fallback to session
        if (userName == null) {
            userName = getOrCreateUserName(session);
        }
        if (userColor == null) {
            userColor = getOrCreateUserColor(session);
        }

        // Broadcast update via WebSocket
        ProjectUpdate update = new ProjectUpdate(
            project.getId(),
            project.getName(),
            project.getStatus(),
            project.getPomodoroStart(),
            "MOVED",
            userName,
            userColor
        );
        messagingTemplate.convertAndSend("/topic/project-updates", update);

        return ResponseEntity.ok("Status updated");
    }
    
    @PostMapping("/projects/{id}/pomodoro/start")
    public String startProjectPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateProjectPomodoro(id, auth, session, true);
        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/pomodoro/stop")
    public String stopProjectPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateProjectPomodoro(id, auth, session, false);
        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/pomodoro/duration")
    public String setPomodoroDuration(@PathVariable Long id, 
                                      @RequestParam int duration) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            project.setPomodoroDuration(duration);
            // Set break proportionally (1:5 ratio - for every 25 min work, 5 min break)
            project.setBreakDuration(Math.max(5, duration / 5));
            projectRepository.save(project);
        }

        return "redirect:/projects";
    }

    @PostMapping("/projects/{projectId}/subtasks")
    public String createSubtask(@PathVariable Long projectId,
                                @PathVariable Long taskId,
                                @ModelAttribute Subtask subtask) {
        // This endpoint needs refactoring for Task hierarchy
        // For now, return error as it's incompatible with new structure
        return "redirect:/projects";
    }

    @PostMapping("/subtasks/{id}/pomodoro/start")
    public String startSubtaskPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateSubtaskPomodoro(id, auth, session, true);
        return "redirect:/projects";
    }

    @PostMapping("/subtasks/{id}/pomodoro/stop")
    public String stopSubtaskPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateSubtaskPomodoro(id, auth, session, false);
        return "redirect:/projects";
    }

    private void updateProjectPomodoro(Long id, Authentication auth, HttpSession session, boolean start) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) return;

        if (start) {
            // Auto-move to DOING when timer starts (only if starting work session, not break)
            if (project.getStatus() == Project.Status.TODO && !project.isBreak()) {
                project.setStatus(Project.Status.DOING);
            }
            project.setPomodoroStart(LocalDateTime.now());
        } else {
            LocalDateTime startTime = project.getPomodoroStart();
            if (startTime != null) {
                // Calculate elapsed time in seconds
                long elapsedSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
                // Always record at least 1 second if timer was running
                if (elapsedSeconds < 1) {
                    elapsedSeconds = 1;
                }
                
                // Only count work time, not break time
                if (!project.isBreak()) {
                    project.setTotalSecondsSpent(project.getTotalSecondsSpent() + elapsedSeconds);
                }
                
                // Check if timer naturally completed (elapsed >= target duration)
                long targetSeconds = project.isBreak() ? project.getBreakDuration() * 60L : project.getPomodoroDuration() * 60L;
                if (elapsedSeconds >= targetSeconds - 5) { // Within 5 seconds of completion
                    // Auto-toggle to next cycle
                    project.setBreak(!project.isBreak());
                }
                
                project.setPomodoroStart(null);
            }
        }

        projectRepository.save(project);

        // Broadcast update
        String userName = getOrCreateUserName(session);
        String userColor = getOrCreateUserColor(session);
        ProjectUpdate update = new ProjectUpdate(
            project.getId(),
            project.getName(),
            project.getStatus(),
            project.getPomodoroStart(),
            start ? "TIMER_STARTED" : "TIMER_STOPPED",
            userName,
            userColor
        );
        messagingTemplate.convertAndSend("/topic/project-updates", update);
    }

    private void updateSubtaskPomodoro(Long id, Authentication auth, HttpSession session, boolean start) {
        Subtask subtask = subtaskRepository.findById(id).orElse(null);
        if (subtask == null) return;
        
        Task task = subtask.getTask();
        if (task == null) return;
        
        Project project = task.getProject();
        if (project == null) return;

        if (start) {
            subtask.setPomodoroStart(LocalDateTime.now());
        } else {
            LocalDateTime startTime = subtask.getPomodoroStart();
            if (startTime != null) {
                // Calculate elapsed time in seconds
                long elapsedSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
                // Always record at least 1 second if timer was running
                if (elapsedSeconds < 1) {
                    elapsedSeconds = 1;
                }
                
                // Add elapsed time to subtask total
                subtask.setTotalSecondsSpent(subtask.getTotalSecondsSpent() + elapsedSeconds);
                
                // Add the SAME elapsed time to project total (subtask time contributes to project)
                project.setTotalSecondsSpent(project.getTotalSecondsSpent() + elapsedSeconds);
                
                subtask.setPomodoroStart(null);
            }
        }

        subtaskRepository.save(subtask);
        projectRepository.save(project);

        // Broadcast project update so all users see correct total
        String userName = getOrCreateUserName(session);
        String userColor = getOrCreateUserColor(session);
        ProjectUpdate update = new ProjectUpdate(
            project.getId(),
            project.getName(),
            project.getStatus(),
            project.getPomodoroStart(),
            start ? "TIMER_STARTED" : "TIMER_STOPPED",
            userName,
            userColor
        );
        messagingTemplate.convertAndSend("/topic/project-updates", update);
    }
    @PostMapping("/projects")
    public String createProject(@ModelAttribute Project project,
                                Authentication auth,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        project.setOwner(user);
        project.setCreatedAt(LocalDateTime.now());
        projectRepository.save(project);

        // Broadcast update
        String userName = getOrCreateUserName(session);
        String userColor = getOrCreateUserColor(session);
        ProjectUpdate update = new ProjectUpdate(
            project.getId(),
            project.getName(),
            project.getStatus(),
            project.getPomodoroStart(),
            "CREATED",
            userName,
            userColor
        );
        messagingTemplate.convertAndSend("/topic/project-updates", update);

        redirectAttributes.addFlashAttribute("message", "Project created!");
        return "redirect:/projects";
    }

    private String getOrCreateUserName(HttpSession session) {
        String userName = (String) session.getAttribute("userName");
        if (userName == null || userName.isEmpty()) {
            userName = "Guest" + System.currentTimeMillis();
            session.setAttribute("userName", userName);
        }
        return userName;
    }

    private String getOrCreateUserColor(HttpSession session) {
        String userColor = (String) session.getAttribute("userColor");
        if (userColor == null || userColor.isEmpty()) {
            String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"};
            userColor = colors[(int) (Math.random() * colors.length)];
            session.setAttribute("userColor", userColor);
        }
        return userColor;
    }

}