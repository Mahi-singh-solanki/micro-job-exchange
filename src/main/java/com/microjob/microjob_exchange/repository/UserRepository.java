package com.microjob.microjob_exchange.repository;

import com.microjob.microjob_exchange.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.classfile.Interfaces;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{

    //to find user by email
    Optional<User> findByEmail(String email);
}
