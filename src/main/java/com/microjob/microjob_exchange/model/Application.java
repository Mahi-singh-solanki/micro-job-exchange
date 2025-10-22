package com.microjob.microjob_exchange.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Task being applied for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // The User applying for the task (the Worker)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(columnDefinition = "TEXT")
    private String coverMessage;

    @Column(name = "proposed_price", nullable = true)
    private BigDecimal proposedPrice; // Optional: if bidding is allowed

    @Column(name = "worker_contact_email", nullable = true) // <-- NEW FIELD
    private String workerContactEmail;

    @Column(name = "worker_phone", nullable = true, length = 15) // <-- NEW FIELD
    private String workerPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();


    public enum ApplicationStatus {
        PENDING,
        SELECTED, // Chosen by the poster
        REJECTED
    }

    public String getWorkerContactEmail() {
        return workerContactEmail;
    }

    // Setter for worker contact email
    public void setWorkerContactEmail(String workerContactEmail) {
        this.workerContactEmail = workerContactEmail;
    }

    // Getter for worker phone number
    public String getWorkerPhoneNumber() {
        return workerPhoneNumber;
    }

    // Setter for worker phone number
    public void setWorkerPhoneNumber(String workerPhoneNumber) {
        this.workerPhoneNumber = workerPhoneNumber;
    }
}