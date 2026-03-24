package com.auction.model;
public class Admin extends User{
    private String department;
    public Admin(String username, String password, String email, String fullName, String department) {
        super(username, password, email, fullName);
        this.department = department;
    }
    @Override
    public void displayRoleInfo() {
        System.out.println("Role: ADMIN | Department: " + department);
    }
    public void banUser(User user, String reason) {
        if (!user.isActive()) {
            System.out.println("Not active already.");
            return;
        }
        user.setActive(false);
        System.out.println("Admin [" + this.getUsername() + "] đã KHÓA user: " + user.getUsername() + ". Lý do: " + reason);
    }
    //public void cancelAuction(Auction auction, String reason) {}
}
