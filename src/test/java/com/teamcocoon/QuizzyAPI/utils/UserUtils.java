package com.teamcocoon.QuizzyAPI.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class UserUtils {
    private MockMvc mvc;
    private ObjectMapper mapper = new ObjectMapper();

    UserUtils(MockMvc mvc){
        this.mvc = mvc;
    }

    void createUserIfNotExists(String username) throws Exception {
        UserRequestDto user = new UserRequestDto(username);
        mvc.perform(post("/api/users")
                .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)));
    }
}
