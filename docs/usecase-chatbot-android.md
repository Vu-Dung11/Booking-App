| **Mã Use Case** | UC04 |
|-----------------|------|
| **Tên Use Case** | Tương tác với trợ lý ảo (Chatbot) |
| **Tác nhân** | Người dùng |
| **Mô tả** | Cho phép người dùng trò chuyện bằng ngôn ngữ tự nhiên (tiếng Việt) với trợ lý ảo để tìm homestay, xem chi tiết homestay đang quan tâm, tra cứu và đặt phòng, cũng như hỏi các chính sách (thanh toán, hủy phòng, nhận phòng). Trợ lý ảo phản hồi dựa trên nội dung trò chuyện và dữ liệu thực tế của hệ thống. |
| **Sự kiện kích hoạt chức năng** | Người dùng chọn biểu tượng trợ lý ảo hiển thị ở góc dưới bên phải màn hình chính của ứng dụng. |
| **Tiền điều kiện** | - Người dùng đã đăng nhập thành công vào ứng dụng.<br>- Thiết bị có kết nối Internet.<br>- Hệ thống đang hoạt động bình thường. |

## Luồng sự kiện chính

| # | Thực hiện bởi | Hành động |
|---|---------------|-----------|
| 1 | Người dùng | Tại màn hình trang chủ của ứng dụng, người dùng chọn vào biểu tượng trợ lý ảo treo ở góc dưới bên phải màn hình. |
| 2 | Hệ thống | Hệ thống mở cửa sổ trò chuyện. Nếu người dùng đã từng trò chuyện trước đó, hệ thống tải và hiển thị lại lịch sử cuộc trò chuyện. Nếu là lần đầu, hệ thống tạo một cuộc trò chuyện mới và hiển thị lời chào cùng một số gợi ý mẫu. |
| 3 | Người dùng | Người dùng nhập nội dung cần hỏi (ví dụ: tìm homestay theo địa điểm, ngày ở, số khách) hoặc chọn một gợi ý có sẵn rồi gửi. |
| 4 | Hệ thống | Hệ thống tiếp nhận nội dung, hiển thị ngay tin nhắn của người dùng và báo hiệu trợ lý đang soạn trả lời. |
| 5 | Hệ thống | Trợ lý ảo phân tích yêu cầu, truy xuất dữ liệu liên quan trong hệ thống (danh sách homestay, chi tiết homestay, đơn đặt phòng, chính sách...) để chuẩn bị câu trả lời. |
| 6 | Hệ thống | Hệ thống hiển thị câu trả lời lên màn hình. Khi phù hợp, kèm theo các thẻ homestay (gồm hình ảnh, tên, địa điểm, giá tham khảo) và các gợi ý câu hỏi tiếp theo. |
| 7 | Người dùng | Người dùng tiếp tục trò chuyện, chọn vào một thẻ homestay để xem chi tiết, hoặc kết thúc cuộc trò chuyện. |

## Luồng sự kiện rẽ nhánh

| # | Thực hiện bởi | Hành động |
|---|---------------|-----------|
| 3a | Người dùng | (Nhánh hỏi về homestay đang xem) Người dùng mở trợ lý ảo từ màn hình chi tiết một homestay và đặt câu hỏi về chính homestay đó. |
| 3a.1 | Hệ thống | Hệ thống tự động hiểu người dùng đang quan tâm homestay nào, trả lời trực tiếp mà không cần người dùng nói lại tên homestay. |
| 3b | Người dùng | (Nhánh tra cứu đơn đặt phòng) Người dùng hỏi về các đơn đặt phòng của mình. |
| 3b.1 | Hệ thống | Hệ thống chỉ trả về các đơn đặt phòng thuộc về chính người dùng đang đăng nhập, không hiển thị dữ liệu của người khác. |
| 3c | Người dùng | (Nhánh đặt phòng qua trò chuyện) Người dùng yêu cầu đặt một phòng. |
| 3c.1 | Hệ thống | Trợ lý ảo xác nhận lại thông tin đặt phòng (homestay, loại phòng, ngày nhận – trả, số lượng phòng) với người dùng trước khi tạo đơn. |
| 3c.2 | Hệ thống | Sau khi người dùng xác nhận, hệ thống tạo đơn đặt phòng ở trạng thái chờ thanh toán và thông báo cho người dùng kèm thời hạn thanh toán. |
| 5a | Hệ thống | Nếu trợ lý ảo tạm thời không phản hồi được, hệ thống hiển thị thông báo lỗi và cho phép người dùng thử gửi lại. |
| * | Hệ thống | Tại bất kỳ thời điểm nào trong quá trình thực hiện Use Case, nếu mất kết nối hoặc không truy xuất được dữ liệu, hệ thống sẽ hiển thị thông báo "Lỗi kết nối". |

| **Hậu điều kiện** | - Nội dung trao đổi giữa người dùng và trợ lý ảo được lưu lại làm lịch sử trò chuyện để sử dụng cho các lần sau.<br>- Nếu người dùng thực hiện đặt phòng qua trò chuyện, một đơn đặt phòng mới ở trạng thái chờ thanh toán được ghi nhận trong hệ thống. |
| **Điểm mở rộng** | Không có |
