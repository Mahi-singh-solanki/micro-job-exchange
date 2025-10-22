// src/main/java/com/microjob/microjob_exchange/service/UserService.java

package com.microjob.microjob_exchange.service;

import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all user profiles in the system.
     * Accessible only by Admin.
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}