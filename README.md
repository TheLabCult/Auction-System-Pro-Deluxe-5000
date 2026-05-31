# FINAL - Auction System Pro Deluxe 5000

## 1. Mô tả bài toán và phạm vi hệ thống

Auction System Pro Deluxe 5000 là app đấu giá trực tuyến dạng client-server. Server quản lý dữ liệu, giao tiếp với database, chứa nghiệp vụ đấu giá; Client là ứng dụng JavaFX để người dùng đăng nhập, xem phiên đấu giá và sử dụng theo vai trò.

Hệ thống đã thực hiện:

- Có người dùng theo vai trò `BIDDER`, `SELLER`, `ADMIN`.
- Quản lý sản phẩm và phiên đấu giá.
- Đặt giá thủ công, auto-bidding, cập nhật giá realtime.
- Tự động kết thúc phiên đấu giá theo thời gian.
- Anti-sniping: gia hạn phiên khi có bid sát thời điểm kết thúc.
- Giao diện desktop JavaFX/FXML cho đăng nhập, đăng ký, đấu giá, dashboard seller và admin panel.
- Lưu trữ dữ liệu bằng SQLite, chỉ server truy cập database.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

### Công nghệ sử dụng

- Java 17
- JavaFX 21
- Maven
- SQLite và SQLite JDBC
- TCP Socket với message JSON 
- Gson 2.10.1.
- SLF4J 2.0.9.
- JUnit 5.10.0 và Mockito 5.6.0 cho kiểm thử.

### Môi trường chạy

- Hệ điều hành khuyến nghị: Windows (các hướng dẫn trong đây đều là cho Windows như là các lệnh Powershell)
- Nếu chạy bằng file `.jar`: cần JDK 17 trở lên.
- Nếu chạy bằng bản đóng gói `.exe` trong `dist/`: không cần JDK
- Maven có thể dùng bản cài sẵn trên máy hoặc Maven đi kèm repo tại `apache-maven-3.9.15/`.
- Server mặc định lắng nghe TCP port `9090`.


### Yêu cầu cài đặt

Nếu build từ source code:

```powershell
mvn install
```

Hoặc dùng Maven đi kèm repo:

```powershell
.\apache-maven-3.9.15\bin\mvn.cmd install
```

Nếu chỉ chạy bản đóng gói:

- Không cần cài Maven.
- Chạy nguyên thư mục `dist/AuctionServer/` và `dist/AuctionClient/`.
- Không tách riêng file `.exe` khỏi thư mục `app/` và `runtime/` đi kèm.

## 3. Cấu trúc thư mục và module chính

```text
.
|-- auction-common/          # DTO, protocol, request/response dùng chung Client/Server
|-- auction-server/          # Server: model, DAO, service, network, database, test
|-- auction-client/          # Client JavaFX: controller, FXML, CSS, network, session
|-- dist/
|   |-- AuctionServer/       # Bản đóng gói chạy trực tiếp cho server
|   |-- AuctionClient/       # Bản đóng gói chạy trực tiếp cho client
|-- sqlite/                  # Công cụ SQLite cho Windows
|-- apache-maven-3.9.15/     # Maven đi kèm repo
|-- auction.db               # Database SQLite mẫu/dữ liệu hiện tại
|-- Bao_cao_btl.docx
|-- Video Demo.mp4
|-- README.md
|-- pom.xml                  
```

Các module chính:

- `auction-common`: định nghĩa `Message`, `MessageType`, DTO và payload request/response để client và server serialize JSON thống nhất.
- `auction-server`: xử lý nghiệp vụ, quản lý SQLite dâtbase, nhận kết nối TCP, gửi request và broadcast sự kiện realtime.
- `auction-client`: giao diện JavaFX, kết nối server, quản lý phiên đăng nhập và các màn hình theo vai trò.

## 4. Vị trí các file `.jar`

File `.jar` sinh ra khi build Maven:

- `auction-common/target/auction-common-1.0.0.jar`
- `auction-server/target/auction-server-1.0.0.jar`
- `auction-server/target/auction-server-1.0.0-fat.jar`
- `auction-client/target/auction-client-1.0.0.jar`
- `auction-client/target/auction-client-1.0.0-fat.jar`

