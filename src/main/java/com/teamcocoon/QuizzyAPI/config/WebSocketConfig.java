package com.teamcocoon.QuizzyAPI.config;

import com.teamcocoon.QuizzyAPI.service.QuestionService;
import com.teamcocoon.QuizzyAPI.service.QuizService;
import com.teamcocoon.QuizzyAPI.service.WebSocketHandlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private QuizService quizService;
    @Autowired
    private QuestionService questionService;
    @Bean
    public WebSocketConfigurator webSocketConfigurator() {
        return new WebSocketConfigurator();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandlerImpl(quizService, questionService), "/");
    }
}
