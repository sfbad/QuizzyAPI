package com.teamcocoon.QuizzyAPI.dtos;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

public class ListQuizResponseLinkDto extends RepresentationModel<ListQuizResponseLinkDto> {
    private final List<QuizDto> data;

    public ListQuizResponseLinkDto(List<QuizDto> data) {
        this.data = data;
    }

    public List<QuizDto> getData() {
        return data;
    }
}
