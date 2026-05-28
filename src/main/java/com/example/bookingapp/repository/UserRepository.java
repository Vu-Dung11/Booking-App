package com.example.bookingapp.repository;


import com.example.bookingapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(User.Role role, Pageable pageable);

    // ===== ADMIN QUERIES =====

    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR u.role = :role)
          AND (:isActive IS NULL OR u.isActive = :isActive)
          AND (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY u.id DESC
    """)
    Page<User> searchForAdmin(@Param("role") User.Role role,
                              @Param("isActive") Boolean isActive,
                              @Param("keyword") String keyword,
                              Pageable pageable);

    long countByRole(User.Role role);

    long countByIsActive(Boolean isActive);

    @Query("SELECT u FROM User u ORDER BY u.id DESC")
    List<User> findRecent(Pageable pageable);
}
