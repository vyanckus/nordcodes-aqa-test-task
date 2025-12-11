package com.nordcodes.aqa.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

import static com.nordcodes.aqa.config.TestConfig.*;

/**
 * Утилита для генерации тестовых токенов.
 * Токен должен быть строкой длиной 32 символа, состоящей только из A-Z0-9.
 */
public class TokenGenerator {

    private static final Logger log = LoggerFactory.getLogger(TokenGenerator.class);
    private static final SecureRandom random = new SecureRandom();

    /**
     * Генерирует токен заданной длины из заданного алфавита.
     *
     * @param alphabet строка допустимых символов
     * @param length длина токена
     * @return сгенерированный токен
     */
    private static String generateToken(String alphabet, int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            token.append(alphabet.charAt(index));
        }
        return token.toString();
    }

    /**
     * Генерирует валидный токен длиной 32 символа (A-Z0-9).
     *
     * @return сгенерированный токен
     */
    public static String generateValidToken() {
        String token = generateToken(TOKEN_ALPHABET, TOKEN_LENGTH);
        log.debug("Сгенерирован валидный токен: {}", token);
        return token;
    }

    /**
     * Генерирует невалидный токен (длина не равна 32).
     *
     * @param length желаемая длина токена
     * @return сгенерированный токен
     */
    public static String generateInvalidToken(int length) {
        if (length == TOKEN_LENGTH) {
            log.warn("Запрошена генерация невалидного токена длиной 32 символа. Это может быть ошибкой.");
        }
        String token = generateToken(TOKEN_ALPHABET, length);
        log.debug("Сгенерирован невалидный токен (длина {}): {}", length, token);
        return token;
    }

    /**
     * Генерирует токен с недопустимыми символами (не A-Z0-9).
     * @return сгенерированный токен
     */
    public static String generateTokenWithInvalidChars() {
        String token = generateToken(INVALID_TOKEN_ALPHABET, TOKEN_LENGTH);
        log.debug("Сгенерирован токен с недопустимыми символами: {}", token);
        return token;
    }

    /**
     * Генерирует валидный HEX-токен длиной 32 символа (0-9A-F).
     * Соответствует фактическому ожидаемому формату приложения.
     *
     * @return сгенерированный HEX-токен
     */
    public static String generateHexToken() {
        String token = generateToken(HEX_TOKEN_ALPHABET, TOKEN_LENGTH);
        log.debug("Сгенерирован HEX-токен: {}", token);
        return token;
    }
}