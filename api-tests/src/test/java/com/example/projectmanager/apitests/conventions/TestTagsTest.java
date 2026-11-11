package com.example.projectmanager.apitests.conventions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.ReflectionSupport;

@Tag("unit")
class TestTagsTest {

    private static final String BASE_PACKAGE = "com.example.projectmanager.apitests";

    @Test
    @DisplayName("every test class has a @Tag so it belongs to a suite")
    void everyTestClassIsTagged() {
        List<String> untagged =
                ReflectionSupport.findAllClassesInPackage(BASE_PACKAGE, TestTagsTest::hasTests, name -> true).stream()
                        .filter(type -> !isTagged(type))
                        .map(Class::getName)
                        .toList();

        assertThat("Test classes missing a @Tag (pick a suite tag, e.g. @Tag(\"smoke\"))", untagged, empty());
    }

    private static boolean hasTests(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .anyMatch(method -> AnnotationSupport.isAnnotated(method, Testable.class));
    }

    private static boolean isTagged(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getEnclosingClass()) {
            if (!AnnotationSupport.findRepeatableAnnotations(current, Tag.class).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
