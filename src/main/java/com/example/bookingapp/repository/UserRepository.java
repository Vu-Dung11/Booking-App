package com.example.bookingapp.repository;


import com.example.bookingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long>{
    // ADMIN, EMPLOYEE, MÂNGER
    // A-1
    // A-2
    // E-1
    // M-1
//
//    long countByRole(User.Role role);
//    User findByUsernameOrEmail(String username, String email);
//
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);




}
