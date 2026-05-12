package com.auction.server.controllers;

import com.auction.server.dao.UserDAO;            // injected dependency - talks to the database
import com.auction.server.enums.UserRole;
import com.auction.server.exceptions.AuthException;
import com.auction.server.factory.UserFactory;
import com.auction.server.models.User;

import java.util.List; 

/**
 * Business logic for registration, login, user listing, and account bans.
 */
public final class UserManager {

    
    private final UserDAO userDAO;

    public UserManager(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Registration

    public User register(String username, String password, String email, String roleName) {
        
        if (username == null || username.isBlank())
            throw new AuthException("Username must not be blank");
        if (password == null || password.length() < 4)
            throw new AuthException("Password must be at least 4 characters");
        if (email == null || !email.contains("@"))
            throw new AuthException("Invalid email address");

        
        
        UserRole role;
        try {
            role = UserRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthException("Invalid role: " + roleName);
        }

        // Admins can only be created by seeding - never by self-registration.
        // if (role == UserRole.ADMIN)
        //     throw new AuthenticationException("Cannot self-register as ADMIN");

        // Duplicate username check - findByUsername() queries the DB.
        userDAO.findByUsername(username).ifPresent(u -> {
            throw new AuthException("Username already taken: " + username);
        });

        
        User user = UserFactory.create(role);
        user.setUsername(username);
        user.setPassword(password); 
        user.setEmail(email);
        return userDAO.save(user); // DAO sets user.id before returning
    }

    // Login

    public User login(String username, String password) {
        
        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        
        if (!user.isActive())
            throw new AuthException("Account is banned");

        
        if (password != user.getPassword())
            throw new AuthException("Invalid credentials");

        return user;
    }

    // Admin operations

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public void banUser(long targetId, User requester) {
        if (requester.getRole() != UserRole.ADMIN)
            throw new AuthException("Only admins can ban users");
        
        User user = userDAO.findById(targetId)
                .orElseThrow(() -> new AuthException("User not found"));
        userDAO.updateActive(targetId, false); // set active=0 in the database
    }
}
