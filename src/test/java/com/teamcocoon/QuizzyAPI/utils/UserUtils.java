package com.teamcocoon.QuizzyAPI.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Configuration
public class UserUtils {
    @Autowired(required = false)  // this will be injected if UserUtils is used in a Spring Boot application
    private static MockMvc mvc ;
    private static ObjectMapper mapper = new ObjectMapper();

    UserUtils(MockMvc mvc){
        this.mvc = mvc;
    }

    public static void createUserIfNotExists(String username) throws Exception {
        UserRequestDto user = new UserRequestDto(username);
        mvc.perform(post("/api/users")
                .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)));
    }
}
