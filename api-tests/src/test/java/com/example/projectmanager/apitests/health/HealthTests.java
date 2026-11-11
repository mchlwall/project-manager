package com.example.projectmanager.apitests.health;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import com.example.projectmanager.apitests.config.Endpoints;
import com.example.projectmanager.apitests.support.ApiClient;
import com.example.projectmanager.apitests.support.ResponseLoggingExtension;
import com.example.projectmanager.apitests.support.TestTimeouts;
import io.restassured.response.Response;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("smoke")
@ExtendWith(ResponseLoggingExtension.class)
class HealthTests {

    private static final ApiClient apiClient = new ApiClient();

    @Test
    @Timeout(value = TestTimeouts.S, unit = TimeUnit.SECONDS)
    @DisplayName("GET /health returns 200 with status ok")
    void healthReturnsOk() {
        Response response = apiClient.get(Endpoints.HEALTH);

        assertThat(response.statusCode(), is(200));
        assertThat(response.contentType(), startsWith("application/json"));
        assertThat(response.path("status"), is("ok"));
    }
}
