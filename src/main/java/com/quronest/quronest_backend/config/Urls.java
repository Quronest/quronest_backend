package com.quronest.quronest_backend.config;

public class Urls {

    public static final String API_BASE_URL = "/api/v1";

    /* websocket path */
    public static final String WEBSOCKET_PATH = "/ws";

    /* Public URLs */
    public static final String PUBLIC_STATIC_FILES = "/static/**";
    public static final String PUBLIC_AUTH_CONTROLLER = API_BASE_URL + "/auth";

    /* websocket path */
    public static final String WEB_SOCKET_PATH = "/ws";

    /* swagger paths */
    public static final String PUBLIC_SWAGGER_BASE_PATH = "/swagger-ui.html";
    public static final String PUBLIC_SWAGGER_PATH = "/swagger-ui/**";
    public static final String PUBLIC_SWAGGER_DATA_PATH = "/v3/api-docs/**";

    public static final String[] ALL_PUBLIC_URLS = new String[]{
            PUBLIC_STATIC_FILES,
            PUBLIC_AUTH_CONTROLLER + "/**",
            PUBLIC_SWAGGER_BASE_PATH,
            PUBLIC_SWAGGER_PATH,
            PUBLIC_SWAGGER_DATA_PATH,
            WEB_SOCKET_PATH
    };
}
