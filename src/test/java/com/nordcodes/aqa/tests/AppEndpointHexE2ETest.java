package com.nordcodes.aqa.tests;

import com.nordcodes.aqa.client.AppClient;
import com.nordcodes.aqa.config.WireMockConfig;
import com.nordcodes.aqa.mock.ExternalServiceMock;
import com.nordcodes.aqa.utils.TokenGenerator;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nordcodes.aqa.config.TestConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Тестирование Spring Boot приложения")
@Feature("Эндпоинт /endpoint (HEX-токены)")
@Tag("hex")
@DisplayName("E2E тесты приложения с корректными HEX-токенами")
public class AppEndpointHexE2ETest {
    private static final Logger log = LoggerFactory.getLogger(AppEndpointE2ETest.class);
    private static AppClient appClient;

    @BeforeAll
    static void setUpAll() {
        log.info("Инициализация тестового окружения");
        WireMockConfig.startWireMock();
        appClient = new AppClient();
    }

    @AfterAll
    static void tearDownAll() {
        log.info("Завершение тестового окружения");
        WireMockConfig.stopWireMock();
    }

    @BeforeEach
    void setUp() {
        log.info("Сброс стабов перед тестом");
        ExternalServiceMock.resetStubs();
    }

    // ===================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ ПРОВЕРОК =====================
    /**
     * Проверяет успешный ответ: статус 200 и result: OK.
     */
    private void assertOkResponse(Response response) {
        assertEquals(HTTP_OK, response.getStatusCode(),
                "Статус-код ответа должен быть 200");
        assertTrue(response.getBody().asString().contains(RESULT_OK),
                "Тело ответа должно содержать result: OK");
    }

    /**
     * Проверяет ответ с ошибкой result: ERROR.
     */
    private void assertErrorResponse(Response response) {
        assertTrue(response.getBody().asString().contains(RESULT_ERROR),
                "Тело ответа должно содержать result: ERROR");
    }

    // ===================== ТЕСТ, ВЫЯВЛЯЮЩИЙ РАСХОЖДЕНИЕ ТЗ И РЕАЛИЗАЦИИ =====================

    @Test
    @Story("Расхождение ТЗ и реализации")
    @DisplayName("LOGIN с токеном по ТЗ (A-Z0-9) выявляет ожидание HEX-формата (0-9A-F)")
    void givenTokenAccordingToSpec_whenLogin_thenResultError() {
        // given
        String tokenBySpec = TokenGenerator.generateValidToken(); // Токен по ТЗ (A-Z0-9)
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest(tokenBySpec, ACTION_LOGIN);
        String responseBody = response.getBody().asString();

        // then
        assertEquals(HTTP_BAD_REQUEST, response.getStatusCode(),
                "Приложение возвращает 400 на токен формата A-Z0-9 из ТЗ");
        assertTrue(responseBody.contains(RESULT_ERROR),
                "Тело ответа должно содержать result: ERROR");
        // Логируем сообщение от приложения, где видно ожидаемый формат
        log.info("Сообщение об ошибке от приложения: {}", responseBody);
    }

    // ===================== HEX-ТЕСТЫ (проверка работы приложения с корректным HEX-токеном) =====================

    @Test
    @Story("Успешная аутентификация с HEX-токеном")
    @DisplayName("[HEX] Успешный LOGIN с токеном 0-9A-F")
    void givenHexToken_whenLogin_thenResultOk() {
        // given
        String hexToken = TokenGenerator.generateHexToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest(hexToken, ACTION_LOGIN);

        // then
        assertOkResponse(response);
        assertEquals(1, ExternalServiceMock.getAuthRequestCount(),
                "Приложение должно отправить один запрос к внешнему сервису /auth");
    }

    @Test
    @Story("Действие после успешной аутентификации с HEX-токеном")
    @DisplayName("[HEX] Успешный ACTION после LOGIN")
    void givenHexTokenAfterLogin_whenAction_thenResultOk() {
        // given
        String hexToken = TokenGenerator.generateHexToken();
        ExternalServiceMock.stubAuth(HTTP_OK);
        ExternalServiceMock.stubDoAction(HTTP_OK);
        appClient.sendRequest(hexToken, ACTION_LOGIN);

        // when
        Response response = appClient.sendRequest(hexToken, ACTION_ACTION);

        // then
        assertOkResponse(response);
        assertEquals(1, ExternalServiceMock.getDoActionRequestCount(),
                "Приложение должно отправить один запрос к внешнему сервису /doAction");
    }

    @Test
    @Story("Завершение сессии с HEX-токеном")
    @DisplayName("[HEX] Успешный LOGOUT после LOGIN")
    void givenHexTokenAfterLogin_whenLogout_thenResultOk() {
        // given
        String hexToken = TokenGenerator.generateHexToken();
        ExternalServiceMock.stubAuth(HTTP_OK);
        appClient.sendRequest(hexToken, ACTION_LOGIN);

        // when
        Response response = appClient.sendRequest(hexToken, ACTION_LOGOUT);

        // then
        assertOkResponse(response);
    }

    @Test
    @Story("Повторная аутентификация с HEX-токеном")
    @DisplayName("[HEX] Повторный LOGIN с тем же токеном -> ошибка 409 (Conflict, без второго вызова /auth)")
    void givenHexTokenAfterFirstLogin_whenLoginAgain_thenResultError() {
        // given
        String hexToken = TokenGenerator.generateHexToken();
        ExternalServiceMock.stubAuth(HTTP_OK);
        appClient.sendRequest(hexToken, ACTION_LOGIN);
        // when
        Response response = appClient.sendRequest(hexToken, ACTION_LOGIN);

        // then
        assertEquals(HTTP_CONFLICT, response.getStatusCode(),
                "Приложение должно возвращать 409 Conflict при повторном LOGIN с активным токеном");
        assertErrorResponse(response);
        assertEquals(1, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно повторно обращаться к внешнему сервису /auth, если токен уже аутентифицирован");
    }

    @Test
    @Story("Внешний сервис отклоняет аутентификацию HEX-токена")
    @DisplayName("[HEX] LOGIN: внешний сервис возвращает 400 -> приложение возвращает ERROR")
    void givenValidHexToken_whenExternalServiceReturns400_thenResultError() {
        // given
        String hexToken = TokenGenerator.generateHexToken();
        ExternalServiceMock.stubAuth(HTTP_BAD_REQUEST);

        // when
        Response response = appClient.sendRequest(hexToken, ACTION_LOGIN);

        // then
        assertErrorResponse(response);
        assertEquals(1, ExternalServiceMock.getAuthRequestCount(),
                "Приложение должно обратиться к внешнему сервису, т.к. токен валиден (HEX)");
    }

    @Test
    @Story("Внешний сервис недоступен для HEX-токена")
    @DisplayName("[HEX] LOGIN: внешний сервис возвращает 500 -> приложение возвращает ERROR")
    void givenValidHexToken_whenExternalServiceReturns500_thenResultError() {
        // given
        String hexToken = TokenGenerator.generateHexToken();
        ExternalServiceMock.stubAuth(HTTP_INTERNAL_ERROR);

        // when
        Response response = appClient.sendRequest(hexToken, ACTION_LOGIN);

        // then
        assertErrorResponse(response);
        assertEquals(1, ExternalServiceMock.getAuthRequestCount(),
                "Приложение должно обратиться к внешнему сервису, т.к. токен валиден (HEX)");
    }
}
