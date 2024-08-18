package com.natthapong.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.TimeZone;

public class JsonHelper {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private JsonHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T jsonStringToObject(String jsonStringValue, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStringValue, clazz);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    public static String objectToJsonString(Object object) {

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    public static String objectToJsonString(Object object, FilterProvider filterId) {

        try {
            return objectMapper.writer(filterId).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    public static <T> T jsonStringToObjectTypeRef(String jsonStringValue, TypeReference<T> typeReference) {

        try {
            return objectMapper.readValue(jsonStringValue, typeReference);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    public static <T> T objectToObject(Object object, Class<T> clazz) {


        try {
            String value = object instanceof String ? object.toString() : objectMapper.writeValueAsString(object);
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    public static <T> T objectToObjectTypeRef(Object object, TypeReference<T> valueTypeRef) {


        try {
            String value = object instanceof String ? object.toString() : objectMapper.writeValueAsString(object);
            return objectMapper.readValue(value, valueTypeRef);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setTimeZone(TimeZone.getDefault());
    }
}

