package com.example.bookingapp.repository;

import com.example.bookingapp.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByStatusAndCreatedAtBefore(Booking.BookingStatus status, LocalDateTime timeout);
    // Tìm các đơn hàng đã xác nhận nhưng ngày Check-out đã qua (hoặc bằng ngày hiện tại)
    List<Booking> findAllByStatusAndCheckOutDateLessThanEqual(Booking.BookingStatus status, LocalDate date);
    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);

    // Có booking nào trên room này không (dùng khi xoá room)
    boolean existsByRoom_Id(Long roomId);

    /**
     * Lấy các booking active overlap với khoảng [start, endExclusive). Dùng để
     * tính bookedCount cho từng ngày trong calendar view.
     * Logic overlap: checkInDate < endExclusive AND checkOutDate > start.
     */
    @org.springframework.data.jpa.repository.Query("""
        SELECT b FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status IN (
              com.example.bookingapp.entity.Booking.BookingStatus.PENDING,
              com.example.bookingapp.entity.Booking.BookingStatus.CONFIRMED,
              com.example.bookingapp.entity.Booking.BookingStatus.COMPLETED
          )
          AND b.checkInDate < :endExclusive
          AND b.checkOutDate > :start
    """)
    List<Booking> findActiveOverlapping(
            @org.springframework.data.repository.query.Param("roomId") Long roomId,
            @org.springframework.data.repository.query.Param("start") LocalDate start,
            @org.springframework.data.repository.query.Param("endExclusive") LocalDate endExclusive
    );

    // Booking của guest (current user)
    Page<Booking> findByGuest_Id(Long guestId, Pageable pageable);
    Page<Booking> findByGuest_IdAndStatus(Long guestId, Booking.BookingStatus status, Pageable pageable);

    // Booking thuộc các property của host hiện tại
    Page<Booking> findByRoom_Property_HostId(Long hostId, Pageable pageable);
    Page<Booking> findByRoom_Property_HostIdAndStatus(Long hostId, Booking.BookingStatus status, Pageable pageable);
    long countByRoom_Property_HostId(Long hostId);
    long countByRoom_Property_HostIdAndStatus(Long hostId, Booking.BookingStatus status);

    @org.springframework.data.jpa.repository.Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.room.property.host.id = :hostId
          AND b.status IN (com.example.bookingapp.entity.Booking.BookingStatus.CONFIRMED,
                           com.example.bookingapp.entity.Booking.BookingStatus.COMPLETED)
    """)
    java.math.BigDecimal sumRevenueByHostId(@org.springframework.data.repository.query.Param("hostId") Long hostId);

    // ===== ADMIN QUERIES =====

    long countByGuest_Id(Long guestId);

    long countByStatus(Booking.BookingStatus status);

    @org.springframework.data.jpa.repository.Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.status IN (com.example.bookingapp.entity.Booking.BookingStatus.CONFIRMED,
                           com.example.bookingapp.entity.Booking.BookingStatus.COMPLETED)
    """)
    java.math.BigDecimal sumTotalRevenue();

    Page<Booking> findByRoom_Property_Id(Long propertyId, Pageable pageable);

    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
