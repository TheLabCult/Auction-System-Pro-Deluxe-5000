package com.auction.shared.dto;

/*
Thông tin tài khoản user an toàn để gửi đến client
đã loại bỏ thuộc tính passowrd
 */
public class UserDTO {

    private long id;        
    private String username;
    private String email;   
    private String role;    
    private boolean active; 

    public UserDTO() {} // dùng bởi Gson cho deserialization

    public UserDTO(long id, String username, String email, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    // Getters

    public long getId() { return id; }

    /*tên login và tên hiển thị trong panel của user trong khi dùng ứng dụng*/
    public String getUsername() { return username; }

    /*hiển thị trong panel của admin (nếu có)*/
    public String getEmail() { return email; }

    /* Client dùng để quyết định màn hình nào sẽ hiển thị sau login
    "BIDDER" -> dánh sách các phiên đấu giá
    "SELLER" -> seller dashboard
    "ADMIN"  -> admin panel nếu có admin */
    public String getRole() { return role; }

    /* nếu đã bị ban thì từ chối login với AuthenticationException */
    public boolean isActive() { return active; }

    // Setters cần bởi Gson khi deserialize luồng JSON đến.
    public void setId(long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
}
