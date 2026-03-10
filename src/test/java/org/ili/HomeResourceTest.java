package org.ili;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.ili.dto.AddMemberRequest;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.RegisterRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HomeResourceTest {

    private static String ownerAccessToken;
    private static String ownerUsername;
    private static String bobAccessToken;
    private static String bobEmail;
    private static UUID bobUserId;
    private static UUID createdHomeId;

    @Test
    @Order(1)
    public void testRegisterUsers() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ownerUsername = "home_owner_" + suffix;
        bobEmail = "bob-" + suffix + "@example.com";

        ownerAccessToken = registerUser(
                "home-owner-" + suffix + "@example.com",
                ownerUsername,
                "Password123!"
        );
        bobAccessToken = registerUser(
                bobEmail,
                "bob_" + suffix,
                "Password123!"
        );

        bobUserId = UUID.fromString(
                given()
                        .header("Authorization", "Bearer " + bobAccessToken)
                        .when().get("/api/v1/auth/me")
                        .then()
                        .statusCode(200)
                        .extract().path("id")
        );
    }

    @Test
    @Order(2)
    public void testCreateHome() {
        CreateHomeRequest request = new CreateHomeRequest("My Test Home");

        String id = given()
                .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + ownerAccessToken)
                .body(request)
                .when().post("/api/v1/homes")
                .then()
                .statusCode(201)
            .body("name", is("My Test Home"))
            .body("memberUsernames", hasItem(ownerUsername))
                .extract().path("id");

        createdHomeId = UUID.fromString(id);
    }

    @Test
    @Order(3)
    public void testGetMyHomes() {
        given()
            .header("Authorization", "Bearer " + ownerAccessToken)
                .when().get("/api/v1/homes")
                .then()
                .statusCode(200)
            .body("size()", is(1));
    }

    @Test
    @Order(4)
    public void testAddMember() {
        AddMemberRequest request = new AddMemberRequest(bobEmail);

        given()
                .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + ownerAccessToken)
                .body(request)
                .when().post("/api/v1/homes/" + createdHomeId + "/members")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(5)
    public void testRemoveMember() {
        given()
                .header("Authorization", "Bearer " + ownerAccessToken)
                .when().delete("/api/v1/homes/" + createdHomeId + "/members/" + bobUserId)
                .then()
                .statusCode(204);
    }

    private String registerUser(String email, String username, String password) {
        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/register")
                .then()
                .statusCode(201)
                .extract().path("accessToken");
    }
}