File `.jar` trong bản đóng gói `dist`:

- `dist/AuctionServer/app/auction-server-1.0.0.jar`
- `dist/AuctionServer/app/auction-server-1.0.0-fat.jar`
- `dist/AuctionClient/app/auction-client-1.0.0.jar`
- `dist/AuctionClient/app/auction-client-1.0.0-fat.jar`

Ghi chú: khi chạy trực tiếp bằng `java -jar`, nên dùng file `*-fat.jar` vì đã đóng gói dependency cần thiết.

## 5. Hướng dẫn chạy ứng dụng

### Cách 1: Chạy từ source code hoặc file JAR

1. Build toàn bộ project:

```powershell
mvn install
```

2. Chạy Server trước:

```powershell
java -jar auction-server/target/auction-server-1.0.0-fat.jar
```


3. Sau khi Server khởi động xong, chạy Client:

```powershell
java -jar auction-client/target/auction-client-1.0.0-fat.jar
```

4. Nếu Client kết nối tới Server trên máy khác trong LAN (sử dụng file exe):

Điều kiện: các máy client và máy server cần ở trên một mạng LAN

#### Các bước:
1. Giữ dist/AuctionServer trên máy server; Các máy client cần có dist/AuctionClient (lưu ý: copy cả thư mục, không chỉ mỗi file exe)
2. Trên máy server, chạy terminal: ```ipconfig```, tìm mục IPv4
3. Trên các máy client, vào trong AuctionClient/app/AuctionClient.cfg, thêm dòng sau đay vào cuối file: 
```
java-options=-Dserver.host=xx.xx.xx.xx        <-- Đây là IPv4 vừa tìm ở máy server
java-options=-Dserver.port=9090 
```
4. Trên máy server, vào settings Firewall, mở Port TCP cho phép nhận tín hiệu trên cùng một mạng LAN.
5. Lần lượt chạy file exe trên máy server trước rồi chạy trên (các) máy client.

### Cách 2: Chạy bản đóng gói `.exe`

1. Chạy Server trước:

```powershell
.\dist\AuctionServer\AuctionServer.exe
```

2. Sau khi Server sẵn sàng, chạy Client:

```powershell
.\dist\AuctionClient\AuctionClient.exe
```

## 6. Danh sách chức năng đã hoàn thành

- Đăng ký tài khoản theo vai trò seller và bidder, không thể đăng kí admin. Tài khoản admin phải seed từ trong source code.
- Đăng nhập và duy trì phiên người dùng.
- Phân quyền Bidder/Seller/Admin.
- Hiển thị danh sách phiên đấu giá.
- Tìm kiếm/lọc phiên đấu giá trên Client.
- Xem chi tiết phiên đấu giá.
- Xem lịch sử đặt giá và biểu đồ giá realtime.
- Đặt giá thủ công.
- Auto-bidding với `maxBid` và `increment`.
- Broadcast giá mới realtime cho các client đang theo dõi phiên đấu giá.
- Tự động kết thúc phiên đấu giá theo `endTime`.
- Anti-sniping: gia hạn phiên khi có bid trong 30 giây cuối.
- Seller tạo sản phẩm theo danh mục `ELECTRONICS`, `ART`, `VEHICLE`.
- Seller tạo phiên đấu giá cho sản phẩm.
- Seller xem danh sách sản phẩm/phiên đấu giá của mình.
- Seller hủy phiên đấu giá.
- Seller đánh dấu phiên đấu giá đã thanh toán.
- Upload/hiển thị ảnh sản phẩm.
- Admin xem danh sách người dùng.
- Admin khóa và mở khóa tài khoản người dùng.
- Lưu trữ dữ liệu bằng SQLite.
- Kiểm thử service/util bằng JUnit và Mockito.
- Đóng gói Server/Client thành `.jar` và `.exe`.

## 7. Link báo cáo PDF và video demo

- Báo cáo PDF: [Bao_cao_bai_tap_lon_Auction_System.pdf](./Bao_cao_btl.pdf)
- Video demo: [Video Demo.mp4](./Video%20Demo.mp4)

## 8. Cây thư mục đầy đủ bổ sung cho mục 3 - cấu trúc thư mục

