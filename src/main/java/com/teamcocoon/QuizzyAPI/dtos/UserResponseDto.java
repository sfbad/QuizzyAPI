package com.teamcocoon.QuizzyAPI.dtos;

public record UserResponseDto(
        String uid,
        String email,
        String username
) {}