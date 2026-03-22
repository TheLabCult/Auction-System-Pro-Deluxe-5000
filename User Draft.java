abstract class User {
    protected String username;
    protected String password;
    protected String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    abstract protected String getPassword();

    public String getRole() {
        return role;
    }

    public String toString() {
        return "Username: " + username + ", Role: " + role;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeRole(String newRole) {
        this.role = newRole;
    }

    public void changeUsername(String newUsername) {
        this.username = newUsername;
    }
}
class Bidder extends User {
    public Bidder(String username, String password) {
        super(username, password, "Bidder");
    }

    @Override
    protected String getPassword() {
        return this.password;
    }
}
class Seller extends User {
    public Seller(String username, String password) {
        super(username, password, "Seller");
    }

    @Override
    protected String getPassword() {
        return this.password;
    }
}
class Admin extends User {
    public Admin(String username, String password) {
        super(username, password, "Admin");
    }

    @Override
    protected String getPassword() {
        return this.password;
    }
}