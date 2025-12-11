package com.nordcodes.aqa.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Утилита для добавления аттачментов в отчёт Allure.
 */
public class AllureAttachments {

    private static final Logger log = LoggerFactory.getLogger(AllureAttachments.class);

    /**
     * Добавляет текстовый аттачмент в отчёт Allure.
     *
     * @param name название аттачмента
     * @param content  содержимое аттачмента
     */
    @Attachment(value = "{name}", type = "text/plain")
    public static String attachText(String name, String content) {
        log.debug("Добавлен текстовый аттачмент: {}", name);
        return content;
    }

    /**
     * Добавляет JSON-аттачмент в отчёт Allure.
     *
     * @param name название аттачмента
     * @param json содержимое JSON (строка)
     */
    @Attachment(value = "{name}", type = "application/json")
    public static String attachJson(String name, String json) {
        log.debug("Добавлен JSON-аттачмент: {}", name);
        return json;
    }

    /**
     * Добавляет HTTP-запрос в отчёт Allure.
     * Используется в клиенте для логирования отправленных запросов.
     *
     * @param method HTTP-метод (GET, POST и т.д.)
     * @param url полный URL запроса
     * @param headers заголовки запроса (в виде строки)
     * @param body тело запроса (если есть)
     */
    public static void attachHttpRequest(String method, String url, String headers, String body) {
        String requestLog = String.format("%s %s%n%nHeaders:%n%s%n%nBody:%n%s",
                method, url, headers, body);
        attachText("HTTP Request", requestLog);
    }

    /**
     * Добавляет HTTP-ответ в отчёт Allure.
     *
     * @param statusCode статус-код ответа
     * @param headers заголовки ответа
     * @param body тело ответа
     */
    public static void attachHttpResponse(int statusCode, String headers, String body) {
        String responseLog = String.format("Status Code: %d%n%nHeaders:%n%s%n%nBody:%n%s",
                statusCode, headers, body);
        attachText("HTTP Response", responseLog);
    }

    /**
     * Добавляет произвольный файл в отчёт Allure (например, скриншот, лог).
     *
     * @param name название файла
     * @param content содержимое файла в виде байтов
     */
    public static void attachFile(String name, byte[] content) {
        Allure.addAttachment(name, "application/octet-stream",
                new ByteArrayInputStream(content), ".bin");
        log.debug("Добавлен файл-аттачмент: {}", name);
    }

    /**
     * Добавляет логи в отчёт Allure.
     *
     * @param logs строки логов
     */
    public static void attachLogs(String logs) {
        attachText("Test Logs", logs);
    }
}