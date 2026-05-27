package com.example.bookingapp.chat.service;

import com.example.bookingapp.chat.dto.PropertyCard;
import com.example.bookingapp.configuration.enm.ErrorCode;
import com.example.bookingapp.configuration.exception.AppException;
import com.example.bookingapp.dto.PropertyDetailResponse;
import com.example.bookingapp.dto.PropertySearchResponse;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.form.BookingRequest;
import com.example.bookingapp.form.SearchRequest;
import com.example.bookingapp.service.BookingService;
import com.example.bookingapp.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map functionCall (name + args) → service nội bộ và trả về Map kết quả.
 * SECURITY: tools liên quan tới user data đều lấy userId từ SecurityContext
 * thông qua BookingService/PropertyService (KHÔNG nhận userId từ args).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatToolExecutor {

    private final PropertyService propertyService;
    private final BookingService bookingService;

    /** Kết quả thực thi: map sẽ được Gemini đọc, cards dùng để render UI. */
    public ToolResult execute(String name, Map<String, Object> args) {
        try {
            return switch (name) {
                case "searchProperties" -> searchProperties(args);
                case "getPropertyDetail" -> getPropertyDetail(args);
                case "getMyBookings" -> getMyBookings(args);
                case "getBookingDetail" -> getBookingDetail(args);
                case "createBooking" -> createBooking(args);
                case "getFAQ" -> getFAQ(args);
                default -> ToolResult.of(Map.of("error", "Unknown function: " + name));
            };
        } catch (AppException e) {
            return ToolResult.of(Map.of(
                    "error", e.getErrorCode().getMessage(),
                    "code", e.getErrorCode().getCode()));
        } catch (Exception e) {
            log.error("Tool {} execution failed: {}", name, e.getMessage(), e);
            return ToolResult.of(Map.of("error", e.getMessage() == null ? "unknown" : e.getMessage()));
        }
    }

    // ============================================================

    private ToolResult searchProperties(Map<String, Object> args) {
        SearchRequest req = new SearchRequest();
        req.setCity(asString(args.get("city")));
        req.setCheckIn(asDate(args.get("checkIn")));
        req.setCheckOut(asDate(args.get("checkOut")));
        req.setGuests(asInt(args.get("guests")));

        List<PropertySearchResponse> results = propertyService.searchProperties(req);

        BigDecimal maxPrice = asBigDecimal(args.get("maxPrice"));
        if (maxPrice != null) {
            results = results.stream()
                    .filter(r -> r.getMinPrice() != null && r.getMinPrice().compareTo(maxPrice) <= 0)
                    .toList();
        }

        List<Map<String, Object>> brief = new ArrayList<>();
        List<PropertyCard> cards = new ArrayList<>();
        for (PropertySearchResponse r : results.stream().limit(5).toList()) {
            brief.add(Map.of(
                    "propertyId", r.getPropertyId(),
                    "name", nullSafe(r.getPropertyName()),
                    "city", nullSafe(r.getCity()),
                    "address", nullSafe(r.getAddress()),
                    "minPrice", r.getMinPrice() == null ? 0 : r.getMinPrice()));
            cards.add(PropertyCard.builder()
                    .propertyId(r.getPropertyId())
                    .name(r.getPropertyName())
                    .city(r.getCity())
                    .thumbnailUrl(r.getThumbnailUrl())
                    .minPrice(r.getMinPrice())
                    .build());
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", brief.size());
        out.put("properties", brief);
        return new ToolResult(out, cards);
    }

    private ToolResult getPropertyDetail(Map<String, Object> args) {
        Long id = asLong(args.get("propertyId"));
        PropertyDetailResponse d = propertyService.getPropertyDetail(id);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("propertyId", d.getPropetyId());
        out.put("name", d.getName());
        out.put("description", nullSafe(d.getDescription()));
        out.put("address", nullSafe(d.getAddress()));
        out.put("city", nullSafe(d.getCity()));
        out.put("country", nullSafe(d.getCountry()));
        out.put("rooms", d.getRooms() == null ? List.of() : d.getRooms().stream().map(r -> Map.of(
                "roomId", r.getRoomId(),
                "roomType", nullSafe(r.getRoomType()),
                "capacity", r.getCapacity() == null ? 0 : r.getCapacity(),
                "price", r.getPrice() == null ? 0 : r.getPrice(),
                "quantity", r.getQuantity() == null ? 0 : r.getQuantity())).toList());
        return ToolResult.of(out);
    }

    private ToolResult getMyBookings(Map<String, Object> args) {
        Booking.BookingStatus status = null;
        String s = asString(args.get("status"));
        if (s != null && !s.isBlank()) {
            try {
                status = Booking.BookingStatus.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // ignore — treat as null filter
            }
        }
        Page<Booking> page = bookingService.getBookingsForCurrentGuest(
                status, PageRequest.of(0, 10));

        List<Map<String, Object>> brief = page.getContent().stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("bookingId", b.getId());
            m.put("propertyName", b.getRoom() != null && b.getRoom().getProperty() != null
                    ? b.getRoom().getProperty().getName() : null);
            m.put("roomType", b.getRoom() != null ? b.getRoom().getRoomType() : null);
            m.put("checkIn", String.valueOf(b.getCheckInDate()));
            m.put("checkOut", String.valueOf(b.getCheckOutDate()));
            m.put("totalPrice", b.getTotalPrice());
            m.put("status", b.getStatus() == null ? null : b.getStatus().name());
            return m;
        }).toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("count", brief.size());
        out.put("bookings", brief);
        return ToolResult.of(out);
    }

    private ToolResult getBookingDetail(Map<String, Object> args) {
        Long id = asLong(args.get("bookingId"));
        Booking b = bookingService.getBookingForCurrentGuest(id);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("bookingId", b.getId());
        out.put("checkIn", String.valueOf(b.getCheckInDate()));
        out.put("checkOut", String.valueOf(b.getCheckOutDate()));
        out.put("totalPrice", b.getTotalPrice());
        out.put("roomQuantity", b.getRoomQuantity());
        out.put("status", b.getStatus() == null ? null : b.getStatus().name());
        if (b.getRoom() != null) {
            out.put("roomType", b.getRoom().getRoomType());
            if (b.getRoom().getProperty() != null) {
                out.put("propertyId", b.getRoom().getProperty().getId());
                out.put("propertyName", b.getRoom().getProperty().getName());
                out.put("address", b.getRoom().getProperty().getAddress());
                out.put("city", b.getRoom().getProperty().getCity());
            }
        }
        return ToolResult.of(out);
    }

    private ToolResult createBooking(Map<String, Object> args) {
        BookingRequest req = new BookingRequest();
        req.setRoomId(asLong(args.get("roomId")));
        req.setCheckIn(asDate(args.get("checkIn")));
        req.setCheckOut(asDate(args.get("checkOut")));
        req.setRoomQuantity(asInt(args.get("roomQuantity")));

        if (req.getRoomId() == null || req.getCheckIn() == null
                || req.getCheckOut() == null || req.getRoomQuantity() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Booking b = bookingService.createBooking(req);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("bookingId", b.getId());
        out.put("status", b.getStatus() == null ? null : b.getStatus().name());
        out.put("totalPrice", b.getTotalPrice());
        out.put("checkIn", String.valueOf(b.getCheckInDate()));
        out.put("checkOut", String.valueOf(b.getCheckOutDate()));
        out.put("message", "Đặt phòng thành công, đơn ở trạng thái chờ thanh toán (PENDING). Bạn có 15 phút để thanh toán.");
        return ToolResult.of(out);
    }

    private ToolResult getFAQ(Map<String, Object> args) {
        String topic = asString(args.get("topic"));
        String answer = switch (topic == null ? "" : topic.toLowerCase()) {
            case "payment" -> "Hỗ trợ thanh toán qua VNPay. Sau khi đặt phòng, bạn có 15 phút để hoàn tất thanh toán, nếu không đơn sẽ tự huỷ.";
            case "cancel" -> "Đơn PENDING có thể huỷ trực tiếp trong lịch sử đặt phòng. Đơn CONFIRMED vui lòng liên hệ chủ homestay để được hỗ trợ.";
            case "checkin" -> "Thời gian nhận phòng từ 14:00 ngày check-in. Vui lòng xuất trình mã đặt phòng và CCCD khi đến nhận phòng.";
            default -> "Chủ đề chưa có FAQ. Bạn có thể hỏi về: payment, cancel, checkin.";
        };
        return ToolResult.of(Map.of("topic", topic == null ? "" : topic, "answer", answer));
    }

    // ============================================================
    // helpers

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal asBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(o.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static LocalDate asDate(Object o) {
        if (o == null) return null;
        try { return LocalDate.parse(o.toString()); } catch (Exception e) { return null; }
    }

    private static Object nullSafe(Object v) {
        return v == null ? "" : v;
    }

    /** Kết quả tool: response cho Gemini + cards optional cho UI. */
    public record ToolResult(Map<String, Object> response, List<PropertyCard> cards) {
        public static ToolResult of(Map<String, Object> response) {
            return new ToolResult(response, List.of());
        }
    }
}
