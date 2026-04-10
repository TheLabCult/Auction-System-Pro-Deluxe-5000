package main.java.com.auction.shared.models.manager;

import main.java.com.auction.shared.models.user.User;

import java.util.HashMap;

public class UserManager {
    //constructors
    private static UserManager instance;

    private HashMap<String, User> users; //Về sau sẽ apply Database vào đây nhé Quang

    private UserManager() {
        users = new HashMap<>();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    //getters
    public HashMap<String, User> getUsers() {
        return users;
    }

    //methods
    public void addUser(User user) { users.put(user.getId(), user); }
    public boolean userAuthorize(User user) { return users.containsValue(user); }
}