package com.nordcodes.aqa.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.nordcodes.aqa.config.TestConfig.WIREMOCK_BASE_URL;
import static com.nordcodes.aqa.config.TestConfig.WIREMOCK_PORT;

/**
 * Конфигурация и управление WireMock сервером.
 * Обеспечивает запуск и остановку мока внешнего сервиса на порту 8888.
 */
public class WireMockConfig {

    private static final Logger log = LoggerFactory.getLogger(WireMockConfig.class);
    private static WireMockServer wireMockServer;


    /**
     * Запускает WireMock сервер, если он ещё не запущен.
     */
    public static void startWireMock() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            log.info("Запуск WireMock сервера на порту {}", WIREMOCK_PORT);
            WireMockConfiguration config = wireMockConfig().port(WIREMOCK_PORT);
            wireMockServer = new WireMockServer(config);
            wireMockServer.start();
            WireMock.configureFor("localhost", WIREMOCK_PORT);
            log.info("WireMock сервер запущен на {}", wireMockServer.baseUrl());
        }
    }

    /**
     * Останавливает WireMock сервер, если он запущен.
     */
    public static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            log.info("Остановка WireMock сервера");
            wireMockServer.stop();
            log.info("WireMock сервер остановлен");
        }
    }

    /**
     * Возвращает экземпляр WireMock сервера.
     *
     * @return экземпляр WireMockServer
     */
    public static WireMockServer getWireMockServer() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            throw new IllegalStateException("WireMock сервер не запущен. Вызовите сначала startWireMock()");
        }
        return wireMockServer;
    }

    /**
     * Возвращает базовый URL запущенного WireMock сервера.
     *
     * @return базовый URL
     */
    public static String getWireMockBaseUrl() {
        return WIREMOCK_BASE_URL;
    }
}