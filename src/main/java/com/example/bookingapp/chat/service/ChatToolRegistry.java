package com.example.bookingapp.chat.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Định nghĩa tools gửi cho Ollama theo format OpenAI-compatible.
 * KHÔNG bao gồm userId trong tham số — backend tự lấy từ SecurityUtils
 * để tránh lộ dữ liệu user khác.
 */
@Component
public class ChatToolRegistry {

    public List<Map<String, Object>> buildTools() {
        return List.of(
                buildTool("searchProperties",
                        "Tìm các homestay phù hợp theo thành phố, ngày nhận/trả, số khách và giá tối đa (tuỳ chọn).",
                        props(
                                "city", strParam("Thành phố cần tìm, ví dụ: Đà Lạt, Hà Nội, Đà Nẵng"),
                                "checkIn", strParam("Ngày nhận phòng, định dạng yyyy-MM-dd"),
                                "checkOut", strParam("Ngày trả phòng, định dạng yyyy-MM-dd"),
                                "guests", intParam("Số khách"),
                                "maxPrice", numParam("Giá tối đa cho 1 đêm (VND), không bắt buộc")
                        ),
                        List.of("city", "checkIn", "checkOut", "guests")),

                buildTool("getPropertyDetail",
                        "Lấy thông tin chi tiết của một homestay (bao gồm danh sách phòng và roomId). "
                                + "propertyId PHẢI lấy từ kết quả searchProperties trước đó hoặc từ context "
                                + "[Bối cảnh: propertyId=X]. KHÔNG bao giờ hỏi user về propertyId.",
                        props("propertyId", intParam("ID của homestay")),
                        List.of("propertyId")),

                buildTool("getMyBookings",
                        "Lấy danh sách booking của chính user đang đăng nhập. KHÔNG bao giờ trả về booking của user khác.",
                        props("status", strParam("Trạng thái booking (tuỳ chọn): PENDING, CONFIRMED, CANCELLED, COMPLETED")),
                        List.of()),

                buildTool("getBookingDetail",
                        "Lấy chi tiết một booking. bookingId PHẢI lấy từ kết quả getMyBookings trước đó. "
                                + "Chỉ truy được booking thuộc về user hiện tại.",
                        props("bookingId", intParam("ID của booking")),
                        List.of("bookingId")),

                buildTool("createBooking",
                        "Tạo booking mới cho user hiện tại. roomId PHẢI lấy từ kết quả getPropertyDetail "
                                + "trước đó — KHÔNG bao giờ hỏi user roomId. PHẢI xác nhận lại với user "
                                + "(tên phòng, ngày, số phòng, tổng tiền) bằng câu hỏi yes/no trước khi gọi.",
                        props(
                                "roomId", intParam("ID của phòng cần đặt"),
                                "checkIn", strParam("Ngày nhận phòng, định dạng yyyy-MM-dd"),
                                "checkOut", strParam("Ngày trả phòng, định dạng yyyy-MM-dd"),
                                "roomQuantity", intParam("Số lượng phòng muốn đặt")
                        ),
                        List.of("roomId", "checkIn", "checkOut", "roomQuantity")),

                buildTool("getFAQ",
                        "Lấy nội dung FAQ về thanh toán, huỷ phòng, check-in.",
                        props("topic", strParam("Chủ đề: payment, cancel, checkin")),
                        List.of("topic"))
        );
    }

    private Map<String, Object> buildTool(String name, String description,
                                           Map<String, Object> properties,
                                           List<String> required) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "object");
        params.put("properties", properties);
        if (!required.isEmpty()) {
            params.put("required", required);
        }

        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", name,
                        "description", description,
                        "parameters", params
                )
        );
    }

    private Map<String, Object> props(Object... entries) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            m.put((String) entries[i], entries[i + 1]);
        }
        return m;
    }

    private Map<String, Object> strParam(String desc) {
        return Map.of("type", "string", "description", desc);
    }

    private Map<String, Object> intParam(String desc) {
        return Map.of("type", "integer", "description", desc);
    }

    private Map<String, Object> numParam(String desc) {
        return Map.of("type", "number", "description", desc);
    }
}
