package com.example.bookingapp.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
    @Table(name = "room_inventory", uniqueConstraints = {
            @UniqueConstraint(columnNames = {"room_id", "inventory_date"})
    })
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // lien ket voi bang Rooms
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "inventory_date", nullable = false)
    private LocalDate inventoryDate;

    @Column(name = "available_count", nullable = false)
    private Integer availableCount;





}
