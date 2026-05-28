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

    // ===== ADMIN QUERIES =====

    @org.springframework.data.jpa.repository.Query("""
        SELECT r FROM Review r
        WHERE (:rating IS NULL OR r.rating = :rating)
          AND (:propertyId IS NULL OR r.property.id = :propertyId)
          AND (:guestId IS NULL OR r.booking.guest.id = :guestId)
          AND (:keyword IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY r.createdAt DESC
    """)
    Page<Review> searchForAdmin(
            @org.springframework.data.repository.query.Param("rating") Integer rating,
            @org.springframework.data.repository.query.Param("propertyId") Long propertyId,
            @org.springframework.data.repository.query.Param("guestId") Long guestId,
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            Pageable pageable);

    long countByBooking_Guest_Id(Long guestId);

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}