package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.UserResponseDto;
import com.teamcocoon.QuizzyAPI.exceptions.AuthentificationException;
import com.teamcocoon.QuizzyAPI.exceptions.EntityAlreadyExists;
import com.teamcocoon.QuizzyAPI.exceptions.EntityNotFoundedException;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.repositories.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(String uid, String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new EntityAlreadyExists("Username already exists");
        }


        User user = new User();
        user.setUserId(uid);
        user.setUsername(username);
        userRepository.save(user);
    }
    public UserResponseDto getUserData(Jwt jwt) {
        if(jwt == null) {
            throw new AuthentificationException("User not authenticated");
        }
        String uid = jwt.getClaim("sub");
        String email = jwt.getClaim("email");
        if (uid == null || email == null) {
            throw new AuthentificationException("Invalid authentication token");
        }
        User user = getUserByUID(uid);
        return new UserResponseDto(user.getUserId(), email, user.getUsername());
    }
    public User getUserByUID(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundedException("User not found"));
    }
}
