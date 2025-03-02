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
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Slf4j
@Configuration
public class TestUtils {

    @Autowired
    private static MockMvc mvc;

    private static ObjectMapper mapper = new ObjectMapper();

    public static final String GET = "GET";
    public static final String POST = "POST";

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

    /**
     * Méthode générique pour effectuer des requêtes HTTP et obtenir une réponse avec des données.
     *
     * @param typeRequest        Le type de la requête ("POST" ou "GET")
     * @param rootPath           Le chemin de l'URL (sans variables dynamiques)
     * @param data               Les données à envoyer (utilisé pour POST)
     * @param returnDtoClass     La classe de l'objet à retourner
     * @param <T>                Le type de la réponse attendue
     * @return                  Un `Optional` contenant la réponse de la requête
     */
    public static <T> Optional<Pair<T,String>> performRequest(
            String typeRequest,
            String rootPath, Object data, Class<T> returnDtoClass) throws Exception {

        MvcResult result = null;

        log.info("PerformRequest : typeRequest: " + typeRequest+"data: " + data);
        if (POST.equalsIgnoreCase(typeRequest)) {
            result = mvc.perform(post(rootPath)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(data)))
                    .andReturn();

        } else if (GET.equalsIgnoreCase(typeRequest)) {
            result = mvc.perform(get(rootPath)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", "testUser")))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + typeRequest);
        }
        log.info("PerformRequest : typeRequest: " + typeRequest+" fin " );


        if(!result.getResponse().getContentAsString().isEmpty()){
            String jsonResponse = result.getResponse().getContentAsString();

            log.info("PerformRequest : response : " + jsonResponse);

            String locationHeader = result.getResponse().getHeader(HttpHeaders.LOCATION);

            T responseObject = jsonResponse.isEmpty() ? null : mapper.readValue(jsonResponse, returnDtoClass);

            log.info("PerformRequest : response in return Type ::  {} :: is {} ",returnDtoClass,responseObject);

            return Optional.of(Pair.of(responseObject, locationHeader));

        }


        return Optional.of(Pair.of(null, result.getResponse().getHeader("Location")));
    }

    /**
     * Crée un HttpHeaders contenant l'URL de la ressource créée, à partir d'un ID.
     *
     * @param baseUrl Le chemin de base de l'URL
     * @param id      L'ID de la ressource
     * @return        L'en-tête HttpHeaders avec le Location
     */
    public static HttpHeaders createURLLocation(String baseUrl, long id) {
        URI location = URI.create(baseUrl + "/" + id);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return headers;
    }

}


