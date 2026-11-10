package com.example.projectmanager.common;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.projectmanager.error.ValidationException;
import com.example.projectmanager.task.TaskStatus;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JsonFieldsTest {

    @ParameterizedTest
    @ValueSource(strings = {"todo", "Done", "BLOCKED", ""})
    void parseEnumRejectsWrongCaseAndUnknownValuesListingAllowedOnes(String value) {
        assertThatThrownBy(() -> JsonFields.parseEnum(TaskStatus.class, value, "status"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("status must be one of TODO, IN_PROGRESS, DONE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-02-30", "2026-13-01", "2026-1-5", "10/11/2026", "2026-11-10T12:00:00Z"})
    void dateRejectsImpossibleDatesAndOtherFormats(String value) {
        ObjectNode body = JsonNodeFactory.instance.objectNode().put("dueDate", value);

        assertThatThrownBy(() -> JsonFields.date(body, "dueDate"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("dueDate must be a valid date in yyyy-MM-dd format");
    }
}
