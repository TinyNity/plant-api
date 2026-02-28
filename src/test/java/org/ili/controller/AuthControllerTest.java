package org.ili.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.ili.dto.LoginRequest;
import org.ili.dto.RegisterRequest;
import org.ili.dto.RefreshRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    private static final String TEST_EMAIL = "user_" + UUID.randomUUID().toString() + "@test.com";
    private static final String TEST_USERNAME = "user_" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_PASSWORD = "Password123!";
    private static String accessToken;
    private static String refreshToken;

    @Test
    @Order(1)
    void testRegisterUser() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .build();

        accessToken = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/register")
                .then()
                .statusCode(201)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract().path("accessToken");
    }

    @Test
    @Order(2)
    void testRegisterDuplicateUser() {
        RegisterRequest request = RegisterRequest.builder()
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/register")
                .then()
                .statusCode(409);
    }

    @Test
    @Order(3)
    void testLoginUser() {
        LoginRequest request = LoginRequest.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        refreshToken = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract().path("refreshToken");
    }

    @Test
    @Order(4)
    void testLoginInvalidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email(TEST_EMAIL)
                .password("WrongPassword123!")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(5)
    void testRefreshToken() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken(refreshToken);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @Order(6)
    void testGetCurrentUser() {
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/v1/auth/me")
                .then()
                .statusCode(200)
                .body("email", is(TEST_EMAIL))
                .body("username", is(TEST_USERNAME));
    }

    @Test
    @Order(7)
    void testGetCurrentUserUnauthorized() {
        given()
                .when().get("/api/v1/auth/me")
                .then()
                .statusCode(401);
    }
}
