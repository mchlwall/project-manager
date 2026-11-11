package com.example.projectmanager.apitests.projects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.example.projectmanager.apitests.config.Endpoints;
import com.example.projectmanager.apitests.support.ApiClient;
import com.example.projectmanager.apitests.support.ResponseLoggingExtension;
import com.example.projectmanager.apitests.support.TestTimeouts;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("projects")
@ExtendWith(ResponseLoggingExtension.class)
class ProjectsTests {

    private static final String TIMESTAMP = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
    private static final String UNKNOWN_ID = "00000000-0000-0000-0000-000000000000";
    private static final ApiClient apiClient = new ApiClient();

    private static Response createProject(ProjectRequest request) {
        Response response = apiClient.post(Endpoints.PROJECTS, request);
        assertThat(response.statusCode(), is(201));
        return response;
    }

    private static String nameOfLength(int length) {
        String unique = ProjectRequest.uniqueName();
        return unique + "x".repeat(length - unique.length());
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects creates a project and returns 201 with its location")
    void createProjectReturns201() {
        ProjectRequest request = ProjectRequest.valid();

        Response response = apiClient.post(Endpoints.PROJECTS, request);

        assertThat(response.statusCode(), is(201));
        String id = response.path("id");
        assertThat(id, not(emptyOrNullString()));
        assertThat(response.header("Location"), endsWith("/api/projects/" + id));
        assertThat(response.path("name"), is(request.name()));
        assertThat(response.path("description"), is(request.description()));
        assertThat(response.path("createdAt"), matchesPattern(TIMESTAMP));
        assertThat(response.path("updatedAt"), is(response.<String>path("createdAt")));
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects trims surrounding whitespace from the name")
    void createTrimsName() {
        String name = ProjectRequest.uniqueName();

        Response response = apiClient.post(Endpoints.PROJECTS, ProjectRequest.named("  " + name + "  "));

        assertThat(response.statusCode(), is(201));
        assertThat(response.path("name"), is(name));
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects without a description stores it as null")
    void createWithoutDescription() {
        Response response = apiClient.post(Endpoints.PROJECTS, new ProjectRequest(ProjectRequest.uniqueName(), null));

        assertThat(response.statusCode(), is(201));
        assertThat(response.path("description"), is(nullValue()));
    }

    @ParameterizedTest(name = "name = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects rejects a missing or blank name with 400")
    void blankNameRejected(String name) {
        Response response = apiClient.post(Endpoints.PROJECTS, ProjectRequest.named(name));

        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error"), is("name must not be blank"));
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects accepts a name of exactly 100 characters")
    void maxLengthNameAccepted() {
        String name = nameOfLength(100);

        Response response = apiClient.post(Endpoints.PROJECTS, ProjectRequest.named(name));

        assertThat(response.statusCode(), is(201));
        assertThat(response.path("name"), is(name));
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects rejects a name over 100 characters with 400")
    void overLongNameRejected() {
        Response response = apiClient.post(Endpoints.PROJECTS, ProjectRequest.named(nameOfLength(101)));

        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error"), is("name must be at most 100 characters"));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects rejects a name that differs from an existing one only by case with 400")
    void duplicateNameRejectedIgnoringCase() {
        String name = createProject(ProjectRequest.valid()).path("name");

        Response response = apiClient.post(Endpoints.PROJECTS, ProjectRequest.named(name.toUpperCase()));

        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error"), is("project name already exists"));
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("POST /api/projects rejects malformed JSON with 400")
    void malformedJsonRejected() {
        Response response = apiClient.post(Endpoints.PROJECTS, "{this is not valid json");

        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error"), is("request body is missing or is not valid JSON"));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("GET /api/projects/{id} returns the project as it was created")
    void getProjectById() {
        Response created = createProject(ProjectRequest.valid());
        String id = created.path("id");

        Response response = apiClient.get(Endpoints.PROJECT_BY_ID, id);

        assertThat(response.statusCode(), is(200));
        assertThat(response.path("id"), is(id));
        assertThat(response.path("name"), is(created.<String>path("name")));
        assertThat(response.path("description"), is(created.<String>path("description")));
        assertThat(response.path("createdAt"), is(created.<String>path("createdAt")));
        assertThat(response.path("updatedAt"), is(created.<String>path("updatedAt")));
    }

    @Test
    @Timeout(value = TestTimeouts.S, unit = TimeUnit.SECONDS)
    @DisplayName("GET /api/projects/{id} returns 404 for an unknown id")
    void getUnknownProjectReturns404() {
        Response response = apiClient.get(Endpoints.PROJECT_BY_ID, UNKNOWN_ID);

        assertThat(response.statusCode(), is(404));
        assertThat(response.path("error"), is("project not found"));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("GET /api/projects includes a newly created project")
    void listIncludesCreatedProject() {
        String id = createProject(ProjectRequest.valid()).path("id");

        Response response = apiClient.get(Endpoints.PROJECTS);

        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getList("id", String.class), hasItem(id));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("PATCH /api/projects/{id} renames the project, keeps createdAt and moves updatedAt forward")
    void renameProject() {
        Response created = createProject(ProjectRequest.valid());
        String newName = ProjectRequest.uniqueName();

        Response response =
                apiClient.patch(Endpoints.PROJECT_BY_ID, Map.of("name", newName), created.<String>path("id"));

        assertThat(response.statusCode(), is(200));
        assertThat(response.path("name"), is(newName));
        assertThat(response.path("description"), is(created.<String>path("description")));
        assertThat(response.path("createdAt"), is(created.<String>path("createdAt")));
        assertThat(Instant.parse(response.path("updatedAt")), greaterThan(Instant.parse(created.path("updatedAt"))));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("PATCH /api/projects/{id} with a null description clears it and keeps the name")
    void nullDescriptionClearsIt() {
        Response created = createProject(ProjectRequest.valid());

        Response response = apiClient.patch(
                Endpoints.PROJECT_BY_ID, Collections.singletonMap("description", null), created.<String>path("id"));

        assertThat(response.statusCode(), is(200));
        assertThat(response.path("description"), is(nullValue()));
        assertThat(response.path("name"), is(created.<String>path("name")));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("PATCH /api/projects/{id} allows changing only the case of the project's own name")
    void changeCaseOfOwnName() {
        Response created = createProject(ProjectRequest.valid());
        String upperName = created.<String>path("name").toUpperCase();

        Response response =
                apiClient.patch(Endpoints.PROJECT_BY_ID, Map.of("name", upperName), created.<String>path("id"));

        assertThat(response.statusCode(), is(200));
        assertThat(response.path("name"), is(upperName));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("PATCH /api/projects/{id} rejects renaming to another project's name with 400")
    void renameToExistingNameRejected() {
        String takenName = createProject(ProjectRequest.valid()).path("name");
        String id = createProject(ProjectRequest.valid()).path("id");

        Response response = apiClient.patch(Endpoints.PROJECT_BY_ID, Map.of("name", takenName), id);

        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error"), is("project name already exists"));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("PATCH /api/projects/{id} rejects an empty body with 400")
    void emptyPatchRejected() {
        String id = createProject(ProjectRequest.valid()).path("id");

        Response response = apiClient.patch(Endpoints.PROJECT_BY_ID, Map.of(), id);

        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error"), is("no updatable fields provided"));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("PATCH /api/projects/{id} rejects a non-string name with 400")
    void nonStringNameRejected() {
        String id = createProject(ProjectRequest.valid()).path("id");

        Response response = apiClient.patch(Endpoints.PROJECT_BY_ID, Map.of("name", 123), id);

        assertThat(response.statusCode(), is(400));
        assertThat(response.path("error"), is("name must be a string"));
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("PATCH /api/projects/{id} returns 404 for an unknown id")
    void patchUnknownProjectReturns404() {
        Response response =
                apiClient.patch(Endpoints.PROJECT_BY_ID, Map.of("name", ProjectRequest.uniqueName()), UNKNOWN_ID);

        assertThat(response.statusCode(), is(404));
        assertThat(response.path("error"), is("project not found"));
    }

    @Test
    @Timeout(value = TestTimeouts.L, unit = TimeUnit.SECONDS)
    @DisplayName("DELETE /api/projects/{id} returns 204 and the project can no longer be fetched")
    void deleteProject() {
        String id = createProject(ProjectRequest.valid()).path("id");

        Response response = apiClient.delete(Endpoints.PROJECT_BY_ID, id);

        assertThat(response.statusCode(), is(204));
        assertThat(apiClient.get(Endpoints.PROJECT_BY_ID, id).statusCode(), is(404));
    }

    @Test
    @Timeout(value = TestTimeouts.M, unit = TimeUnit.SECONDS)
    @DisplayName("DELETE /api/projects/{id} returns 404 for an unknown id")
    void deleteUnknownProjectReturns404() {
        Response response = apiClient.delete(Endpoints.PROJECT_BY_ID, UNKNOWN_ID);

        assertThat(response.statusCode(), is(404));
        assertThat(response.path("error"), is("project not found"));
    }
}
