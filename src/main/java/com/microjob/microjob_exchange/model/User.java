package com.microjob.microjob_exchange.model;

//used for enabling to use annotation @
import jakarta.persistence.*;

//used for creating shortcuts in boiler plate
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//used for generating date and time
import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {
    @Id//column type
    @GeneratedValue(strategy = GenerationType.IDENTITY)//for auto increment
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public Long getId() {
        return id;
    }
}
