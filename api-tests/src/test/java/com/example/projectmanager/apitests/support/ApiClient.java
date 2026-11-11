package com.example.projectmanager.apitests.support;

import com.example.projectmanager.apitests.config.Endpoints;
import com.example.projectmanager.apitests.config.TestConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests must send every request through this class, never RestAssured directly, so each call is attached to the
 * Allure report and logged by {@link ResponseLoggingExtension} when a test fails. Every project created through it
 * is deleted by {@link TestDataListener} when the run finishes. Argument order is:
 * {@code (endpoint, body?, pathParams...)}.
 */
public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);
    private static final AllureRestAssured ALLURE = new AllureRestAssured();

    private final String baseUrl;

    public ApiClient() {
        this(TestConfig.baseUrl());
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Response get(String endpoint, Object... pathParams) {
        log.info("GET {}{}", baseUrl, endpoint);
        return remember(request().get(endpoint, pathParams), null);
    }

    public Response getWithQueryParams(String endpoint, Map<String, ?> queryParams, Object... pathParams) {
        log.info("GET {}{} {}", baseUrl, endpoint, queryParams);
        return remember(request().queryParams(queryParams).get(endpoint, pathParams), queryParams);
    }

    public Response post(String endpoint, Object body, Object... pathParams) {
        log.info("POST {}{}", baseUrl, endpoint);
        Response response = jsonRequest(body).post(endpoint, pathParams);
        if (Endpoints.PROJECTS.equals(endpoint) && response.statusCode() == 201) {
            TestDataListener.track(response.path("id"));
        }
        return remember(response, body);
    }

    public Response patch(String endpoint, Object body, Object... pathParams) {
        log.info("PATCH {}{}", baseUrl, endpoint);
        return remember(jsonRequest(body).patch(endpoint, pathParams), body);
    }

    public Response delete(String endpoint, Object... pathParams) {
        log.info("DELETE {}{}", baseUrl, endpoint);
        return remember(request().delete(endpoint, pathParams), null);
    }

    private RequestSpecification request() {
        return RestAssured.given().baseUri(baseUrl).accept(ContentType.JSON).filter(ALLURE);
    }

    private RequestSpecification jsonRequest(Object body) {
        return request().contentType(ContentType.JSON).body(body);
    }

    private static Response remember(Response response, Object requestBody) {
        ResponseLoggingExtension.setResponse(response, requestBody);
        return response;
    }
}
