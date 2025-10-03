package com.microjob.microjob_exchange.repository;

import com.microjob.microjob_exchange.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Finds all applications for a specific task ID (used by the poster).
     */
    List<Application> findByTaskId(Long taskId);

    /**
     * Finds all applications made by a specific applicant ID (used by the worker).
     */

    @Query("SELECT a FROM Application a JOIN FETCH a.task t JOIN FETCH a.applicant u WHERE a.task.id = :taskId")
    List<Application> findByTaskIdEagerly(@Param("taskId") Long taskId);
}