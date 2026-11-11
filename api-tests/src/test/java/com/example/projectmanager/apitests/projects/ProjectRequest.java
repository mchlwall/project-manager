package com.example.projectmanager.apitests.projects;

import com.example.projectmanager.apitests.support.TestDataListener;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

/** Null fields are left out of the JSON body, so a null name tests a missing name. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectRequest(String name, String description) {

    public static ProjectRequest valid() {
        return named(uniqueName());
    }

    public static ProjectRequest named(String name) {
        return new ProjectRequest(name, "Created by API tests");
    }

    public static String uniqueName() {
        return TestDataListener.TEST_NAME_PREFIX + UUID.randomUUID();
    }
}
