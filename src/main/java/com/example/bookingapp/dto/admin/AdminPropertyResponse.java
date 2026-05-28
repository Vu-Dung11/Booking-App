package com.example.bookingapp.dto.admin;

import com.example.bookingapp.entity.Property;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPropertyResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Boolean isActive;

    // Host snapshot
    private Long hostId;
    private String hostEmail;
    private String hostFullName;

    // Optional aggregates - set bởi service
    private String thumbnailUrl;
    private Double averageRating;
    private Long reviewCount;
    private Long roomCount;

    public static AdminPropertyResponse fromEntity(Property p) {
        if (p == null) return null;
        return AdminPropertyResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .address(p.getAddress())
                .city(p.getCity())
                .country(p.getCountry())
                .isActive(p.getIsActive())
                .hostId(p.getHost() != null ? p.getHost().getId() : null)
                .hostEmail(p.getHost() != null ? p.getHost().getEmail() : null)
                .hostFullName(p.getHost() != null ? p.getHost().getFullName() : null)
                .thumbnailUrl(p.getThumbnailUrl())
                .build();
    }
}
