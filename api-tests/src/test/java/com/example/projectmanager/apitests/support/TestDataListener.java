package com.example.projectmanager.apitests.support;

import com.example.projectmanager.apitests.config.Endpoints;
import com.example.projectmanager.apitests.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registered through META-INF/services, so it runs once per test run without any annotation. Outside CI it first
 * deletes test projects left behind by interrupted runs, then warns when the target holds other projects. When the
 * run finishes it deletes every project the run created through {@link ApiClient}. Deleting a project also deletes
 * its tasks.
 */
public class TestDataListener implements TestExecutionListener {

    /** Every project created by the tests must start with this, so leftovers can be found and deleted. */
    public static final String TEST_NAME_PREFIX = "[api-test] ";

    // Younger test projects may belong to another run that is still going
    private static final Duration LEFTOVER_AGE = Duration.ofHours(1);

    private static final Logger log = LoggerFactory.getLogger(TestDataListener.class);
    private static final Set<String> createdProjectIds = ConcurrentHashMap.newKeySet();

    static void track(String projectId) {
        createdProjectIds.add(projectId);
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        if (System.getenv("CI") != null) {
            return;
        }
        try {
            List<Map<String, Object>> projects =
                    request().get(Endpoints.PROJECTS).jsonPath().getList("$");
            deleteLeftovers(projects);
            warnAboutExistingData(projects);
        } catch (RuntimeException e) {
            // Target unreachable or not this API: the tests themselves will report that.
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        List<String> ids = List.copyOf(createdProjectIds);
        createdProjectIds.removeAll(ids);
        deleteProjects(ids, "created by this run");
    }

    private static void deleteLeftovers(List<Map<String, Object>> projects) {
        Instant cutoff = Instant.now().minus(LEFTOVER_AGE);
        List<String> leftoverIds = projects.stream()
                .filter(TestDataListener::isTestProject)
                .filter(project ->
                        Instant.parse((String) project.get("createdAt")).isBefore(cutoff))
                .map(project -> (String) project.get("id"))
                .toList();
        deleteProjects(leftoverIds, "left over from earlier runs");
    }

    private static void warnAboutExistingData(List<Map<String, Object>> projects) {
        long existing =
                projects.stream().filter(project -> !isTestProject(project)).count();
        if (existing > 0) {
            log.warn(
                    "Target {} already has {} project(s) not created by API tests. API tests add their own projects"
                            + " (named \"{}...\") and delete them when the run finishes. For an isolated run, start"
                            + " the app with DB_PATH=/tmp/pm-api-tests.db ./mvnw spring-boot:run",
                    TestConfig.baseUrl(),
                    existing,
                    TEST_NAME_PREFIX);
        }
    }

    private static boolean isTestProject(Map<String, Object> project) {
        String name = (String) project.get("name");
        return name != null && name.regionMatches(true, 0, TEST_NAME_PREFIX, 0, TEST_NAME_PREFIX.length());
    }

    private static void deleteProjects(List<String> ids, String description) {
        if (ids.isEmpty()) {
            return;
        }
        int failed = 0;
        for (String id : ids) {
            try {
                int status = request().delete(Endpoints.PROJECT_BY_ID, id).statusCode();
                if (status != 204 && status != 404) {
                    failed++;
                }
            } catch (RuntimeException e) {
                failed++;
            }
        }
        if (failed > 0) {
            log.warn("Could not delete {} of {} project(s) {}", failed, ids.size(), description);
        } else {
            log.info("Deleted {} project(s) {}", ids.size(), description);
        }
    }

    private static RequestSpecification request() {
        return RestAssured.given().baseUri(TestConfig.baseUrl()).accept(ContentType.JSON);
    }
}
