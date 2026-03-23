package com.quronest.quronest_backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Gson gson = new Gson();

    public static <T> T fromJson(String jsonString, Class<T> t) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, t);
    }

    public static <T> T fromJson(String jsonString, TypeReference<T> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, typeReference);
    }

    public static <T> String toJson(T obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> String toBase64UrlEncoded(T obj) throws JsonProcessingException {
        String objJson = toJson(obj);
        String base64 = Base64.getEncoder().encodeToString(objJson.getBytes());
        return URLEncoder.encode(base64, StandardCharsets.UTF_8);
    }

    public static <T> T fromBase64UrlEncoded(String encodedString, Class<T> t) throws IOException {
        String base64 = URLDecoder.decode(encodedString, StandardCharsets.UTF_8);
        return objectMapper.readValue(Base64.getDecoder().decode(base64), t);
    }

    public static <T> String toJsonUsingGson(T obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJsonUsingGson(String jsonString, Class<T> t) {
        return gson.fromJson(jsonString, t);
    }

    public static <T> List<T> fromJsonToList(String jsonString, Class<T> t) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, objectMapper.getTypeFactory().constructCollectionType(List.class, t));
    }

    public static <T> List<T> fromBase64UrlEncodedToList(String encodedString, Class<T> t) throws IOException {
        String decodedJson = new String(Base64.getDecoder().decode(encodedString), StandardCharsets.UTF_8);
        return fromJsonToList(decodedJson, t);
    }

    public static <T> T fromBase64EncodedToObject(String encodedString, Class<T> t) throws IOException {
        String decodedJson = new String(Base64.getDecoder().decode(encodedString), StandardCharsets.UTF_8);
        return fromJson(decodedJson, t);
    }
}