Lệnh Bash (không phải Powershell):
```tree -I "node_modules|.git|build|dist|target|*.log|apache-maven-3.9.15|resources|sqlite|*.docx|*.pdf"```


``` Folder tree:
├── README.md
├── Tree.md
├── auction-client
│   ├── dependency-reduced-pom.xml
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── auction
│                       └── client
│                           ├── ClientLauncher.java
│                           ├── ClientMain.java
│                           ├── controller
│                           │   ├── AdminController.java
│                           │   ├── AuctionDetailController.java
│                           │   ├── AuctionListController.java
│                           │   ├── ItemDetailDialogController.java
│                           │   ├── LoginController.java
│                           │   ├── RegisterController.java
│                           │   ├── SellerDashboardController.java
│                           │   └── UserProfileController.java
│                           ├── network
│                           │   └── ServerConnection.java
│                           ├── session
│                           │   └── ClientSession.java
│                           └── util
│                               ├── AlertUtil.java
│                               └── SceneManager.java
├── auction-common
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── auction
│                       └── common
│                           ├── dto
│                           │   ├── AuctionDTO.java
│                           │   ├── BidDTO.java
│                           │   ├── ItemDTO.java
│                           │   ├── UserDTO.java
│                           │   └── package-info.java
│                           ├── protocol
│                           │   ├── Message.java
│                           │   └── MessageType.java
│                           └── request
│                               ├── EmptyPayload.java
│                               ├── Requests.java
│                               └── Responses.java
├── auction-server
│   ├── dependency-reduced-pom.xml
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── com
│       │           └── auction
│       │               └── server
│       │                   ├── ServerMain.java
│       │                   ├── dao
│       │                   │   ├── AuctionDAO.java
│       │                   │   ├── AutoBidDAO.java
│       │                   │   ├── BidDAO.java
│       │                   │   ├── ItemDAO.java
│       │                   │   ├── UserDAO.java
│       │                   │   └── impl
│       │                   │       ├── SQLiteAuctionDAO.java
│       │                   │       ├── SQLiteAutoBidDAO.java
│       │                   │       ├── SQLiteBidDAO.java
│       │                   │       ├── SQLiteItemDAO.java
│       │                   │       └── SQLiteUserDAO.java
│       │                   ├── db
│       │                   │   └── DatabaseManager.java
│       │                   ├── exception
│       │                   │   ├── AuctionException.java
│       │                   │   ├── AuthException.java
│       │                   │   └── BidException.java
│       │                   ├── factory
│       │                   │   ├── ItemFactory.java
│       │                   │   └── UserFactory.java
│       │                   ├── model
│       │                   │   ├── Admin.java
│       │                   │   ├── Art.java
│       │                   │   ├── Auction.java
│       │                   │   ├── AuctionStatus.java
│       │                   │   ├── AutoBid.java
│       │                   │   ├── BidTransaction.java
│       │                   │   ├── Bidder.java
│       │                   │   ├── Electronics.java
│       │                   │   ├── Entity.java
│       │                   │   ├── Item.java
│       │                   │   ├── ItemCategory.java
│       │                   │   ├── Seller.java
│       │                   │   ├── User.java
│       │                   │   ├── UserRole.java
│       │                   │   └── Vehicle.java
│       │                   ├── network
│       │                   │   ├── AuctionServer.java
│       │                   │   └── ClientHandler.java
│       │                   ├── observer
│       │                   │   ├── AuctionEventBus.java
│       │                   │   └── AuctionObserver.java
│       │                   ├── service
│       │                   │   ├── AuctionService.java
│       │                   │   ├── BidService.java
│       │                   │   ├── ItemService.java
│       │                   │   └── UserService.java
│       │                   └── util
│       │                       ├── DateUtil.java
│       │                       ├── DtoMapper.java
│       │                       └── PasswordUtil.java
│       └── test
│           └── java
│               └── com
│                   └── auction
│                       └── server
│                           ├── service
│                           │   ├── AuctionServiceTest.java
│                           │   ├── BidServiceTest.java
│                           │   └── UserServiceTest.java
│                           └── util
│                               └── PasswordUtilTest.java
├── auction.db
└── pom.xml
```

