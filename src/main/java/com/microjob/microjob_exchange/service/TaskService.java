package com.microjob.microjob_exchange.service;


import com.microjob.microjob_exchange.model.Review;
import com.microjob.microjob_exchange.DTO.ReviewProfileResponse;
import com.microjob.microjob_exchange.repository.ReviewRepository;
import com.microjob.microjob_exchange.DTO.ReviewRequest;
import com.microjob.microjob_exchange.model.Task;
import com.microjob.microjob_exchange.model.Task.Status;
import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.model.Application;
import com.microjob.microjob_exchange.model.Application.ApplicationStatus;
import com.microjob.microjob_exchange.repository.TaskRepository;
import com.microjob.microjob_exchange.repository.UserRepository;
import com.microjob.microjob_exchange.repository.ApplicationRepository;
import com.microjob.microjob_exchange.DTO.TaskCreationRequest;
import com.microjob.microjob_exchange.DTO.TaskApplyRequest;
import com.microjob.microjob_exchange.DTO.TaskAssignmentRequest;
import com.microjob.microjob_exchange.exception.ResourceNotFoundException;
import com.microjob.microjob_exchange.exception.UnauthorizedActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository; // Corrected casing

    // -------------------------------------------------------------------------
    // CRITICAL FIX: Combined Constructor Injection
    // -------------------------------------------------------------------------
    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       ApplicationRepository applicationRepository,
                       ReviewRepository reviewRepository) { // Corrected parameter name

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.reviewRepository = reviewRepository; // Correct initialization
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
        task.setPosterContactEmail(request.getContactEmail());
        task.setPosterPhoneNumber(request.getContactPhoneNumber());
        task.setPoster(poster);
        task.setStatus(Status.OPEN);
        return taskRepository.save(task);
    }

    // -------------------------------------------------------------------------
    // 2. Logic for GET /api/tasks (Get All Tasks)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() { // Renamed from getAllOpenTasks for clarity

        // Call the new query that retrieves ALL tasks regardless of status
        return taskRepository.findAllEagerly();
    }
    // -------------------------------------------------------------------------
    // 3. Logic for GET /api/tasks/{taskId} (Get Task by ID)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId) {
        // CRITICAL FIX: Use the eager query to load poster/acceptor data before serialization
        return taskRepository.findByIdEagerly(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
    }



    // -------------------------------------------------------------------------
    // 4. Logic for POST /api/tasks/{taskId}/apply (Create Application)
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

        // CREATE AND SAVE THE APPLICATION ENTITY
        Application application = new Application();
        application.setTask(task);
        application.setApplicant(applicant);
        application.setCoverMessage(request.getCoverMessage());
        application.setProposedPrice(request.getProposedPrice());
        application.setWorkerContactEmail(request.getWorkerContactEmail());
        application.setWorkerPhoneNumber(request.getWorkerPhoneNumber());
        application.setStatus(ApplicationStatus.PENDING);

        return applicationRepository.save(application);
    }

    // -------------------------------------------------------------------------
    // 5. Logic for GET /api/tasks/my-applications (Get Jobs I Applied For)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Application> getMyApplications(Long applicantId) {
        // Calling the deeply EAGER fetch method from the repository (FIXED)
        return applicationRepository.findByApplicantIdEagerly(applicantId);
    }

    // -------------------------------------------------------------------------
    // 6. Logic for GET /api/tasks/{taskId}/applications (View Bids for a Task)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByTaskId(Long taskId, Long posterId) {
        Task task = getTaskById(taskId);

        if (!task.getPoster().getId().equals(posterId)) {
            throw new UnauthorizedActionException("You are not authorized to view these applications.");
        }

        return applicationRepository.findByTaskIdEagerly(taskId);
    }

    // -------------------------------------------------------------------------
    // 7. Logic for POST /api/tasks/{taskId}/assign (Assign Worker)
    // -------------------------------------------------------------------------
    // Inside com.microjob.microjob_exchange.service.TaskService.java

    @Transactional
    public Task assignTask(Long taskId, TaskAssignmentRequest request, Long posterId) {
        Task task = getTaskById(taskId);

        // 1. Initial validation checks (remain correct)
        if (!task.getPoster().getId().equals(posterId)) {
            throw new UnauthorizedActionException("Only the poster can assign a worker.");
        }
        if (task.getStatus() != Status.OPEN) {
            throw new UnauthorizedActionException("Task is already assigned or closed.");
        }

        // 2. Application lookup and updates
        Application selectedApplication = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

        task.setAcceptor(selectedApplication.getApplicant());
        task.setStatus(Status.ASSIGNED);

        // Save the Task update (this closes the session upon method exit)
        taskRepository.save(task);

        // 3. Update application status
        selectedApplication.setStatus(ApplicationStatus.SELECTED);
        applicationRepository.save(selectedApplication);

        // Reject all other pending applications (using eager fetch for safety)
        applicationRepository.findByTaskIdEagerly(taskId).stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .forEach(app -> {
                    app.setStatus(ApplicationStatus.REJECTED);
                    applicationRepository.save(app);
                });

        // 4. CRITICAL FIX: Fetch the fully initialized entity before returning.
        // getTaskById uses the findByIdEagerly query which loads Poster and Acceptor.
        return getTaskById(taskId);
    }
    // -------------------------------------------------------------------------
    // 8. Logic for POST /api/tasks/{taskId}/review (Submit Review)
    // -------------------------------------------------------------------------
    @Transactional
    public Review submitReview(Long taskId, Long reviewerId, ReviewRequest request) {
        Task task = getTaskById(taskId);
        User reviewer = getUserById(reviewerId);

        // 1. Validation
        if (task.getStatus() != Status.PAID) {
            throw new UnauthorizedActionException("Cannot submit a review until the task is paid.");
        }

        // Check if this specific review (direction) already exists
        if (reviewRepository.findByTaskIdAndReviewerId(taskId, reviewerId).isPresent()) {
            throw new UnauthorizedActionException("You have already submitted your review for this task.");
        }

        // 2. Determine REVIEWED USER and Authorization
        User reviewedUser;

        if (request.getReviewType() == Review.ReviewType.POSTER_TO_WORKER) {
            if (!task.getPoster().getId().equals(reviewerId)) {
                throw new UnauthorizedActionException("Only the poster can submit a POSTER_TO_WORKER review.");
            }
            reviewedUser = task.getAcceptor();

        } else if (request.getReviewType() == Review.ReviewType.WORKER_TO_POSTER) {
            if (task.getAcceptor() == null || !task.getAcceptor().getId().equals(reviewerId)) {
                throw new UnauthorizedActionException("Only the assigned worker can submit a WORKER_TO_POSTER review.");
            }
            reviewedUser = task.getPoster();

        } else {
            throw new IllegalArgumentException("Invalid review type.");
        }

        if (reviewedUser == null) {
            throw new ResourceNotFoundException("Reviewed user (poster or acceptor) is missing for this task.");
        }

        // 3. Create and Save Review
        Review review = new Review();
        review.setTask(task);
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewedUser);
        review.setRating(request.getRating());
        review.setComments(request.getComments());
        review.setReviewType(request.getReviewType());

        return reviewRepository.save(review);
    }

    // Optional: Method to get average rating for a user profile
    @Transactional(readOnly = true)
    public double getAverageRatingForUser(Long userId) {
        // Uses the injected and correctly named reviewRepository instance
        List<Review> reviews = reviewRepository.findByReviewedUserId(userId);
        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // -------------------------------------------------------------------------
    // 9. Logic for POST /api/tasks/{taskId}/complete (Worker Confirmation)
    // -------------------------------------------------------------------------
    @Transactional
    public Task completeTask(Long taskId, Long workerId) {
        Task task = getTaskById(taskId);

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
    // 10. Logic for POST /api/tasks/{taskId}/pay (Poster Finalization)
    // -------------------------------------------------------------------------
    @Transactional
    public Task payTask(Long taskId, Long posterId) {
        Task task = getTaskById(taskId);

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
    @Transactional(readOnly = true)
    public ReviewProfileResponse getUserReviewProfile(Long reviewedUserId) {

        // 1. Fetch all reviews for the user using the EAGER FETCHING QUERY
        // This query (defined in ReviewRepository) MUST eagerly load the Task, Poster, and Acceptor
        List<Review> reviews = reviewRepository.findByReviewedUserId(reviewedUserId); // Using the method defined with JOIN FETCH

        // 2. Calculate the average rating (Correct)
        double averageRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        // 3. Populate and return the DTO (Correct)
        ReviewProfileResponse profile = new ReviewProfileResponse();
        profile.setUserId(reviewedUserId);
        profile.setAverageRating(Math.round(averageRating * 10.0) / 10.0);
        profile.setTotalReviews(reviews.size());
        profile.setReviews(reviews); // This now contains fully initialized entities

        return profile;
    }
}