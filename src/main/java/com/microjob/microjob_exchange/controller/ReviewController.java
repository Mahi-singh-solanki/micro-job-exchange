// src/main/java/com/microjob/microjob_exchange/controller/ReviewController.java

package com.microjob.microjob_exchange.controller;

import com.microjob.microjob_exchange.DTO.ReviewProfileResponse;
import com.microjob.microjob_exchange.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final TaskService taskService;

    @Autowired
    public ReviewController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Retrieves the average rating for any user profile.
     * GET /api/reviews/profile/{userId}
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ReviewProfileResponse> getUserProfile(@PathVariable Long userId) {

        // This endpoint can be left unauthenticated or secured depending on your business rules.
        // Assuming public access to view user profiles:

        ReviewProfileResponse profile = taskService.getUserReviewProfile(userId);

        return ResponseEntity.ok(profile);
    }
}