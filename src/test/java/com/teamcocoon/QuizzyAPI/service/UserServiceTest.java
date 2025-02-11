package com.teamcocoon.QuizzyAPI.service;

import com.teamcocoon.QuizzyAPI.dtos.UserResponseDto;
import com.teamcocoon.QuizzyAPI.exceptions.AuthentificationException;
import com.teamcocoon.QuizzyAPI.exceptions.EntityAlreadyExists;
import com.teamcocoon.QuizzyAPI.exceptions.EntityNotFoundedException;
import com.teamcocoon.QuizzyAPI.model.User;
import com.teamcocoon.QuizzyAPI.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId("12345");
        user.setUsername("testUser");

        jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn("12345");
        when(jwt.getClaim("email")).thenReturn("test@example.com");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Simuler l'absence de l'utilisateur dans la base
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        // Appeler la méthode de test
        userService.registerUser("12345", "testUser");

        // Vérifier que save a bien été appelé
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionIfUserAlreadyExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        assertThrows(EntityAlreadyExists.class, () -> userService.registerUser("12345", "testUser"));
    }

    @Test
    void shouldReturnUserDataIfAuthenticated() {
        when(userRepository.findById("12345")).thenReturn(Optional.of(user));
        UserResponseDto userResponse = userService.getUserData(jwt);

        assertNotNull(userResponse);
        assertEquals("12345", userResponse.uid());
        assertEquals("testUser", userResponse.username());
    }

    @Test
    void shouldThrowExceptionIfUserNotAuthenticated() {
        assertThrows(AuthentificationException.class, () -> userService.getUserData(null));
    }

    @Test
    void shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findById("12345")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundedException.class, () -> userService.getUserByUID("12345"));
    }
}
