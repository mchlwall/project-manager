package com.example.projectmanager.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.example.projectmanager.error.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;

public final class JsonFields {

    // STRICT rejects impossible dates such as 2026-02-30 instead of rolling them over
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private JsonFields() {
    }

    public static void requireObject(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("request body must be a JSON object");
        }
    }

    /** The field's string value, or null when absent or JSON null. */
    public static String text(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw new ValidationException(field + " must be a string");
        }
        return value.asText();
    }

    /** The field's date value, or null when absent or JSON null. */
    public static LocalDate date(JsonNode body, String field) {
        String value = text(body, field);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field + " must be a valid date in yyyy-MM-dd format");
        }
    }

    /** Exact, case-sensitive match on the constant name; null is rejected. */
    public static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equals(value)) {
                return constant;
            }
        }
        String allowed = Arrays.stream(type.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
        throw new ValidationException(field + " must be one of " + allowed);
    }

    /** The field's enum value, or null when absent. A present non-matching value (including null) is rejected. */
    public static <E extends Enum<E>> E enumValue(JsonNode body, String field, Class<E> type) {
        JsonNode value = body.get(field);
        if (value == null) {
            return null;
        }
        return parseEnum(type, value.isTextual() ? value.asText() : null, field);
    }
}
