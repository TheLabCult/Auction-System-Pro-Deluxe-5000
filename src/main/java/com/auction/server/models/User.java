package com.auction.server.models;

import com.auction.server.enums.UserRole;

public abstract class User extends Entity {
    private String username;
    private String password;
    private String email;
    private boolean active;

    protected User() { super(); }


    /* Abstract contract =========================================== */ 
    /* Trả lại enums: BIDDER, SELLER, ADMIN */
    public abstract UserRole getRole();

    /* Gọi bởi Frontend để hiển thị màn hình cho frontend
    kiểm tra xem đây là bidder hay là seller */
    public abstract boolean canBid();
    public abstract boolean canSell();


    @Override
    public void printInfo() {
        System.out.printf("[%s] id=%d  username=%-20s  email=%s  active=%s%n",
                getRole(), id, username, email, active);
    }
    

    /* Setters / Getters ============================================ */
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Active status — false tức là acc đã bị ban bởi admin
     * UserService.login() từ chối đăng nhập nếu acc đã bị ban
     */
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
