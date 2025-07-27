package com.microjob.microjob_exchange.service;

import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.repository.UserRepository;

//@Autowired – Injects dependencies (UserRepository, JwtUtil).
//BCryptPasswordEncoder – A secure password hashing algorithm (prevents storing plain-text passwords).
//@Service – Marks this class as a service layer (business logic).
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    //signup
    public User signup(String name,String email,String password,String role){
        if(password.length()<8)
        {
            throw new IllegalArgumentException("Password must be atleast 8 character");
        }

        if(userRepository.findByEmail(email).isPresent())
        {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user=new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    //login
    public String login(String email,String password)
    {
        Optional<User> userOptional=userRepository.findByEmail(email);
        if(userOptional.isEmpty())
        {
            throw new IllegalArgumentException("invalid email or password");
        }
        User user=userOptional.get();
        if(!passwordEncoder.matches(password,user.getPassword()))
        {
            throw new IllegalArgumentException(("Incorrect email or password"));
        }

        //generate JWT
        return jwtUtil.generateToken(user.getId(),user.getEmail(),user.getRole());
    }
    //get current user
    public User getCurrentUser(String token)
    {
        String email=jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found"));
    }
}
