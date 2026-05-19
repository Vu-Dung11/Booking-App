package com.example.bookingapp.repository;

import com.example.bookingapp.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByHostId(Long hostId);

    Page<Property> findByHostId(Long hostId, Pageable pageable);

    Optional<Property> findByIdAndHostId(Long id, Long hostId);

    long countByHostId(Long hostId);

    long countByHostIdAndIsActive(Long hostId, Boolean isActive);

    @Query(value = """
        SELECT DISTINCT p.* FROM properties p
        JOIN rooms r ON p.id = r.property_id
        WHERE p.city = :city 
        AND p.is_active = true
        AND r.capacity >= :guests
        AND r.id IN (
            -- Lấy các phòng có đủ số ngày trống liên tiếp và số lượng > 0
            SELECT ri.room_id FROM room_inventory ri
            WHERE ri.inventory_date >= :checkIn AND ri.inventory_date < :checkOut
            AND ri.available_count > 0
            GROUP BY ri.room_id
            HAVING COUNT(ri.id) = :duration
        )
    """, nativeQuery = true)
    List<Property> searchAvailableProperties(
            @Param("city") String city,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") int guests,
            @Param("duration") long duration);

    @Query("SELECT DISTINCT p.city FROM Property p WHERE p.isActive = true ORDER BY p.city")
    List<String> findDistinctActiveCities();

    @Query("""
        SELECT new com.example.bookingapp.dto.PropertySummaryResponse(
            p.id, p.name, p.address, p.city,
            MIN(pi.imageUrl),
            MIN(r.basePrice),
            AVG(rv.rating),
            COUNT(DISTINCT rv.id))
        FROM Property p
        LEFT JOIN Room r ON r.property = p
        LEFT JOIN Review rv ON rv.property = p
        LEFT JOIN PropertyImage pi ON pi.property = p AND pi.isThumbnail = true
        WHERE p.isActive = true
        GROUP BY p.id, p.name, p.address, p.city
        """)
    Page<com.example.bookingapp.dto.PropertySummaryResponse> findAllSummaries(Pageable pageable);
}
