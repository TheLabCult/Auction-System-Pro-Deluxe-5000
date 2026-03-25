package com.auction.service;

import com.auction.model.Bidder;
import com.auction.model.Seller;
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
    public Bidder registerBidder(String username, String password, String email, String fullName){
        if (userDatabase.containsKey(username)) throw new IllegalArgumentException("Username is already in use");
        Bidder bidder = new Bidder(username, password, email, fullName, 0.0);
        userDatabase.put(username, bidder);
        System.out.println("Successfully registered Bidder: " + username);
        return bidder;
    }
    public Seller registerSeller(String username, String password, String email, String fullName, String shopName){
        if  (userDatabase.containsKey(username)) throw new IllegalArgumentException("Username is already in use");
        Seller seller = new Seller(username, password, email, fullName, shopName);
        userDatabase.put(username, seller);
        System.out.println("Successfully registered Seller: " + username);
        return seller;
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
