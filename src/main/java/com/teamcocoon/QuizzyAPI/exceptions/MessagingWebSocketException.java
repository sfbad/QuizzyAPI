package com.teamcocoon.QuizzyAPI.exceptions;

import org.springframework.messaging.MessagingException;

public class MessagingWebSocketException extends MessagingException {
    public MessagingWebSocketException(String message) {
        super(message);
    }
}
