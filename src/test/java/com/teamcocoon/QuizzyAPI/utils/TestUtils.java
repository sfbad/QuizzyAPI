package com.teamcocoon.QuizzyAPI.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Configuration
public class TestUtils {

    @Autowired
    private static MockMvc mvc;

    private static ObjectMapper mapper = new ObjectMapper();

    // Constantes pour les types de requêtes HTTP
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";

    // Constructeur
    public TestUtils(MockMvc mvc) {
        TestUtils.mvc = mvc;
    }

    /**
     * Crée un utilisateur si celui-ci n'existe pas déjà.
     * Cette méthode simule l'appel à l'API pour créer un utilisateur.
     * @param username Le nom d'utilisateur à créer
     * @throws Exception Si une erreur survient lors de l'exécution de la requête HTTP
     */
    public static void createUserIfNotExists(String username) throws Exception {
        UserRequestDto user = new UserRequestDto(username);
        mvc.perform(post("/api/users")
                .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(user)));
    }

    /**
     * Méthode générique pour gérer les requêtes HTTP.
     * Effectue une requête HTTP selon le type spécifié (POST, GET, PUT, PATCH).
     * @param rootPath L'URL de la ressource cible de la requête
     * @param typeRequest Le type de la requête HTTP (POST, GET, PUT, PATCH)
     * @param data Les données à envoyer dans la requête (peut être null pour une requête GET)
     * @return Le résultat de la requête HTTP sous la forme d'un MvcResult
     * @throws Exception Si une erreur survient lors de l'exécution de la requête
     */
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
        } else if (PUT.equalsIgnoreCase(typeRequest)) {
            return mvc.perform(put(rootPath)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(data)))
                    .andReturn();
        } else if (PATCH.equalsIgnoreCase(typeRequest)) {
            return mvc.perform(patch(rootPath)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(data)))
                    .andReturn();
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + typeRequest);
        }
    }

    /**
     * Effectue une requête HTTP POST et gère la réponse.
     * @param rootPath L'URL de la ressource cible
     * @param data Les données à envoyer dans la requête
     * @param returnDtoClass La classe du DTO attendu dans la réponse
     * @param <T> Le type générique du DTO
     * @return Un objet Response contenant le statut, le corps et les headers de la réponse
     * @throws Exception Si une erreur survient lors de l'exécution de la requête
     */
    public static <T> Response<T> performPostRequest(String rootPath, Object data, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, POST, data);

        return handleResponse(result, returnDtoClass);
    }

    /**
     * Effectue une requête HTTP GET et gère la réponse.
     * @param rootPath L'URL de la ressource cible
     * @param returnDtoClass La classe du DTO attendu dans la réponse
     * @param <T> Le type générique du DTO
     * @return Un objet Response contenant le statut, le corps et les headers de la réponse
     * @throws Exception Si une erreur survient lors de l'exécution de la requête
     */
    public static <T> Response<T> performGetRequest(String rootPath, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, GET, null);
        return handleResponse(result, returnDtoClass);
    }

    /**
     * Effectue une requête HTTP PUT et gère la réponse.
     * @param rootPath L'URL de la ressource cible
     * @param data Les données à envoyer dans la requête
     * @param returnDtoClass La classe du DTO attendu dans la réponse
     * @param <T> Le type générique du DTO
     * @return Un objet Response contenant le statut, le corps et les headers de la réponse
     * @throws Exception Si une erreur survient lors de l'exécution de la requête
     */
    public static <T> Response<T> performPutRequest(String rootPath, Object data, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, PUT, data);
        return handleResponse(result, returnDtoClass);
    }

    /**
     * Effectue une requête HTTP PATCH et gère la réponse.
     * @param rootPath L'URL de la ressource cible
     * @param data Les données à envoyer dans la requête
     * @param returnDtoClass La classe du DTO attendu dans la réponse
     * @param <T> Le type générique du DTO
     * @return Un objet Response contenant le statut, le corps et les headers de la réponse
     * @throws Exception Si une erreur survient lors de l'exécution de la requête
     */
    public static <T> Response<T> performPatchRequest(String rootPath, Object data, Class<T> returnDtoClass) throws Exception {
        MvcResult result = performHttpRequest(rootPath, PATCH, data);
        return handleResponse(result, returnDtoClass);
    }

    /**
     * Traite la réponse d'une requête HTTP.
     * Cette méthode extrait le statut HTTP, le corps de la réponse et les headers.
     * Elle sérialise également le corps de la réponse dans un objet du type générique `returnDtoClass`.
     * @param result L'objet MvcResult contenant la réponse de la requête HTTP
     * @param returnDtoClass La classe du DTO attendu dans la réponse
     * @param <T> Le type générique du DTO
     * @return Un objet Response contenant le statut, le corps et les headers de la réponse
     * @throws Exception Si une erreur survient lors de la gestion de la réponse
     */
    private static <T> Response<T> handleResponse(MvcResult result, Class<T> returnDtoClass) throws Exception {
        int status = result.getResponse().getStatus();

        String jsonResponse = result.getResponse().getContentAsString();

        Map<String, String> headers = new HashMap<>();  // Créer un Map pour les headers
        headers.put("Location", result.getResponse().getHeader(HttpHeaders.LOCATION));


        // Désérialiser le corps de la réponse JSON en objet du type `returnDtoClass`
        T responseObject = jsonResponse.isEmpty() ? null : mapper.readValue(jsonResponse, returnDtoClass);

        log.info("Response body: {}", responseObject);
        log.info("Headers: {}", headers);

        return new Response<>(status, responseObject, headers);
    }

    /**
     * Représente une réponse HTTP contenant un statut, un corps et des headers.
     * @param <T> Le type générique du corps de la réponse
     */
    public static record Response<T>(
            int status,
            T body,
            Map<String, String> headers
    ) {}

    public static void testProtectedEndpoint(String url) throws Exception {
        // Cas où l'utilisateur est authentifié
        mvc.perform(get(url)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))) // JWT valide
                .andExpect(status().isOk());

        // Cas où l'utilisateur n'est PAS authentifié
        mvc.perform(get(url)) // Aucune authentification
                .andExpect(status().isUnauthorized());
    }

}
