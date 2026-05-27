# Chat API — Postman Test Guide

## Prerequisites

1. Set environment variable trước khi chạy app:
   ```
   GEMINI_API_KEY=<your-key>
   ```
2. Đảm bảo 2 bảng `chat_sessions` và `chat_messages` đã có trong MySQL.
3. Login bằng tài khoản GUEST/ADMIN/HOST để lấy JWT token:
   - POST `http://localhost:8080/api/v1/auth/login`
   - Body: `{ "email": "...", "password": "..." }`
   - Lấy `data.token` từ response.

Tất cả request bên dưới thêm header:
```
Authorization: Bearer <token>
Content-Type: application/json
```

---

## 1. Tạo session mới

**POST** `http://localhost:8080/api/v1/chat/session`

Response:
```json
{
  "code": 0,
  "message": "Success",
  "data": { "sessionId": "uuid-...." }
}
```

→ Lưu lại `sessionId` cho các bước sau.

---

## 2. Gửi tin nhắn

**POST** `http://localhost:8080/api/v1/chat/message`

Body:
```json
{
  "sessionId": "<sessionId>",
  "message": "Tìm cho mình homestay ở Đà Lạt cho 2 khách, nhận phòng 2026-06-01 trả phòng 2026-06-03"
}
```

Response:
```json
{
  "code": 0,
  "message": "Success",
  "data": {
    "reply": "Mình đã tìm được 3 homestay phù hợp tại Đà Lạt ...",
    "suggestions": ["Xem chi tiết", "Đặt phòng luôn", "Tìm chỗ khác"],
    "cards": [
      {
        "propertyId": 1,
        "name": "Homestay Sunny Hill",
        "thumbnailUrl": "https://...",
        "city": "Đà Lạt",
        "minPrice": 850000
      }
    ]
  }
}
```

### Một số scenario test:

- "Cho mình xem chi tiết homestay id 1" → tool `getPropertyDetail`
- "Tôi có đơn đặt phòng nào đang pending không?" → tool `getMyBookings`
- "Cho xem booking id 5 của tôi" → tool `getBookingDetail`
- "Tôi muốn đặt 1 phòng id 3 từ 2026-06-10 đến 2026-06-12" → bot xác nhận → "Ừ đúng rồi" → tool `createBooking`
- "Chính sách huỷ phòng thế nào?" → tool `getFAQ` (topic: cancel)

### Context-aware (mở chat từ màn property detail):
```json
{
  "sessionId": "<sessionId>",
  "message": "Phòng nào rẻ nhất ở đây?",
  "currentPropertyId": 5
}
```

---

## 3. Lấy lịch sử tin nhắn

**GET** `http://localhost:8080/api/v1/chat/history?sessionId=<sessionId>`

Response:
```json
{
  "code": 0,
  "message": "Success",
  "data": [
    { "id": 1, "role": "USER", "content": "...", "createdAt": "..." },
    { "id": 2, "role": "TOOL", "content": "{...}", "toolName": "searchProperties", ... },
    { "id": 3, "role": "ASSISTANT", "content": "...", "createdAt": "..." }
  ]
}
```

---

## 4. Lấy danh sách session của user

**GET** `http://localhost:8080/api/v1/chat/sessions`

Response:
```json
{
  "code": 0,
  "message": "Success",
  "data": [
    {
      "id": "uuid-...",
      "title": "Tìm cho mình homestay ở Đà Lạt...",
      "createdAt": "...",
      "lastActiveAt": "..."
    }
  ]
}
```

---

## Error cases

| Status | Code | Message |
|--------|------|---------|
| 401 | — | Thiếu/sai JWT |
| 400 | 512 | sessionId không tồn tại |
| 400 | 401 | sessionId thuộc về user khác (UNAUTHORIZED) |
| 500 | 513 | Lỗi gọi Gemini (sai API key, network, v.v.) |
| 400 | 514 | Lỗi thực thi function call |
