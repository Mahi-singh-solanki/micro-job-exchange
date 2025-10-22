package com.microjob.microjob_exchange.controller;

import com.microjob.microjob_exchange.model.ChatMessage;
import com.microjob.microjob_exchange.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller; // Keep @Controller for @MessageMapping
import org.springframework.web.bind.annotation.RestController;

// --- IMPORTS TO FIX GET MAPPING ERROR ---
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
// --- END IMPORTS ---

import java.security.Principal;
import java.util.List;

@RestController // NOTE: Using @RestController here means every method returns JSON/data.
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // --- Endpoint for Chat History (REST API) ---
    // @RestController implies @ResponseBody, so @GetMapping is correct if imports are present.
    @GetMapping("/api/chat/{taskId}/history")
    public List<ChatMessage> getChatHistory(@PathVariable Long taskId) {
        return chatMessageRepository.findByTaskIdOrderBySentAtAsc(taskId);
    }


    // --- Endpoint for Real-time Messaging (STOMP) ---
    // This is correct for a STOMP endpoint in a Controller-based component.
    @MessageMapping("/chat/{taskId}")
    public void sendMessage(@DestinationVariable Long taskId, @Payload ChatMessage chatMessage, Principal principal) {

        // ... (rest of the logic) ...

        // 1. Get authenticated user's ID
        Long senderId = 0L;
        if (principal != null) {
            System.out.println("User is authenticated: " + principal.getName());
        }

        // 2. Set necessary fields
        chatMessage.setTaskId(taskId);
        chatMessage.setSenderId(senderId); // Use actual senderId here

        // 3. Save message to database
        chatMessageRepository.save(chatMessage);

        // 4. Broadcast message to all subscribers
        messagingTemplate.convertAndSend("/topic/tasks/" + taskId, chatMessage);
    }
}