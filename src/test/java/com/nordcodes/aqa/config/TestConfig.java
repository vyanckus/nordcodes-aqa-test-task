package com.nordcodes.aqa.config;

/**
 * Конфигурационные константы для тестов.
 */
public class TestConfig {

    // Тестируемое приложение
    public static final String APP_BASE_URL = "http://localhost:8080";
    public static final String APP_ENDPOINT = "/endpoint";
    public static final String APP_API_KEY = "qazWSXedc";

    // Внешний сервис (мокается WireMock)
    public static final int WIREMOCK_PORT = 8888;
    public static final String WIREMOCK_BASE_URL = "http://localhost:" + WIREMOCK_PORT;
    public static final String MOCK_AUTH_PATH = "/auth";
    public static final String MOCK_DO_ACTION_PATH = "/doAction";

    // Параметры запроса
    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_ACTION = "action";

    // Действия (action values)
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_ACTION = "ACTION";
    public static final String ACTION_LOGOUT = "LOGOUT";

    // Ожидаемые результаты в JSON-ответе
    public static final String RESULT_OK = "OK";
    public static final String RESULT_ERROR = "ERROR";
    public static final String JSON_KEY_RESULT = "result";
    public static final String JSON_KEY_MESSAGE = "message";

    // Заголовки
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_X_API_KEY = "X-Api-Key";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // Токен
    public static final int TOKEN_LENGTH = 32;
    public static final String TOKEN_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String HEX_TOKEN_ALPHABET = "0123456789ABCDEF"; // Верный формат токена, соответствующий "^[0-9A-F]{32}$\"
    public static final String INVALID_TOKEN_ALPHABET = "abcdefghijklmnopqrstuvwxyz!@#$%^&*()";

    // Коды ответов HTTP
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_INTERNAL_ERROR = 500;
}
