package com.microjob.microjob_exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import needed for the fix

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the task this review is about
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    @JsonIgnore // <--- CRITICAL FIX: Ignore Task to prevent circular dependency crash
    private Task task;

    // The user who WROTE the review (Poster or Worker)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @JsonIgnore // <--- CRITICAL FIX: Ignore User entity during Review serialization
    private User reviewer;

    // The user who RECEIVED the review (Poster or Worker)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_user_id", nullable = false)
    @JsonIgnore // <--- CRITICAL FIX: Ignore User entity during Review serialization
    private User reviewedUser;

    @Column(nullable = false)
    private Integer rating; // Rating from 1 to 5

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Enum to indicate the direction of the review
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType;

    public enum ReviewType {
        POSTER_TO_WORKER,
        WORKER_TO_POSTER
    }
}