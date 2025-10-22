// src/main/java/com/microjob/microjob_exchange/controller/AdminController.java

package com.microjob.microjob_exchange.controller;

import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/admin/users
     * Retrieves the list of all registered users.
     * This endpoint is secured in SecurityConfig to only allow ROLE_ADMIN.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}