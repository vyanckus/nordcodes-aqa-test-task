package com.nordcodes.aqa.client;

import com.nordcodes.aqa.utils.AllureAttachments;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.nordcodes.aqa.config.TestConfig.*;
import static io.restassured.RestAssured.given;

/**
 * HTTP-клиент для взаимодействия с тестируемым приложением.
 */
public class AppClient {

    private static final Logger log = LoggerFactory.getLogger(AppClient.class);

    static {
        RestAssured.baseURI = APP_BASE_URL;
    }

    /**
     * Отправляет POST-запрос к эндпоинту приложения с обязательным заголовком X-Api-Key.
     *
     * @param token  токен (32 символа A-Z0-9)
     * @param action действие (LOGIN, ACTION, LOGOUT)
     * @return ответ от сервера (RestAssured Response)
     */
    @Step("Отправка запроса к приложению: action = {action}")
    public Response sendRequest(String token, String action) {
        log.info("Отправка запроса: action={}, token={}", action, token);

        Map<String, String> requestParams = Map.of(
                PARAM_TOKEN, token,
                PARAM_ACTION, action
        );

        RequestSpecification request = given()
                .header(HEADER_X_API_KEY, APP_API_KEY)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED)
                .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                .formParams(requestParams);

        logRequest(requestParams);
        Response response = request.post(APP_ENDPOINT);
        logResponse(response);

        return response;
    }

    /**
     * Отправляет POST-запрос к приложению БЕЗ заголовка X-Api-Key.
     * Используется для тестирования валидации обязательного заголовка.
     *
     * @param token  токен
     * @param action действие
     * @return ответ от сервера
     */
    @Step("Отправка запроса БЕЗ заголовка X-Api-Key")
    public Response sendRequestWithoutApiKey(String token, String action) {
        log.info("Отправка запроса без X-Api-Key: action={}, token={}", action, token);

        Map<String, String> requestParams = Map.of(
                PARAM_TOKEN, token,
                PARAM_ACTION, action
        );

        RequestSpecification request = given()
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED)
                .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                .formParams(requestParams);

        logRequest(requestParams);
        Response response = request.post(APP_ENDPOINT);
        logResponse(response);

        return response;
    }

    /**
     * Отправляет POST-запрос к приложению с произвольным Content-Type.
     * Используется для тестирования неверного Content-Type.
     *
     * @param token  токен
     * @param action действие
     * @param contentType Content-Type
     * @return ответ от сервера
     */
    @Step("Отправка запроса с произвольным Content-Type: {contentType}")
    public Response sendRequestWithCustomContentType(String token, String action, String contentType) {
        log.info("Отправка запроса с Content-Type {}: action={}, token={}", contentType, action, token);

        Map<String, String> requestParams = Map.of(
                PARAM_TOKEN, token,
                PARAM_ACTION, action
        );

        RequestSpecification request = given()
                .header(HEADER_X_API_KEY, APP_API_KEY)
                .header(HEADER_CONTENT_TYPE, contentType)
                .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                .formParams(requestParams);

        logRequest(requestParams);
        Response response = request.post(APP_ENDPOINT);
        logResponse(response);

        return response;
    }

    /**
     * Логирует детали HTTP-запроса в Allure-отчёт.
     *
     * @param requestParams параметры запроса
     */
    private void logRequest(Map<String, String> requestParams) {
        String method = "POST";
        String url = APP_BASE_URL + APP_ENDPOINT;
        String headers = String.format("%s: %s%n%s: %s%n%s: %s",
                HEADER_X_API_KEY, APP_API_KEY,
                HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED,
                HEADER_ACCEPT, CONTENT_TYPE_JSON);
        String body = String.format("%s=%s&%s=%s",
                PARAM_TOKEN, requestParams.get(PARAM_TOKEN),
                PARAM_ACTION, requestParams.get(PARAM_ACTION));

        AllureAttachments.attachHttpRequest(method, url, headers, body);
    }

    /**
     * Логирует детали HTTP-ответа в Allure-отчёт.
     *
     * @param response ответ от сервера
     */
    private void logResponse(Response response) {
        int statusCode = response.getStatusCode();
        String headers = response.getHeaders().toString();
        String body = response.getBody().asString();

        AllureAttachments.attachHttpResponse(statusCode, headers, body);
        log.info("Получен ответ: status={}, body={}", statusCode, body);
    }
}