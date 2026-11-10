package com.example.projectmanager.project;

import com.example.projectmanager.common.JsonFields;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A partial update. The {@code has*} flags distinguish an omitted field from one sent as null,
 * so {@code "description": null} clears the description while omitting it leaves it unchanged.
 */
public record ProjectPatch(boolean hasName, String name, boolean hasDescription, String description) {

    public static ProjectPatch fromJson(JsonNode body) {
        JsonFields.requireObject(body);
        return new ProjectPatch(
                body.has("name"), JsonFields.text(body, "name"),
                body.has("description"), JsonFields.text(body, "description"));
    }

    public boolean isEmpty() {
        return !hasName && !hasDescription;
    }
}
