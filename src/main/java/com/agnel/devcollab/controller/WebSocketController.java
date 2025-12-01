package com.agnel.devcollab.controller;

import com.agnel.devcollab.dto.ChatMessage;
import com.agnel.devcollab.dto.CursorMove;
import com.agnel.devcollab.dto.UserPresence;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public ChatMessage handleChat(ChatMessage chatMessage) {
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }
        
        // Check for @mentions and send notifications
        String message = chatMessage.getMessage();
        if (message != null) {
            Set<String> mentionedUsers = extractMentions(message);
            if (!mentionedUsers.isEmpty()) {
                // Send notification to mentioned users
                for (String mentionedUser : mentionedUsers) {
                    ChatMessage notification = new ChatMessage();
                    notification.setProjectId(chatMessage.getProjectId());
                    notification.setMessage("You were mentioned by " + chatMessage.getUserName() + ": " + message);
                    notification.setUserName("System");
                    notification.setUserColor("#FF6B6B");
                    notification.setTimestamp(LocalDateTime.now());
                    
                    // Send to a user-specific queue (in a real app, you'd have user-specific queues)
                    // For now, we'll broadcast to all and let the client filter
                    messagingTemplate.convertAndSend("/topic/notifications", notification);
                }
            }
        }
        
        return chatMessage;
    }

    @MessageMapping("/cursor")
    @SendTo("/topic/cursors")
    public CursorMove handleCursorMove(CursorMove cursorMove) {
        return cursorMove;
    }

    @MessageMapping("/presence")
    @SendTo("/topic/presence")
    public UserPresence handlePresence(UserPresence presence) {
        return presence;
    }

    private Set<String> extractMentions(String message) {
        Set<String> mentions = new HashSet<>();
        if (message == null) return mentions;
        
        Matcher matcher = MENTION_PATTERN.matcher(message);
        while (matcher.find()) {
            mentions.add(matcher.group(1)); // Extract username without @
        }
        return mentions;
    }
}
