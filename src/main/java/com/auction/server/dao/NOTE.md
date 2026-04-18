Đã thực hiên:

1. DatabaseConnection - mở cổng kết nối đến database

2. DatabaseManager - gọi khi main() bắt đầu chạy server, tạo các bảng lưu trữ dữ liệu
trong database nếu chưa có, có rồi thì không tạo - các thuộc tính trong các bảng hiện
chưa check hết với tất cả các thuộc tính của các đối tượng đã lưu, cần chỉnh sửa thêm

3. UserDAO là phương thức truy cập, read và write lên bảng users trong DB

Trong phần UserDAO, user cần bổ sung thuộc tính role để khi hiện dữ liệu trên bảng
database, từng người dùng có thể biết ai là Bidder, ai là Seller, ai là Admin (nếu có)
Hiện tại chưa lưu Role ở trong bảng DB, khi nào mấy bn backend bổ sung sẽ thêm

UserDAO đã có các phương thức save() - thêm user