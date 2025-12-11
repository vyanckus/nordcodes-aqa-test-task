package com.nordcodes.aqa.mock;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.nordcodes.aqa.config.TestConfig.*;

/**
 * Класс для настройки стабов WireMock внешнего сервиса.
 * Эмулирует эндпоинты /auth и /doAction, которые вызываются тестируемым приложением.
 */
public class ExternalServiceMock {

    private static final Logger log = LoggerFactory.getLogger(ExternalServiceMock.class);

    /**
     * Настраивает стаб для /auth с заданным статусом.
     * Тело ответа - пустой JSON {} (по ТЗ приложение смотрит только на код).
     */
    public static void stubAuth(int statusCode) {
        log.info("Настройка стаба: /auth -> {}", statusCode);
        stubFor(post(urlEqualTo(MOCK_AUTH_PATH))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody("{}")));
    }

    /**
     * Настраивает стаб для /doAction с заданным статусом.
     */
    public static void stubDoAction(int statusCode) {
        log.info("Настройка стаба: /doAction -> {}", statusCode);
        stubFor(post(urlEqualTo(MOCK_DO_ACTION_PATH))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody("{}")));
    }

    /**
     * Настраивает стаб для /auth с задержкой ответа (для тестирования таймаутов).
     */
    public static void stubAuthWithDelay(int delayMillis) {
        log.info("Настройка стаба: /auth -> 200 OK с задержкой {} мс", delayMillis);
        stubFor(post(urlEqualTo(MOCK_AUTH_PATH))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withFixedDelay(delayMillis)
                        .withHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody("{}")));
    }

    /**
     * Удаляет все стабы WireMock (очистка перед тестом).
     */
    public static void resetStubs() {
        log.info("Сброс всех стабов WireMock");
        WireMock.reset();
    }

    /**
     * Возвращает количество запросов, сделанных к /auth.
     */
    public static int getAuthRequestCount() {
        int count = findAll(postRequestedFor(urlEqualTo(MOCK_AUTH_PATH))).size();
        log.debug("Количество запросов к /auth: {}", count);
        return count;
    }

    /**
     * Возвращает количество запросов, сделанных к /doAction.
     */
    public static int getDoActionRequestCount() {
        int count = findAll(postRequestedFor(urlEqualTo(MOCK_DO_ACTION_PATH))).size();
        log.debug("Количество запросов к /doAction: {}", count);
        return count;
    }
}