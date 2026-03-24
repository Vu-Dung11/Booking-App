package com.example.bookingapp.repository;


import com.example.bookingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    org.springframework.data.domain.Page<User> findByRole(User.Role role, org.springframework.data.domain.Pageable pageable);

}
