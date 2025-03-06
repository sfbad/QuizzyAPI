package com.teamcocoon.QuizzyAPI.controller;

import com.teamcocoon.QuizzyAPI.dtos.UserRequestDto;
import com.teamcocoon.QuizzyAPI.dtos.UserResponseDto;
import com.teamcocoon.QuizzyAPI.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Endpoints pour les utilisateurs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Enregistrer un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur enregistré")
    })
    @PostMapping()
    public ResponseEntity<Void> registerUser(
            @AuthenticationPrincipal Jwt jwt,
           @Valid @RequestBody UserRequestDto userRequestDto
            ) {
        String uid = jwt.getClaim("sub");
        String email = jwt.getClaim("email");
        String username = userRequestDto.username();
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        userService.registerUser(uid, username,email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Récupérer les données de l'utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Données de l'utilisateur récupérées"),
            @ApiResponse(responseCode = "401", description = "Non autorisé")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUserData(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUserData(jwt));
    }
}
