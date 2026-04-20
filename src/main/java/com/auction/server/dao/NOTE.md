# Đã thực hiên:

1. DatabaseConnection - mở cổng kết nối đến database

2. DatabaseManager - gọi khi main() bắt đầu chạy server, tạo các bảng lưu trữ dữ liệu
trong database nếu chưa có, có rồi thì không tạo - các thuộc tính trong các bảng hiện
chưa check hết với tất cả các thuộc tính của các đối tượng đã lưu, cần chỉnh sửa thêm

3. UserDAO là phương thức truy cập, read và write lên bảng users trong DB

Trong phần UserDAO, user cần bổ sung thuộc tính role để khi hiện dữ liệu trên bảng
database, từng người dùng có thể biết ai là Bidder, ai là Seller, ai là Admin (nếu có)
Hiện tại chưa lưu Role ở trong bảng DB, khi nào mấy bn backend bổ sung sẽ thêm

UserDAO đã có các phương thức save() - thêm user

4. Xóa thuộc tính List<Item> items trong Seller, SQLite là một relational DB, tức là mối quan
hệ của Seller và các items sẽ được quản lí bởi DB, không phải bởi Object [x]

# Cần thực hiện:
Hiện tại, khi lấy dữ liệu ra từ DB là sẽ khởi tạo các Object mới, dữ liệu trong DB chỉ
là các thông tin thuộc tính
Cần làm: 
1. Overloading - hàm khởi tạo cho phép gán ID và createdAt, không phải tự tạo do
chương trình tạo ra ở cả các lớp con của Entity [x]
2. Thêm cột createdAt vào trong bảng SQL, để kiểu dữ liệu là String, khi nào cần lấy ra thì 
parse [x]
3. Thêm enum Role cho các loại User [x]

Câu hỏi: có lẽ Seller không cần danh sách các sản phẩm, chỉ cần gán ID sản phẩm reference tới
username hoặc ID của Seller là được? [x]