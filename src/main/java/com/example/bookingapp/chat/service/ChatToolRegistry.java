package com.example.bookingapp.chat.service;

import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Định nghĩa các function declarations gửi cho Gemini.
 * KHÔNG bao gồm userId trong tham số — backend tự lấy từ SecurityUtils
 * để tránh bot lộ dữ liệu user khác.
 */
@Component
public class ChatToolRegistry {

    public List<Tool> buildTools() {
        return List.of(Tool.builder()
                .functionDeclarations(List.of(
                        searchProperties(),
                        getPropertyDetail(),
                        getMyBookings(),
                        getBookingDetail(),
                        createBooking(),
                        getFAQ()))
                .build());
    }

    private FunctionDeclaration searchProperties() {
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("city", strSchema("Thành phố cần tìm, ví dụ: Đà Lạt, Hà Nội, Đà Nẵng"));
        props.put("checkIn", strSchema("Ngày nhận phòng, định dạng yyyy-MM-dd"));
        props.put("checkOut", strSchema("Ngày trả phòng, định dạng yyyy-MM-dd"));
        props.put("guests", intSchema("Số khách"));
        props.put("maxPrice", numSchema("Giá tối đa cho 1 đêm (VND), không bắt buộc"));

        return FunctionDeclaration.builder()
                .name("searchProperties")
                .description("Tìm các homestay phù hợp theo thành phố, ngày nhận/trả, số khách và giá tối đa (tuỳ chọn).")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(props)
                        .required(List.of("city", "checkIn", "checkOut", "guests"))
                        .build())
                .build();
    }

    private FunctionDeclaration getPropertyDetail() {
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("propertyId", intSchema("ID của homestay"));
        return FunctionDeclaration.builder()
                .name("getPropertyDetail")
                .description("Lấy thông tin chi tiết của một homestay (bao gồm danh sách phòng và roomId). "
                        + "propertyId PHẢI lấy từ kết quả searchProperties trước đó hoặc từ context "
                        + "[Bối cảnh: propertyId=X]. KHÔNG bao giờ hỏi user về propertyId.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(props)
                        .required(List.of("propertyId"))
                        .build())
                .build();
    }

    private FunctionDeclaration getMyBookings() {
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("status", strSchema("Trạng thái booking (tuỳ chọn): PENDING, CONFIRMED, CANCELLED, COMPLETED"));
        return FunctionDeclaration.builder()
                .name("getMyBookings")
                .description("Lấy danh sách booking của chính user đang đăng nhập. KHÔNG bao giờ trả về booking của user khác.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(props)
                        .build())
                .build();
    }

    private FunctionDeclaration getBookingDetail() {
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("bookingId", intSchema("ID của booking"));
        return FunctionDeclaration.builder()
                .name("getBookingDetail")
                .description("Lấy chi tiết một booking. bookingId PHẢI lấy từ kết quả getMyBookings trước đó. "
                        + "Chỉ truy được booking thuộc về user hiện tại.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(props)
                        .required(List.of("bookingId"))
                        .build())
                .build();
    }

    private FunctionDeclaration createBooking() {
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("roomId", intSchema("ID của phòng cần đặt"));
        props.put("checkIn", strSchema("Ngày nhận phòng, định dạng yyyy-MM-dd"));
        props.put("checkOut", strSchema("Ngày trả phòng, định dạng yyyy-MM-dd"));
        props.put("roomQuantity", intSchema("Số lượng phòng muốn đặt"));
        return FunctionDeclaration.builder()
                .name("createBooking")
                .description("Tạo booking mới cho user hiện tại. roomId PHẢI lấy từ kết quả getPropertyDetail "
                        + "trước đó — KHÔNG bao giờ hỏi user roomId. PHẢI xác nhận lại với user "
                        + "(tên phòng, ngày, số phòng, tổng tiền) bằng câu hỏi yes/no trước khi gọi.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(props)
                        .required(List.of("roomId", "checkIn", "checkOut", "roomQuantity"))
                        .build())
                .build();
    }

    private FunctionDeclaration getFAQ() {
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("topic", strSchema("Chủ đề: payment, cancel, checkin"));
        return FunctionDeclaration.builder()
                .name("getFAQ")
                .description("Lấy nội dung FAQ về thanh toán, huỷ phòng, check-in.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(props)
                        .required(List.of("topic"))
                        .build())
                .build();
    }

    private Schema strSchema(String desc) {
        return Schema.builder().type(Type.Known.STRING).description(desc).build();
    }

    private Schema intSchema(String desc) {
        return Schema.builder().type(Type.Known.INTEGER).description(desc).build();
    }

    private Schema numSchema(String desc) {
        return Schema.builder().type(Type.Known.NUMBER).description(desc).build();
    }
}
