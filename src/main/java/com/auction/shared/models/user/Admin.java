package main.java.com.auction.shared.models.user;

public class Admin extends User{
    //constructors
    public Admin(String fullname, String username, String email, String password) {
        super(fullname, username, email, password);
    }

    //Admin Powers
    public void ban(User user) {
        user.setActiveStatus(false);
    }
    public void unban(User user) {
        user.setActiveStatus(true);
    }
}
