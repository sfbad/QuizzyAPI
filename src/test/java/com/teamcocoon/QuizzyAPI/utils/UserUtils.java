package com.teamcocoon.QuizzyAPI.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Configuration
public class UserUtils {
    @Autowired(required = false)  // this will be injected if UserUtils is used in a Spring Boot application
    private static MockMvc mvc ;
    private static ObjectMapper mapper = new ObjectMapper();
    public static final String GET="GET";
    public static final String POST="POST";

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

    public static <T> Optional<Pair<T, String>> postToPathThenReturnData(
            String typeRequest,
            String rootPath, Object data, Class<T> returnDtoClass,
            boolean returnLocationHeader, Object... pathVariables) throws Exception {

        String url = pathVariables.length > 0 ? String.format(rootPath, pathVariables) : rootPath;
        System.out.println("URL : " + url);

        MvcResult result;

        if ("POST".equalsIgnoreCase(typeRequest)) {
            result = mvc.perform(post(url)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(data)))
                    .andReturn();
        } else if ("GET".equalsIgnoreCase(typeRequest)) {
            result = mvc.perform(get(url)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } else {
            throw new IllegalArgumentException("Type de requête non supporté : " + typeRequest);
        }

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("JSON Response : " + jsonResponse);

        String locationHeader = returnLocationHeader ? result.getResponse().getHeader("Location") : null;

        // Convertir la réponse JSON en objet si elle n'est pas vide
        T responseObject = jsonResponse.isEmpty() ? null : mapper.readValue(jsonResponse, returnDtoClass);

        return Optional.of(Pair.of(responseObject, locationHeader));
    }

    /**
     * Génère un HttpHeaders contenant l'URL de location basée sur un ID.
     */
    private static HttpHeaders createURLLocation(String baseUrl, long id) {
        URI location = URI.create(baseUrl + "/" + id);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return headers;
    }

}
