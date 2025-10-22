package com.microjob.microjob_exchange.controller;
import com.microjob.microjob_exchange.DTO.TaskApplyRequest;
import com.microjob.microjob_exchange.model.Review;
import com.microjob.microjob_exchange.DTO.ReviewRequest;
import com.microjob.microjob_exchange.DTO.TaskAssignmentRequest;
import com.microjob.microjob_exchange.model.Application;
import com.microjob.microjob_exchange.DTO.TaskCreationRequest;
import com.microjob.microjob_exchange.model.Task;
import com.microjob.microjob_exchange.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.model.User;
import org.springframework.web.bind.annotation.*;
import com.microjob.microjob_exchange.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @Autowired
    private UserRepository userRepository;
    // --- Placeholder for Auth Context (Replace with Spring Security logic) ---
    private Long getUserIdFromAuthContext() {
        // 1. Get the authenticated principal (the email string)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Look up the full User object to get the ID
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in DB."));

        return user.getId(); // <--- This will correctly return 3
    }
    // --------------------------------------------------------------------------

    // 1. POST /api/tasks (Add Task)
    @PostMapping
    public ResponseEntity<Task> addTask(@RequestBody TaskCreationRequest request) {
        Long posterId = getUserIdFromAuthContext(); // Get the ID of the user creating the task
        Task createdTask = taskService.createTask(request, posterId);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // 2. GET /api/tasks (Get All Tasks)
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        // Call the updated method (which is now returning ALL tasks)
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // 3. GET /api/tasks/{taskId} (Get Task by ID)
    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    // 4. POST /api/tasks/{taskId}/apply (Apply for a Task)
    @PostMapping("/{taskId}/apply")
    public ResponseEntity<Application> applyForTask(@PathVariable Long taskId, @RequestBody TaskApplyRequest request) {
        Long applicantId = getUserIdFromAuthContext(); // Get the ID of the user applying

        Application application = taskService.applyForTask(taskId, applicantId, request);

        return new ResponseEntity<>(application, HttpStatus.CREATED);

    }
    @GetMapping("/{taskId}/applications")
    public ResponseEntity<List<Application>> getApplications(@PathVariable Long taskId) {
        Long posterId = getUserIdFromAuthContext();
        List<Application> applications = taskService.getApplicationsByTaskId(taskId, posterId);
        return ResponseEntity.ok(applications);
    }
    @GetMapping("/my-applications")
    public ResponseEntity<List<Application>> getMyApplications() {
        // 1. Get the current user's ID from the JWT token
        Long applicantId = getUserIdFromAuthContext();

        // 2. Call the new service method
        List<Application> applications = taskService.getMyApplications(applicantId);

        // 3. Return the list
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<Task> assignTask(@PathVariable Long taskId, @RequestBody TaskAssignmentRequest request) {
        Long posterId = getUserIdFromAuthContext();
        Task updatedTask = taskService.assignTask(taskId, request, posterId);
        return ResponseEntity.ok(updatedTask);
    }
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long taskId) {
        Long workerId = getUserIdFromAuthContext();
        Task updatedTask = taskService.completeTask(taskId, workerId);
        return ResponseEntity.ok(updatedTask);
    }
    @PostMapping("/{taskId}/pay")
    public ResponseEntity<Task> payTask(@PathVariable Long taskId) {
        Long posterId = getUserIdFromAuthContext();
        Task updatedTask = taskService.payTask(taskId, posterId);
        return ResponseEntity.ok(updatedTask);
    }
    @PostMapping("/{taskId}/review")
    public ResponseEntity<Review> submitReview(@PathVariable Long taskId,
                                               @RequestBody ReviewRequest request) {

        Long reviewerId = getUserIdFromAuthContext(); // User submitting the review

        Review newReview = taskService.submitReview(taskId, reviewerId, request);

        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    // Optional: Endpoint to view a user's rating profile

}