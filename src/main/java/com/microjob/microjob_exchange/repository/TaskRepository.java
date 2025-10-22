package com.microjob.microjob_exchange.repository;

import com.microjob.microjob_exchange.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import com.microjob.microjob_exchange.model.Task.Status;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>  {

    List<Task> findByStatus(Status status);
    // Inside com.microjob.microjob_exchange.repository.TaskRepository.java

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.poster p LEFT JOIN FETCH t.acceptor a")
    List<Task> findAllEagerly();
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.poster p LEFT JOIN FETCH t.acceptor a WHERE t.id = :taskId")
    Optional<Task> findByIdEagerly(@Param("taskId") Long taskId);
}
