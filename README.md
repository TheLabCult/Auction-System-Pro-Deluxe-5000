Trót push -f vài lần nên mất một đống commit rồi =(((

Không sao


File hierarchy:


online-auction-system/
    |--- .github/workflows/                 (CI/CD tự động test)
    |--- .gitignore                         (loại bỏ .idea, .vscode, .class)
    |--- pom.xml                            (cấu hình Maven quản lí thư viện)
    |--- README.md                          (hướng dẫn cài đặt và chạy dự án)
    |--- src/
        |--- main/
            |-- java/com/auction/
                |--- shared/
                    |--- models/            (Domain classes như Entity, User,...)
                    |--- enums/             (AutionStatus, ItemCategory...)
                    |--- exceptions         (InvalidBidException,...)
                |--- server/                (Backend: server truy cập database)
                    |--- network/           (Socket, đa luồng)
                    |--- controllers/       (Logic xử lí đấu giá, quản lý người dùng)
                    |--- dao/               (Data Access Object)
                |--- client/
                    |--- network/           (Socket client)
                    |--- controllers/       (MVC: điều khiển JavaFX)
                    |--- views/             (Tải các file FXML)
        |--- resources/                     (Chứa file giao diện, ảnh, icon)
    |-- test/
        |--- java/com/aution/               (JUnit test cho server)
                