package org.ili;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.CreateRoomRequest;
import org.ili.dto.LoginRequest;
import org.ili.dto.RefreshRequest;
import org.ili.dto.RegisterRequest;
import org.ili.entity.CareLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class FullApplicationE2ETest {

    @Test
    void testEntireApplicationFlow() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "e2e-" + suffix + "@example.com";
        String username = "e2e_" + suffix;
        String password = "Password123!";

        AuthTokens registerTokens = registerUser(email, username, password);
        AuthTokens loginTokens = loginUser(email, password);

        given()
                .header("Authorization", "Bearer " + loginTokens.accessToken)
                .when().get("/api/v1/auth/me")
                .then()
                .statusCode(200)
                .body("email", equalTo(email));

        AuthTokens refreshedTokens = refreshTokens(loginTokens.refreshToken);

        given()
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .when().get("/api/v1/auth/me")
                .then()
                .statusCode(200)
                .body("email", equalTo(email))
                .body("username", equalTo(username));

        String homeId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .body(new CreateHomeRequest("E2E Home " + suffix))
                .when().post("/api/v1/homes")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().path("id");

        String roomId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .body(new CreateRoomRequest("E2E Room " + suffix))
                .when().post("/api/v1/homes/" + homeId + "/rooms")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().path("id");

        String plantId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .body(new CreatePlantRequest(
                        "Monstera " + suffix,
                        "Monstera Deliciosa",
                        7,
                        LocalDate.now().minusDays(2),
                        UUID.fromString(roomId)
                ))
                .when().post("/api/v1/plants")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().path("id");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .body(new CreateLogRequest(CareLog.CareType.WATERING, "E2E watering"))
                .when().post("/api/v1/plants/" + plantId + "/logs")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .when().get("/api/v1/plants/" + plantId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Monstera " + suffix))
                .body("lastWateredDate", equalTo(LocalDate.now().toString()));

        given()
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .when().get("/api/v1/plants/" + plantId + "/logs")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].type", equalTo("WATERING"));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + refreshedTokens.accessToken)
                .when().post("/api/v1/auth/logout")
                .then()
                .statusCode(204);

        given()
                .contentType(ContentType.JSON)
                .body(new RefreshRequest(refreshedTokens.refreshToken))
                .when().post("/api/v1/auth/refresh")
                .then()
                .statusCode(401);

        given()
                .header("Authorization", "Bearer " + registerTokens.accessToken)
                .when().get("/api/v1/auth/me")
                .then()
                .statusCode(200);
    }

    private AuthTokens registerUser(String email, String username, String password) {
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();

        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/register")
                .then()
                .statusCode(201)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract().response();

        return new AuthTokens(
                response.path("accessToken"),
                response.path("refreshToken")
        );
    }

    private AuthTokens loginUser(String email, String password) {
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract().response();

        return new AuthTokens(
                response.path("accessToken"),
                response.path("refreshToken")
        );
    }

    private AuthTokens refreshTokens(String refreshToken) {
        RefreshRequest request = new RefreshRequest(refreshToken);

        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .extract().response();

        return new AuthTokens(
                response.path("accessToken"),
                response.path("refreshToken")
        );
    }

    private record AuthTokens(String accessToken, String refreshToken) {
    }
}
