package com.microjob.microjob_exchange.repository;

import com.microjob.microjob_exchange.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Removed simple findByTaskId(Long taskId)

    // --- FIX: DEEP EAGER FETCH FOR MY-APPLICATIONS ---
    // Loads Application, its Applicant, the Task, and the Poster of the Task.
    @Query("SELECT a FROM Application a " +
            "JOIN FETCH a.task t " +      // Load the Task
            "JOIN FETCH t.poster p " +
            "LEFT JOIN FETCH t.acceptor w " +// Load the Poster (t.poster is correct)
            "WHERE a.applicant.id = :applicantId") // Applicant is on Application (a.applicant)
    List<Application> findByApplicantIdEagerly(@Param("applicantId") Long applicantId);

    // --- METHOD FOR POSTER TO VIEW BIDS (EAGERLY FETCHED) ---
    @Query("SELECT a FROM Application a JOIN FETCH a.task t JOIN FETCH a.applicant u WHERE a.task.id = :taskId")
    List<Application> findByTaskIdEagerly(@Param("taskId") Long taskId);
}