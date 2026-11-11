package com.example.projectmanager.apitests.support;

import io.restassured.response.Response;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logs the last request body and response of a test only when it fails, so passing runs stay quiet. */
public class ResponseLoggingExtension implements TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingExtension.class);
    private static final ThreadLocal<Response> latestResponse = new ThreadLocal<>();
    private static final ThreadLocal<Object> latestRequestBody = new ThreadLocal<>();

    public static void setResponse(Response response, Object requestBody) {
        latestResponse.set(response);
        latestRequestBody.set(requestBody);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        Response response = latestResponse.get();
        if (response != null) {
            log.info("Test failed: {}", context.getDisplayName());
            log.info("Status: {}", response.getStatusCode());
            log.info("Request body: {}", latestRequestBody.get());
            log.info("Response body: {}", response.getBody().asPrettyString());
        }
        clear();
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        clear();
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        clear();
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        clear();
    }

    private static void clear() {
        latestResponse.remove();
        latestRequestBody.remove();
    }
}
