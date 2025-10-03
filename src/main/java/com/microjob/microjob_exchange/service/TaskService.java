package com.microjob.microjob_exchange.service;

import com.microjob.microjob_exchange.model.Task;
import com.microjob.microjob_exchange.model.Task.Status; // Added import for Task.Status
import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.model.Application; // New Dependency
import com.microjob.microjob_exchange.model.Application.ApplicationStatus; // New Dependency
import com.microjob.microjob_exchange.repository.TaskRepository;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.repository.ApplicationRepository; // New Dependency
import com.microjob.microjob_exchange.DTO.TaskCreationRequest;
import com.microjob.microjob_exchange.DTO.TaskApplyRequest;
import com.microjob.microjob_exchange.DTO.TaskAssignmentRequest; // New Dependency
import com.microjob.microjob_exchange.exception.ResourceNotFoundException;
import com.microjob.microjob_exchange.exception.UnauthorizedActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository; // NEW: Application Repository

    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ApplicationRepository applicationRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository; // Initialized
    }

    // Utility method to fetch User, which will be needed for Poster and Acceptor
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    // -------------------------------------------------------------------------
    // 1. Logic for POST /api/tasks (Add Task)
    // -------------------------------------------------------------------------
    @Transactional
    public Task createTask(TaskCreationRequest request, Long posterId) {
        User poster = getUserById(posterId);
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPrice(request.getPrice());
        task.setLatitude(request.getLatitude());
        task.setLongitude(request.getLongitude());
        task.setDurationMinutes(request.getDurationMinutes());
        task.setPoster(poster);
        task.setStatus(Status.OPEN);
        return taskRepository.save(task);
    }

    // -------------------------------------------------------------------------
    // 2. Logic for GET /api/tasks (Get All Tasks)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Task> getAllOpenTasks() {
        return taskRepository.findByStatus(Status.OPEN);
    }

    // -------------------------------------------------------------------------
    // 3. Logic for GET /api/tasks/{taskId} (Get Task by ID)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
    }

    // -------------------------------------------------------------------------
    // 4. FIX: Logic for POST /api/tasks/{taskId}/apply (Create Application)
    // -------------------------------------------------------------------------
    @Transactional
    public Application applyForTask(Long taskId, Long applicantId, TaskApplyRequest request) {
        Task task = getTaskById(taskId);
        User applicant = getUserById(applicantId);

        if (task.getStatus() != Status.OPEN) {
            throw new UnauthorizedActionException("Task is not open for applications.");
        }
        if (task.getPoster().getId().equals(applicantId)) {
            throw new UnauthorizedActionException("A user cannot apply for their own task.");
        }

        // CREATE AND SAVE THE APPLICATION ENTITY (Replaces placeholder logic)
        Application application = new Application();
        application.setTask(task);
        application.setApplicant(applicant);
        application.setCoverMessage(request.getCoverMessage());
        application.setProposedPrice(request.getProposedPrice());
        application.setStatus(ApplicationStatus.PENDING);

        return applicationRepository.save(application);
    }

    // -------------------------------------------------------------------------
    // 5. NEW: Logic for GET /api/tasks/{taskId}/applications (View Bids)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByTaskId(Long taskId, Long posterId) {
        Task task = getTaskById(taskId);

        // Security check: Only the poster can view applications
        if (!task.getPoster().getId().equals(posterId)) {
            throw new UnauthorizedActionException("You are not authorized to view these applications.");
        }

        return applicationRepository.findByTaskIdEagerly(taskId);
    }

    // -------------------------------------------------------------------------
    // 6. NEW: Logic for POST /api/tasks/{taskId}/assign (Assign Worker)
    // -------------------------------------------------------------------------
    @Transactional
    public Task assignTask(Long taskId, TaskAssignmentRequest request, Long posterId) {
        Task task = getTaskById(taskId);

        // Security checks
        if (!task.getPoster().getId().equals(posterId)) {
            throw new UnauthorizedActionException("Only the poster can assign a worker.");
        }
        if (task.getStatus() != Status.OPEN) {
            throw new UnauthorizedActionException("Task is already assigned or closed.");
        }

        Application selectedApplication = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

        // Update task status and acceptor
        task.setAcceptor(selectedApplication.getApplicant());
        task.setStatus(Status.ASSIGNED);
        taskRepository.save(task);

        // Update application status
        selectedApplication.setStatus(ApplicationStatus.SELECTED);
        applicationRepository.save(selectedApplication);

        // Reject all other pending applications
        applicationRepository.findByTaskId(taskId).stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .forEach(app -> {
                    app.setStatus(ApplicationStatus.REJECTED);
                    applicationRepository.save(app);
                });

        return task;
    }

    // -------------------------------------------------------------------------
    // 7. NEW: Logic for POST /api/tasks/{taskId}/complete (Worker Confirmation)
    // -------------------------------------------------------------------------
    @Transactional
    public Task completeTask(Long taskId, Long workerId) {
        Task task = getTaskById(taskId);

        // Security check: Only the assigned worker can mark as complete
        if (task.getAcceptor() == null || !task.getAcceptor().getId().equals(workerId)) {
            throw new UnauthorizedActionException("You are not the assigned worker for this task.");
        }
        if (task.getStatus() != Status.ASSIGNED) {
            throw new UnauthorizedActionException("Task is not in the assigned state for completion.");
        }

        task.setStatus(Status.COMPLETED);
        return taskRepository.save(task);
    }

    // -------------------------------------------------------------------------
    // 8. NEW: Logic for POST /api/tasks/{taskId}/pay (Poster Finalization)
    // -------------------------------------------------------------------------
    @Transactional
    public Task payTask(Long taskId, Long posterId) {
        Task task = getTaskById(taskId);

        // Security check: Only the poster can finalize and pay
        if (!task.getPoster().getId().equals(posterId)) {
            throw new UnauthorizedActionException("Only the task poster can finalize payment.");
        }
        if (task.getStatus() != Status.COMPLETED) {
            throw new UnauthorizedActionException("Task must be marked as completed before payment.");
        }

        task.setStatus(Status.PAID);
        // NOTE: Actual payment gateway logic would go here
        return taskRepository.save(task);
    }
}