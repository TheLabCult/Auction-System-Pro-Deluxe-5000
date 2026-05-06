package com.auction.server.dao;

import com.auction.shared.models.User;

import java.util.List;     
import java.util.Optional; 

/*
Vai trò của File:

Design Pattern: DAO (Data Access Object)

Phân tách logic lưu trữ dữ liệu khỏi business logic
UserService phụ thuộc vào cái này

Lí do của Optional: 
- findById() và findByUsername() có thể không trả lại gì cả vì không tìm thấy 
- Trả lại Optional thì hàm gọi có thể xử lý not found dễ dàng
- Nếu không thì phải null check

Triển khai bởi: SQLiteUserDAO, UserDAO chỉ là interface
 */
public interface UserDAO {

    User save(User user);

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();
    
    void updateActive(long userId, boolean active);
}
