package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.Project.Status;
import org.springframework.security.access.prepost.PreAuthorize;
import com.agnel.devcollab.entity.Project;
import com.agnel.devcollab.entity.User;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

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

    // === ONLY ONE listProjects METHOD ===
    @GetMapping("/projects")
    @PreAuthorize("permitAll()")
    public String listProjects(Model model, Authentication auth, HttpSession session) {
        List<Project> allProjects;
        boolean isGuest = (auth == null || !auth.isAuthenticated());

        if (!isGuest && auth != null) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            allProjects = projectRepository.findByOwnerId(user.getId());
        } else {
            @SuppressWarnings("unchecked")
            List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
            allProjects = (guestProjects != null) ? guestProjects : new ArrayList<>();
        }

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
        model.addAttribute("isGuest", isGuest);
        model.addAttribute("pageTitle", "My Projects");

        return "projects/list";
    }

    @GetMapping("/projects/column/{status}")
    @PreAuthorize("permitAll()")
    public String getColumnFragment(@PathVariable String status, 
                                   Model model, 
                                   Authentication auth, 
                                   HttpSession session) {
        List<Project> allProjects;
        boolean isGuest = (auth == null || !auth.isAuthenticated());

        if (!isGuest && auth != null) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            allProjects = projectRepository.findByOwnerId(user.getId());
        } else {
            @SuppressWarnings("unchecked")
            List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
            allProjects = (guestProjects != null) ? guestProjects : new ArrayList<>();
        }

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
    @PreAuthorize("permitAll()")
    public String newProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "projects/form";
    }

    @GetMapping("/projects/export")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> exportGuestProjects(HttpSession session, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.badRequest().body("Only guests can export");
        }

        @SuppressWarnings("unchecked")
        List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
        if (guestProjects == null || guestProjects.isEmpty()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=devcollab-guest.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("[]");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            String json = mapper.writeValueAsString(guestProjects);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=devcollab-guest.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body("Export failed");
        }
    }

    @GetMapping("/projects/{projectId}/subtasks/new")
    @PreAuthorize("permitAll()")
    public String newSubtaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("subtask", new Subtask());
        model.addAttribute("projectId", projectId);
        return "projects/subtask-form";
    }

    @PostMapping("/projects/import")
    @PreAuthorize("permitAll()")
    public String importGuestProjects(@RequestParam("file") MultipartFile file,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file");
            return "redirect:/projects";
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            List<Project> imported = mapper.readValue(
                file.getInputStream(),
                new TypeReference<List<Project>>() {}
            );

            @SuppressWarnings("unchecked")
            List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
            if (guestProjects == null) {
                guestProjects = new ArrayList<>();
                session.setAttribute("guestProjects", guestProjects);
            }

            guestProjects.addAll(imported);

            Long nextProjectId = (Long) session.getAttribute("nextGuestProjectId");
            if (nextProjectId == null) {
                nextProjectId = -1L;
            }
            Long nextSubtaskId = (Long) session.getAttribute("nextGuestSubtaskId");
            if (nextSubtaskId == null) {
                nextSubtaskId = -1L;
            }
            for (Project p : imported) {
                if (p.getId() == null) {
                    p.setId(nextProjectId);
                    nextProjectId--;
                }
                for (Subtask s : p.getSubtasks()) {
                    if (s.getId() == null) {
                        s.setId(nextSubtaskId);
                        nextSubtaskId--;
                    }
                }
            }
            session.setAttribute("nextGuestProjectId", nextProjectId);
            session.setAttribute("nextGuestSubtaskId", nextSubtaskId);

            redirectAttributes.addFlashAttribute("message", "Imported " + imported.size() + " project(s)!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Import failed: " + e.getMessage());
        }

        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/delete")
    @PreAuthorize("permitAll()")
    public String deleteProject(@PathVariable Long id,
                                Authentication auth,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        boolean isGuest = (auth == null || !auth.isAuthenticated());
        String projectName = null;

        if (isGuest) {
            @SuppressWarnings("unchecked")
            List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
            if (guestProjects != null) {
                Project toDelete = guestProjects.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(id))
                    .findFirst()
                    .orElse(null);
                if (toDelete != null) {
                    projectName = toDelete.getName();
                    guestProjects.removeIf(p -> p.getId() != null && p.getId().equals(id));
                }
            }
        } else {
            Project project = projectRepository.findById(id).orElse(null);
            if (project != null) {
                projectName = project.getName();
                projectRepository.deleteById(id);
            }
        }

        // Broadcast update
        if (projectName != null) {
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
    @PreAuthorize("permitAll()")
    public String updateStatus(@PathVariable Long id,
                            @RequestParam Status status,
                            Authentication auth,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        boolean isGuest = (auth == null || !auth.isAuthenticated());
        Project project = null;

        if (isGuest) {
            @SuppressWarnings("unchecked")
            List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
            if (guestProjects != null) {
                project = guestProjects.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(id))
                    .findFirst()
                    .orElse(null);
                if (project != null) {
                    project.setStatus(status);
                }
            }
        } else {
            project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
            project.setStatus(status);
            projectRepository.save(project);
        }

        // Broadcast update
        if (project != null) {
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
        }

        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/status/ajax")
    @PreAuthorize("permitAll()")
    @ResponseBody
    public ResponseEntity<String> updateStatusAjax(@PathVariable Long id,
                                                   @RequestParam Status status,
                                                   Authentication auth,
                                                   HttpSession session,
                                                   @RequestHeader(value = "X-User-Name", required = false) String userName,
                                                   @RequestHeader(value = "X-User-Color", required = false) String userColor) {
        boolean isGuest = (auth == null || !auth.isAuthenticated());
        Project project = null;

        if (isGuest) {
            @SuppressWarnings("unchecked")
            List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
            if (guestProjects != null) {
                project = guestProjects.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(id))
                    .findFirst()
                    .orElse(null);
                if (project != null) {
                    project.setStatus(status);
                }
            }
        } else {
            project = projectRepository.findById(id).orElse(null);
            if (project != null) {
                project.setStatus(status);
                projectRepository.save(project);
            }
        }

        if (project == null) {
            return ResponseEntity.notFound().build();
        }

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
    @PreAuthorize("permitAll()")
    public String startProjectPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateProjectPomodoro(id, auth, session, true);
        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/pomodoro/stop")
    @PreAuthorize("permitAll()")
    public String stopProjectPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateProjectPomodoro(id, auth, session, false);
        return "redirect:/projects";
    }


    @PostMapping("/projects/{projectId}/subtasks")
    @PreAuthorize("permitAll()")
    public String createSubtask(@PathVariable Long projectId,
                                @ModelAttribute Subtask subtask,
                                Authentication auth,
                                HttpSession session) {
        boolean isGuest = (auth == null || !auth.isAuthenticated());
        Project project = getProject(projectId, auth, session);
        if (project == null) return "redirect:/projects";

        subtask.setProject(project);

        if (isGuest) {
            Long nextSubtaskId = (Long) session.getAttribute("nextGuestSubtaskId");
            if (nextSubtaskId == null) {
                nextSubtaskId = -1L;
            }
            subtask.setId(nextSubtaskId);
            session.setAttribute("nextGuestSubtaskId", nextSubtaskId - 1);
            project.getSubtasks().add(subtask);
            // No DB save needed
        } else {
            subtaskRepository.save(subtask);
        }
        return "redirect:/projects";
    }

    @PostMapping("/subtasks/{id}/pomodoro/start")
    @PreAuthorize("permitAll()")
    public String startSubtaskPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateSubtaskPomodoro(id, auth, session, true);
        return "redirect:/projects";
    }

    @PostMapping("/subtasks/{id}/pomodoro/stop")
    @PreAuthorize("permitAll()")
    public String stopSubtaskPomodoro(@PathVariable Long id, Authentication auth, HttpSession session) {
        updateSubtaskPomodoro(id, auth, session, false);
        return "redirect:/projects";
    }

    private void updateProjectPomodoro(Long id, Authentication auth, HttpSession session, boolean start) {
        boolean isGuest = (auth == null || !auth.isAuthenticated());
        Project project;

        if (isGuest) {
            List<Project> projects = getGuestProjects(session);
            project = projects.stream()
                .filter(p -> p.getId() != null && p.getId().equals(id))
                .findFirst().orElse(null);
        } else {
            project = projectRepository.findById(id).orElse(null);
        }

        if (project == null) return;

        if (start) {
            project.setPomodoroStart(LocalDateTime.now());
        } else {
            LocalDateTime startTime = project.getPomodoroStart();
            if (startTime != null) {
                // Calculate elapsed time in seconds, then convert to minutes (more accurate)
                long elapsedSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
                // Always record at least 1 minute if timer was running (even if less than 1 minute elapsed)
                // This ensures time is never lost
                long minutes = 1; // Minimum 1 minute
                if (elapsedSeconds > 60) {
                    // For times over 1 minute, round up to capture partial minutes
                    minutes = (elapsedSeconds + 59) / 60;
                }
                project.setTotalMinutesSpent(project.getTotalMinutesSpent() + (int) minutes);
                project.setPomodoroStart(null);
            }
        }

        if (!isGuest) {
            projectRepository.save(project);
        }

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
        boolean isGuest = (auth == null || !auth.isAuthenticated());
        Subtask subtask = null;
        Project project = null;

        if (isGuest) {
            List<Project> projects = getGuestProjects(session);
            for (Project p : projects) {
                subtask = p.getSubtasks().stream()
                    .filter(s -> s.getId() != null && s.getId().equals(id))
                    .findFirst().orElse(null);
                if (subtask != null) {
                    project = p;
                    break;
                }
            }
        } else {
            subtask = subtaskRepository.findById(id).orElse(null);
            if (subtask != null) {
                project = subtask.getProject();
            }
        }

        if (subtask == null || project == null) return;

        if (start) {
            subtask.setPomodoroStart(LocalDateTime.now());
        } else {
            LocalDateTime startTime = subtask.getPomodoroStart();
            if (startTime != null) {
                // Calculate elapsed time in seconds, then convert to minutes (more accurate)
                long elapsedSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
                // Always record at least 1 minute if timer was running (even if less than 1 minute elapsed)
                // This ensures time is never lost
                long minutes = 1; // Minimum 1 minute
                if (elapsedSeconds > 60) {
                    // For times over 1 minute, round up to capture partial minutes
                    minutes = (elapsedSeconds + 59) / 60;
                }
                
                // Add elapsed time to subtask total
                subtask.setTotalMinutesSpent(subtask.getTotalMinutesSpent() + minutes);
                
                // Add the SAME elapsed time to project total (subtask time contributes to project)
                project.setTotalMinutesSpent(project.getTotalMinutesSpent() + (int) minutes);
                
                subtask.setPomodoroStart(null);
            }
        }

        if (!isGuest) {
            subtaskRepository.save(subtask);
            if (project != null) {
                projectRepository.save(project);
            }
        }

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
    @PreAuthorize("permitAll()")
    public String createProject(@ModelAttribute Project project,
                                Authentication auth,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        boolean isGuest = (auth == null || !auth.isAuthenticated());

        if (isGuest || auth == null) {
            @SuppressWarnings("unchecked")
            List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
            if (guestProjects == null) {
                guestProjects = new ArrayList<>();
                session.setAttribute("guestProjects", guestProjects);
            }
            project.setCreatedAt(LocalDateTime.now());
            Long nextProjectId = (Long) session.getAttribute("nextGuestProjectId");
            if (nextProjectId == null) {
                nextProjectId = -1L;
            }
            project.setId(nextProjectId);
            session.setAttribute("nextGuestProjectId", nextProjectId - 1);
            guestProjects.add(project);
        } else {
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            project.setOwner(user);
            project.setCreatedAt(LocalDateTime.now());
            projectRepository.save(project);
        }

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

    private Project getProject(Long id, Authentication auth, HttpSession session) {
    boolean isGuest = (auth == null || !auth.isAuthenticated());
    if (isGuest) {
        List<Project> projects = getGuestProjects(session);
        return projects.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    } else {
        return projectRepository.findById(id).orElse(null);
    }
}

    private List<Project> getGuestProjects(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Project> guestProjects = (List<Project>) session.getAttribute("guestProjects");
        return (guestProjects != null) ? guestProjects : new ArrayList<>();
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