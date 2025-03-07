package com.teamcocoon.QuizzyAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.QuizzyApiApplication;
import com.teamcocoon.QuizzyAPI.dtos.ErrorResponseDto;
import com.teamcocoon.QuizzyAPI.dtos.ExceptionsResponseDTO;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import com.teamcocoon.QuizzyAPI.dtos.UserResponseDto;
import com.teamcocoon.QuizzyAPI.service.UserService;
import com.teamcocoon.QuizzyAPI.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = QuizzyApiApplication.class
)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@TestPropertySource("classpath:application-test.properties")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private Jwt jwt;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jwt = Mockito.mock(Jwt.class);
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldRegisterAndReturnUserDataSuccessfully() throws Exception {

        UserRequestDto user1 = new UserRequestDto("testUser");

        doNothing().when(userService).registerUser(Mockito.any(), Mockito.any(), Mockito.any());

        TestUtils.Response<Void> registerResponse = TestUtils.performPostRequest("/api/users", user1, Void.class);
        assertEquals(201, registerResponse.status());

        UserRequestDto user2 = new UserRequestDto("");
        TestUtils.Response<Void> badRequestResponse = TestUtils.performPostRequest("/api/users", user2, Void.class);
        assertEquals(400, badRequestResponse.status());

        UserResponseDto userResponseDto = new UserResponseDto("12345", "test@example.com", "testUser");
        when(userService.getUserData(Mockito.any(Jwt.class))).thenReturn(userResponseDto);

    }

    @Test
    void shouldReturnUserData() throws Exception {
        TestUtils.createUserIfNotExists("testUser");
        TestUtils.Response<UserResponseDto> getResponse = TestUtils.performGetRequest("/api/users/me", UserResponseDto.class);

        assertEquals(200, getResponse.status());
        assertEquals("12345", getResponse.body().uid());
        assertEquals("testUser", getResponse.body().username());
        assertEquals("test@example.com", getResponse.body().email());
    }

    @Test
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {
        TestUtils.Response<ExceptionsResponseDTO> response = TestUtils.performGetRequest("/api/users/me", ExceptionsResponseDTO.class);
        assertEquals(401, response.status());
    }
}
