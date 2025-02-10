package com.teamcocoon.QuizzyAPI.utils;

import java.security.SecureRandom;

public class RandomIDGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 6;

    public static String generateIDFromUsername(String username) {
        if (username == null || username.length() < 2) {
            throw new IllegalArgumentException("Le nom d'utilisateur doit comporter au moins 2 caractères.");
        }

        String prefix = username.substring(0, 2).toUpperCase();

        SecureRandom random = new SecureRandom();
        StringBuilder randomID = new StringBuilder(prefix);

        for (int i = 0; i < ID_LENGTH - 2; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            randomID.append(CHARACTERS.charAt(randomIndex));
        }

        return randomID.toString();
    }

}
