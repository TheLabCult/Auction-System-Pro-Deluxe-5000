package com.auction.service;

import com.auction.model.User;
import java.util.HashMap;
import java.util.Map;
public class UserManager {
    private Map<String, User> userDatabase; //key = username, value = User
    //singleton pattern: only 1 usermanager
    private static UserManager instance;
    private UserManager(){
        userDatabase = new HashMap<>();
    }
    public static UserManager getInstance(){
        if(instance == null){
            instance = new UserManager();
        }
        return instance;
    }
    //REGISTRATION
    public User registerUser(String username, String password, String email, String fullName, String shopName){
        if (userDatabase.containsKey(username)) throw new IllegalArgumentException("Username is already in use");
        User user = new User(username, password, email, fullName, shopName);
        userDatabase.put(username, user);
        System.out.println("Successfully registered User: " + username);
        return user;
    }
    //LOGIN
    public User login(String username, String password){
        User user = userDatabase.get(username);
        if (user == null) throw new IllegalArgumentException("Cannot find user");
        if (!user.verifyPassword(password)) throw new IllegalArgumentException("Wrong Password") ;
        if (!user.isActive()) throw new IllegalArgumentException("User is not active");
        System.out.println("Successfully logged in. Welcome: " + username);
        return user;
    }
    public Map<String, User> getAllUsers(){ //for Admin
        return userDatabase;
    }
}
