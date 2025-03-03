package com.teamcocoon.QuizzyAPI.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;



@Slf4j
@Configuration
public class TestUtils {

    @Autowired
    private static MockMvc mvc;

    private static ObjectMapper mapper = new ObjectMapper();

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";


    // Constructeur
    public TestUtils(MockMvc mvc) {
        TestUtils.mvc = mvc;
    }

    // Créer un utilisateur si non existant (pour simplification, appel simulé)
    public static void createUserIfNotExists(String username) throws Exception {
        UserRequestDto user = new UserRequestDto(username);
        mvc.perform(post("/api/users")
                .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)));
    }

//    public static <T> Optional<Pair<T,String>> performRequest(
//            String typeRequest,
//            String rootPath, Object data, Class<T> returnDtoClass) throws Exception {
//
//        MvcResult result = null;
//
//        log.info("PerformRequest : typeRequest: " + typeRequest+"data: " + data);
//        if (POST.equalsIgnoreCase(typeRequest)) {
//            result = mvc.perform(post(rootPath)
//                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(mapper.writeValueAsString(data)))
//                    .andReturn();
//
//        } else if (GET.equalsIgnoreCase(typeRequest)) {
//            result = mvc.perform(get(rootPath)
//                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andReturn();
//        } else {
//            throw new IllegalArgumentException("Unsupported request type: " + typeRequest);
//        }
//        log.info("PerformRequest : typeRequest: " + typeRequest+" fin " );
//
//
//        if(!result.getResponse().getContentAsString().isEmpty()){
//            String jsonResponse = result.getResponse().getContentAsString();
//
//            log.info("PerformRequest : response : " + jsonResponse);
//
//            String locationHeader = result.getResponse().getHeader(HttpHeaders.LOCATION);
//
//            T responseObject = jsonResponse.isEmpty() ? null : mapper.readValue(jsonResponse, returnDtoClass);
//
//            log.info("PerformRequest : response in return Type ::  {} :: is {} ",returnDtoClass,responseObject);
//
//            return Optional.of(Pair.of(responseObject, locationHeader));
//
//        }
//
//
//        return Optional.of(Pair.of(null, result.getResponse().getHeader("Location")));
//    }

    // Méthode générique pour la gestion des requêtes
    private static MvcResult performHttpRequest(String rootPath, String typeRequest, Object data) throws Exception {
        if (POST.equalsIgnoreCase(typeRequest)) {
            return mvc.perform(post(rootPath)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(data)))
                    .andReturn();
        } else if (GET.equalsIgnoreCase(typeRequest)) {
            return mvc.perform(get(rootPath)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } else if ("PUT".equalsIgnoreCase(typeRequest)) {
            return mvc.perform(put(rootPath)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(data)))
                    .andReturn();
        }  else if ("PATCH".equalsIgnoreCase(typeRequest)) {
        return mvc.perform(patch(rootPath)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(data)))
                .andReturn();
    } else {
            throw new IllegalArgumentException("Unsupported request type: " + typeRequest);
        }
    }

    // POST Request
    public static <T> Response<T> performPostRequest(String rootPath, Object data, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, POST, data);
        return handleResponse(result, returnDtoClass);
    }

    // GET Request
    public static <T> Response<T> performGetRequest(String rootPath, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, GET, null);
        return handleResponse(result, returnDtoClass);
    }

    // PUT Request
    public static <T> Response<T> performPutRequest(String rootPath, Object data, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, PUT, data);
        return handleResponse(result, returnDtoClass);
    }
    public static <T> Response<T> performPatchRequest(String rootPath, Object data, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, PATCH, data);
        return handleResponse(result, returnDtoClass);
    }

    private static <T> Response<T> handleResponse(MvcResult result, Class<T> returnDtoClass) throws Exception {
        int status = result.getResponse().getStatus();

        String jsonResponse = result.getResponse().getContentAsString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Location", result.getResponse().getHeader(HttpHeaders.LOCATION));

        T responseObject = jsonResponse.isEmpty() ? null : mapper.readValue(jsonResponse, returnDtoClass);

        log.info("Response body: {}", responseObject);
        log.info("Headers: {}", headers);

        return new Response<>(status, responseObject, headers);
    }


    public static record Response<T>(
            int status,               // Le statut HTTP de la réponse
            T body,                   // Le corps de la réponse avec le type générique
            Map<String, String> headers // Les headers de la réponse
    ) {}
}