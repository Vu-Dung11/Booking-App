package com.example.bookingapp.controller;

import com.example.bookingapp.dto.ApiResponse;
import com.example.bookingapp.dto.PropertyDetailResponse;
import com.example.bookingapp.dto.PropertySearchResponse;
import com.example.bookingapp.entity.Property;
import com.example.bookingapp.entity.Room;
import com.example.bookingapp.form.PropertyRequest;
import com.example.bookingapp.form.RoomRequest;
import com.example.bookingapp.form.SearchRequest;
import com.example.bookingapp.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('HOST')")
    public ApiResponse<Property> create(@RequestBody PropertyRequest request) {
        Property property = propertyService.createProperty(request);
        return ApiResponse.success(property);
    }

    @PostMapping("/{propertyId}/rooms")
    @PreAuthorize("hasRole('HOST')")
    public ApiResponse<Room> addRoom(
            @Valid @RequestBody RoomRequest roomRequest,
            @PathVariable("propertyId") Long id) {
        Room room = propertyService.addRoomToProperty(id, roomRequest);
        return ApiResponse.success(room);
    }
    @GetMapping("/search")
    public ApiResponse<List<PropertySearchResponse>> search(@Valid @ModelAttribute SearchRequest request) {
        // Dùng @ModelAttribute vì đây là request GET, các tham số nằm trên URL (query params)
        List<PropertySearchResponse> results = propertyService.searchProperties(request);
        return ApiResponse.success(results);
    }

    @GetMapping
    public ApiResponse<Page<Property>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(propertyService.getAllProperties(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<Property> getPropertyById(@PathVariable Long id) {
        return ApiResponse.success(propertyService.getPropertyById(id));
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<PropertyDetailResponse> getPropertyDetail(@PathVariable Long id) {
        return ApiResponse.success(propertyService.getPropertyDetail(id));
    }

}
