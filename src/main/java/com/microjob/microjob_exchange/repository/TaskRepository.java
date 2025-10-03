package com.microjob.microjob_exchange.repository;

import com.microjob.microjob_exchange.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.microjob.microjob_exchange.model.Task.Status;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>  {

    List<Task> findByStatus(Status status);
}
