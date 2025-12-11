package com.nordcodes.aqa.tests;

import com.nordcodes.aqa.client.AppClient;
import com.nordcodes.aqa.config.WireMockConfig;
import com.nordcodes.aqa.mock.ExternalServiceMock;
import com.nordcodes.aqa.utils.TokenGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nordcodes.aqa.config.TestConfig.*;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Тестирование Spring Boot приложения")
@Feature("Эндпоинт /endpoint")
@Tag("e2e")
@DisplayName("E2E тесты приложения: проверка аутентификации и действий пользователя")
public class AppEndpointE2ETest {

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

    // ===================== ПОЗИТИВНЫЕ СЦЕНАРИИ =====================

    @Test
    @Story("Пользователь успешно проходит аутентификацию")
    @DisplayName("LOGIN: внешний сервис подтверждает токен -> приложение возвращает OK")
    void givenValidTokenAndLoginAction_whenExternalServiceReturns200_thenResultOk() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest(token, ACTION_LOGIN);

        // then
        assertOkResponse(response);
        assertEquals(1, ExternalServiceMock.getAuthRequestCount(),
                "Приложение должно отправить один запрос к внешнему сервису /auth");
    }

    @Test
    @Story("Пользователь выполняет действие после успешной аутентификации")
    @DisplayName("ACTION после LOGIN: приложение разрешает действие и возвращает OK")
    void givenValidTokenAfterLogin_whenAction_thenResultOk() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);
        ExternalServiceMock.stubDoAction(HTTP_OK);

        appClient.sendRequest(token, ACTION_LOGIN);

        // when
        Response response = appClient.sendRequest(token, ACTION_ACTION);

        // then
        assertOkResponse(response);
        assertEquals(1, ExternalServiceMock.getDoActionRequestCount(),
                "Приложение должно отправить один запрос к внешнему сервису /doAction");
    }

    @Test
    @Story("Пользователь завершает сессию")
    @DisplayName("LOGOUT после LOGIN: приложение завершает сессию и возвращает OK")
    void givenValidTokenAfterLogin_whenLogout_thenResultOk() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        appClient.sendRequest(token, ACTION_LOGIN);

        // when
        Response response = appClient.sendRequest(token, ACTION_LOGOUT);

        // then
        assertOkResponse(response);
    }

    @Test
    @Story("Пользователь повторно проходит аутентификацию с тем же токеном")
    @DisplayName("Повторный LOGIN: приложение перезаписывает сессию и возвращает OK")
    void givenValidTokenAfterFirstLogin_whenLoginAgain_thenResultOk() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        appClient.sendRequest(token, ACTION_LOGIN);

        // when
        Response response = appClient.sendRequest(token, ACTION_LOGIN);

        // then
        assertOkResponse(response);
        assertEquals(2, ExternalServiceMock.getAuthRequestCount(),
                "Приложение должно дважды обратиться к внешнему сервису /auth");
    }

    // ===================== НЕГАТИВНЫЕ СЦЕНАРИИ =====================

    @Test
    @Story("Внешний сервис отклоняет аутентификацию")
    @DisplayName("LOGIN: внешний сервис возвращает ошибку 400 -> приложение возвращает ERROR")
    void givenValidTokenAndLoginAction_whenExternalServiceReturns400_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_BAD_REQUEST);

        // when
        Response response = appClient.sendRequest(token, ACTION_LOGIN);

        // then
        assertErrorResponse(response);
        assertEquals(1, ExternalServiceMock.getAuthRequestCount(),
                "Приложение должно отправить один запрос к внешнему сервису /auth");
    }

    @Test
    @Story("Пользователь пытается выполнить действие без аутентификации")
    @DisplayName("ACTION без LOGIN: приложение возвращает ERROR и не обращается к внешнему сервису")
    void givenValidTokenWithoutLogin_whenAction_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubDoAction(HTTP_OK);

        // when
        Response response = appClient.sendRequest(token, ACTION_ACTION);

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getDoActionRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /doAction без предварительной аутентификации");
    }

    @Test
    @Story("Пользователь пытается выполнить действие после завершения сессии")
    @DisplayName("ACTION после LOGOUT: приложение возвращает ERROR и не обращается к внешнему сервису")
    void givenTokenAfterLogout_whenAction_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);
        ExternalServiceMock.stubDoAction(HTTP_OK);

        appClient.sendRequest(token, ACTION_LOGIN);
        appClient.sendRequest(token, ACTION_LOGOUT);

        // when
        Response response = appClient.sendRequest(token, ACTION_ACTION);

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getDoActionRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /doAction после LOGOUT");
    }

    @Test
    @Story("Пользователь вводит токен неверной длины")
    @DisplayName("LOGIN с токеном длиной 31 символ -> приложение возвращает ERROR без вызова внешнего сервиса")
    void givenTokenWithInvalidLength_whenLogin_thenResultError() {
        // given
        String invalidToken = TokenGenerator.generateInvalidToken(31);
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest(invalidToken, ACTION_LOGIN);

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /auth при неверной длине токена");
    }

    @Test
    @Story("Пользователь вводит токен с недопустимыми символами")
    @DisplayName("LOGIN с токеном, содержащим строчные буквы -> приложение возвращает ERROR без вызова внешнего сервиса")
    void givenTokenWithInvalidChars_whenLogin_thenResultError() {
        // given
        String invalidToken = TokenGenerator.generateTokenWithInvalidChars();
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest(invalidToken, ACTION_LOGIN);

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /auth при недопустимых символах в токене");
    }

    @Test
    @Story("Пользователь отправляет запрос без обязательного заголовка X-Api-Key")
    @DisplayName("Отправка запроса без X-Api-Key -> приложение возвращает ERROR")
    void givenRequestWithoutApiKey_whenSend_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequestWithoutApiKey(token, ACTION_LOGIN);

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /auth при отсутствии X-Api-Key");
    }

    @Test
    @Story("Пользователь указывает неверное действие")
    @DisplayName("Неверный action (например, 'INVALID') -> приложение возвращает ERROR")
    void givenInvalidAction_whenSend_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest(token, "INVALID");

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /auth при неверном action");
    }

    @Test
    @Story("Пользователь отправляет запрос без параметра token")
    @DisplayName("Отсутствие параметра token -> приложение возвращает ERROR")
    void givenRequestWithoutToken_whenSend_thenResultError() {
        // given
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest("", ACTION_LOGIN);

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /auth при отсутствии token");
    }

    @Test
    @Story("Пользователь отправляет запрос без параметра action")
    @DisplayName("Отсутствие параметра action -> приложение возвращает ERROR")
    void givenRequestWithoutAction_whenSend_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequest(token, "");

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /auth при отсутствии action");
    }

    @Test
    @Story("Пользователь отправляет запрос с неверным Content-Type")
    @DisplayName("Неверный Content-Type (например, application/json) -> приложение возвращает ERROR")
    void givenRequestWithWrongContentType_whenSend_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuth(HTTP_OK);

        // when
        Response response = appClient.sendRequestWithCustomContentType(
                token, ACTION_LOGIN, "application/json");

        // then
        assertErrorResponse(response);
        assertEquals(0, ExternalServiceMock.getAuthRequestCount(),
                "Приложение НЕ должно обращаться к внешнему сервису /auth при неверном Content-Type");
    }

    @Test
    @Story("Внешний сервис отвечает с большой задержкой")
    @DisplayName("LOGIN при таймауте внешнего сервиса -> приложение возвращает ERROR")
    @Disabled("Требует настройки таймаутов в приложении, оставляем как пример")
    void givenExternalServiceTimeout_whenLogin_thenResultError() {
        // given
        String token = TokenGenerator.generateValidToken();
        ExternalServiceMock.stubAuthWithDelay(5000);

        // when
        Response response = appClient.sendRequest(token, ACTION_LOGIN);

        // then
        assertTrue(response.getBody().asString().contains(RESULT_ERROR),
                "Тело ответа должно содержать result: ERROR");
    }
}