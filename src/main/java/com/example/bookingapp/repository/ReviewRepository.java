package com.example.bookingapp.repository;


import com.example.bookingapp.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBookingId(Long bookingId);

    Page<Review> findByProperty_HostId(Long hostId, Pageable pageable);
    long countByProperty_HostId(Long hostId);

    Page<Review> findByPropertyIdOrderByCreatedAtDesc(Long propertyId, Pageable pageable);

    java.util.List<Review> findByPropertyId(Long propertyId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.property.host.id = :hostId"
    )
    Double averageRatingByHostId(@org.springframework.data.repository.query.Param("hostId") Long hostId);
}