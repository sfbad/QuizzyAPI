package com.teamcocoon.QuizzyAPI.dtos;

import java.util.List;
import java.util.Map;

public record ListQuizResponseLinkDto(List<QuizDto> data, Map<String, String> _links){
}