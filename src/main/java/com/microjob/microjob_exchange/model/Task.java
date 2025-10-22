package com.microjob.microjob_exchange.model;

import java.math.BigDecimal; // Import for price
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microjob.microjob_exchange.model.User; // <-- Add this import
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {

    // 1. Change ID type to Long
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- Added fields for Geo-location and Price ---
    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "duration_minutes", nullable = true)
    private Integer durationMinutes; // Time constraint (e.g., 30)
    // -----------------------------------------------

    @Column(name = "poster_contact_email", nullable = true) // <-- NEW FIELD
    private String posterContactEmail;

    @Column(name = "poster_phone", nullable = true, length = 15) // <-- NEW FIELD
    private String posterPhoneNumber;

    // 2. Change User ID types to Long
    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY is generally better for performance
    @JoinColumn(name = "uploader_id", nullable = false)
    private User poster;

    // 3. Updated Status Enum (initial status changed)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN; // Changed from PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptor_id", nullable = true) // nullable=true since it starts null/unassigned
    private User acceptor;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Application> applications;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 4. Revised Status Enum for better lifecycle tracking
    public enum Status {
        OPEN,       // Task is listed and available to apply for
        ASSIGNED,   // Task has been accepted by a worker
        COMPLETED,  // Worker claims completion, awaiting poster approval
        PAID        // Task is finished, payment processed, and commission taken
    }

    // Default constructor
    public Task() {
    }

    // NOTE: Update constructors and all getters/setters to use Long for IDs/user IDs
    // and include the new fields (price, latitude, longitude, durationMinutes).

    // Getters and setters... (Ensure you update the types in these methods!)

    public Long getId() {
        return id;
    }

    // ... all other getters/setters (not shown for brevity)

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public User getPoster() { // <--- FIX 2: Correct getter for the User object
        return poster;
    }

    public void setPoster(User poster) { // <--- Don't forget the setter
        this.poster = poster;
    }
    public Status getStatus() {
        return status;
    }

    // Set Status
    public void setStatus(Status status) {
        this.status = status;
    }

    // Get Title
    public String getTitle() {
        return title;
    }

    // Set Title
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Setter for longitude
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // Getter for longitude
    public Double getLongitude() {
        return longitude;
    }

    // Setter for durationMinutes
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    // Getter for durationMinutes
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    public User getAcceptor() {
        return acceptor;
    }

    // Setter for Acceptor
    public void setAcceptor(User acceptor) {
        this.acceptor = acceptor;
    }

    public String getPosterContactEmail() {
        return posterContactEmail;
    }

    // Setter for the contact email
    public void setPosterContactEmail(String posterContactEmail) {
        this.posterContactEmail = posterContactEmail;
    }

    // Getter for the phone number
    public String getPosterPhoneNumber() {
        return posterPhoneNumber;
    }

    // Setter for the phone number
    public void setPosterPhoneNumber(String posterPhoneNumber) {
        this.posterPhoneNumber = posterPhoneNumber;
    }

}