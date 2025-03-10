package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.*;
import com.teamcocoon.QuizzyAPI.exceptions.EntityNotFoundedException;
import com.teamcocoon.QuizzyAPI.model.Quiz;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.teamcocoon.QuizzyAPI.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Endpoints concernant les quiz")
public class QuizController {

    private final QuizService quizService;


    /**
     * Endpoint REST qui récupère la liste des quiz avec un lien de création.
     * Extrait l'ID utilisateur du token JWT et construit la réponse pour l'issue #12.
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Récupérer la liste des quiz de l'utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des quiz récupérée avec succès", content = @Content(schema = @Schema(implementation = ListQuizResponseDto.class)))
    })
    @GetMapping()
    public ResponseEntity<?> getListQuiz(@AuthenticationPrincipal Jwt jwt) {
        String uid = jwt.getClaim("sub");
        System.out.println("UID: " + uid);
        ResponseEntity<ListQuizResponseDto> response;
        response = quizService.getListQuizByUserId(uid);

        Map<String, String> links = new HashMap<>();
        links.put("create", "http://127.0.0.1:3000/api/quiz");
        ListQuizResponseLinkDto responseWithLinks = new ListQuizResponseLinkDto(response.getBody().data(), links);
        log.info("issue 12  "+ responseWithLinks);
        return ResponseEntity.ok(responseWithLinks);
    }



    /**
     * Issue6
     * Point d'entrée REST pour la création d'un nouveau quiz.
     * Gère la transformation du DTO en entité et la réponse HTTP standardisée.
     *
     * Fonctionnalités principales :
     * - Validation du token JWT
     * - Délégation de la création au service
     * - Construction de l'URI de ressource
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Créer un quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quiz créé avec succès")
    })
    @PostMapping()
    public ResponseEntity<Void> createQuiz(@RequestBody QuizPostDto quizDto, @AuthenticationPrincipal Jwt jwt){
        if (jwt == null) {
            throw new IllegalStateException("Jwt is null");
        }

        System.out.println("Received QuizDto: " + quizDto);
        System.out.println("Request URL: " + ServletUriComponentsBuilder.fromCurrentRequest().toUriString());

        String uid = jwt.getClaim("sub");
        System.out.println("JWT received, user UID: " + uid);

        Quiz savedQuiz = quizService.createQuiz(quizDto, uid);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedQuiz.getQuizId())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * Point d'entrée REST pour la modification du titre d'un quiz.
     *
     * Applique une mise à jour partielle avec validation JWT et contrôle d'accès.
     * Implémente la logique de modification selon la story #8.
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mettre à jour le titre d'un quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Titre du quiz mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Quiz inexistant ou non autorisé", content = @Content(schema = @Schema(implementation = ExceptionsResponseDTO.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuizTitle(
            @Valid @PathVariable Long id,
            @Valid @RequestBody List<PatchQuizTitleRequestDTO> patchRequests,
            @AuthenticationPrincipal Jwt jwt) {

        String uid = jwt.getClaim("sub");

        quizService.updateQuizTitle(id, patchRequests, uid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint REST pour ajouter une nouvelle question à un quiz.
     *
     * Characteristics clés :
     * - Sécurisé avec authentification JWT
     * - Crée la question via le service
     * - Génère une URI de localisation pour la nouvelle question
     * - Retourne un statut 201 (Created) avec l'en-tête de localisation
     *
     * param jwt Token d'authentification
     * param id Identifiant du quiz
     * param question DTO contenant les détails de la nouvelle question
     * return ResponseEntity avec les en-têtes de réponse
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ajouter une question à un quiz")
    @ApiResponses({

            // status 201 et header location
            @ApiResponse(responseCode = "201", description = "Question ajoutée avec succès", headers = {
                    @io.swagger.v3.oas.annotations.headers.Header(name = "Location", description = "URI de la nouvelle question", schema = @Schema(type = "string"))
            }),
   })
    @PostMapping("/{id}/questions")
    public ResponseEntity<?> addNewQuestion( @AuthenticationPrincipal Jwt jwt, @Valid @PathVariable Long id, @Valid @RequestBody AddNewQuestionDTO question){
        System.out.println("addNewQuestion : " + question);
        Long questionId = quizService.addQuestionToQuiz(id, question);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{questionId}")
                .buildAndExpand(questionId)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        log.info("location : " + location);
        headers.setLocation(location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * Issue7
     * Endpoint sécurisé pour récupérer un quiz.
     * Extrait l'ID utilisateur du token JWT avant de déléguer au service.
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Récupérer un quiz par son ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Quiz non trouvé ou non autorisé", content = @Content(schema = @Schema(implementation = ExceptionsResponseDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ListQuestionsDto> getQuizById(@AuthenticationPrincipal Jwt jwt, @Valid @PathVariable Long id){
        String uid = jwt.getClaim("sub");
        System.out.println("getQuizById : " + id);
        return quizService.getQuizByIdAndUserId(id, uid);
    }

    /**
     * Issue11
     * Endpoint pour modifier une question existante dans un quiz.
     * Transmet les détails de mise à jour au service.
     * Retourne un statut 204 sans contenu en cas de succès.
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Modifier une question ou ses réponses")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Question modifiée avec succès"),
            @ApiResponse(responseCode = "404", description = "Question ou quiz non trouvé", content = @Content(schema = @Schema(implementation = ExceptionsResponseDTO.class)))
    })
    @PutMapping("/{quizId}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuestion(@Valid @PathVariable Long quizId, @Valid @PathVariable Long questionId,
                               @RequestBody @Valid AddNewQuestionDTO updateQuestionDTO) {

        String newTitle = updateQuestionDTO.title();
        quizService.updateQuestion(quizId,questionId,newTitle,updateQuestionDTO.answers());
    }


    /**
     * Issue14
     * Démarre un quiz en générant un code unique et vérifiant sa validité.
     * Génère une URI d'exécution si le quiz est prêt et appartient à l'utilisateur.
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Démarrer un quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quiz démarré avec succès"),
            @ApiResponse(responseCode = "400", description = "Quiz non prêt", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz introuvable ou non autorisé", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{quizId}/start")
    public ResponseEntity<Void> startQuiz(@AuthenticationPrincipal Jwt jwt, @PathVariable Long quizId) {
        String uid = jwt.getClaim("sub");

        Quiz quiz = quizService.getQuizByUserId(uid,quizId);

        if(quiz == null) {
            throw new EntityNotFoundedException("Quizz introuvable ");
        }

        if (!quizService.isQuizReady(quiz)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String executionId = quizService.generateExecutionId();
        quiz.setQuizCode(executionId);

        quizService.saveQuiz(quiz);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/execution/{executionId}")
                .buildAndExpand(executionId)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        System.out.println("location is : " + location);

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }


}
