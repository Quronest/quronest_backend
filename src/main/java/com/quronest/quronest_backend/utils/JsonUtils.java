package com.quronest.quronest_backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


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
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON using Jackson", e);
        }
    }

    public static <T> T fromJsonUsingGson(String jsonString, Class<T> t) {
        try {
            return objectMapper.readValue(jsonString, t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize from JSON using Jackson", e);
        }
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

    public static <T> T fromJsonNode(JsonNode jsonNode, Class<T> t) {
        try {
            if (jsonNode == null || jsonNode.isNull()) {
                return null;
            }
            return objectMapper.treeToValue(jsonNode, t);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert JsonNode to " + t.getName(), e);
        }
    }

    public static <T> JsonNode toJsonNode(T obj) {
        return objectMapper.valueToTree(obj);
    }

    public static JsonNode readTree(String jsonString) throws JsonProcessingException {
        return objectMapper.readTree(jsonString);
    }
}

