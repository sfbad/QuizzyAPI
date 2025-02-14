package com.teamcocoon.QuizzyAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import com.teamcocoon.QuizzyAPI.dtos.UserResponseDto;
import com.teamcocoon.QuizzyAPI.repositories.UserRepository;
import com.teamcocoon.QuizzyAPI.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private UserRepository userRepository;
    @MockitoBean
    private UserService userService;

    private Jwt jwt;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jwt = Mockito.mock(Jwt.class);
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("testUser");

        doNothing().when(userService).registerUser(any(String.class), any(String.class),any(String.class));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldReturnUserData() throws Exception {
        UserResponseDto responseDto = new UserResponseDto("12345", "test@example.com", "testUser");

        when(userService.getUserData(any(Jwt.class))).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/me")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("12345"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testUser"));
    }
}
