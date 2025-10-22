package com.microjob.microjob_exchange.repository;

import com.microjob.microjob_exchange.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. Check if a review already exists for a given task ID
    Optional<Review> findByTaskIdAndReviewerId(Long taskId, Long reviewerId);

    // 2. Retrieve all reviews received by a user (for profile display)
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.task t " + // Eagerly load the Task entity
            "JOIN FETCH t.poster p " + // Eagerly load the Poster (User) of that Task
            "WHERE r.reviewedUser.id = :reviewedUserId")
    List<Review> findByReviewedUserId(@Param("reviewedUserId") Long reviewedUserId);
}