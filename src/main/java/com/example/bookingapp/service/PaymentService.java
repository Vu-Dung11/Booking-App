package com.example.bookingapp.service;


import com.example.bookingapp.dto.PaymentCallbackRequest;
import com.example.bookingapp.enm.ErrorCode;
import com.example.bookingapp.entity.Booking;
import com.example.bookingapp.entity.Payment;
import com.example.bookingapp.exception.AppException;
import com.example.bookingapp.repository.BookingRepository;
import com.example.bookingapp.repository.PaymentRepository;
import com.example.bookingapp.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;
    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;
    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;
    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;
    @Value("${vnpay.version}")
    private String vnp_Version;
    @Value("${vnpay.command}")
    private String vnp_Command;

    public String createVnPayUrl(Long bookingId, HttpServletRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // Các tham số bắt buộc của VNPay
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);

        // VNPay tính tiền bằng VNĐ x 100 (Ví dụ: 100,000 VNĐ -> 10000000)
        long amount = booking.getTotalPrice().longValue() * 100;
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        // Cố tình nhét bookingId vào vnp_TxnRef để lúc VNPay gọi về mình biết là đơn nào
        vnp_Params.put("vnp_TxnRef", bookingId + "_" + System.currentTimeMillis());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don dat phong: " + bookingId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        // Định dạng thời gian
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút (khớp với Redis)
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        // Build URL và Hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (i < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnp_PayUrl + "?" + queryUrl;
    }

    // HÀM 2: XỬ LÝ KHI VNPAY GỌI TRẢ VỀ
    @Transactional
    public String processVnPayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty()) && fieldName.startsWith("vnp_")) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Kiểm tra chữ ký xem có đúng VNPay gửi không (chống hacker)
        String signValue = VNPayUtil.hashAllFields(fields, vnp_HashSecret);
        if (!signValue.equals(vnp_SecureHash)) {
            return "CHỮ KÝ KHÔNG HỢP LỆ (INVALID SIGNATURE)";
        }

        // Lấy thông tin
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        // Tách lấy Booking ID từ chuỗi "BookingID_Time"
        Long bookingId = Long.parseLong(vnp_TxnRef.split("_")[0]);

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return "KHÔNG TÌM THẤY ĐƠN HÀNG";

        // "00" là mã VNPay quy định giao dịch thành công
        if ("00".equals(vnp_ResponseCode) && booking.getStatus() == Booking.BookingStatus.PENDING) {

            // Cập nhật trạng thái
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Lưu lịch sử
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(booking.getTotalPrice())
                    .transactionId(vnp_TxnRef)
                    .paymentMethod("VNPAY")
                    .status(Payment.PaymentStatus.SUCCESS)
                    .build();
            paymentRepository.save(payment);

            // Xóa key Redis chống hủy đơn
            redisTemplate.delete("booking:timeout:" + booking.getId());

            log.info("Giao dịch VNPay thành công cho Booking: {}", booking.getId());
            return "THANH TOÁN THÀNH CÔNG!";
        }

        return "THANH TOÁN THẤT BẠI HOẶC BỊ HỦY BỞI NGƯỜI DÙNG";
    }

    @Transactional
    public void processPaymentCallback(PaymentCallbackRequest request) {
        // 1. Tìm đơn hàng
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT));

        // 2. Kiểm tra xem đơn hàng có đang đợi thanh toán không
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            log.warn("Đơn hàng {} đã được xử lý hoặc đã hủy trước đó.", booking.getId());
            return;
        }

        // 3. Nếu thanh toán thành công (giả lập status "SUCCESS")
        if ("SUCCESS".equals(request.getStatus())) {

            // Cập nhật trạng thái Booking
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Tạo bản ghi Payment
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(request.getAmount())
                    .transactionId(request.getTransactionId())
                    .paymentMethod("VNPAY_MOCK")
                    .status(Payment.PaymentStatus.SUCCESS)
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            // 4. QUAN TRỌNG: Xóa Key timeout trong Redis để dừng việc tự động hủy đơn
            String redisKey = "booking:timeout:" + booking.getId();
            Boolean deleted = redisTemplate.delete(redisKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("Đã xóa key timeout Redis cho đơn hàng {}. Đơn hàng an toàn!", booking.getId());
            }

            log.info("Thanh toán thành công cho đơn hàng {}", booking.getId());
        } else {
            // Xử lý khi thanh toán thất bại (nếu cần)
            log.error("Thanh toán thất bại cho đơn hàng {}", booking.getId());
        }
    }
}