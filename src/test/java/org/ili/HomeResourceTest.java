package org.ili;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.ili.dto.AddMemberRequest;
import org.ili.dto.CreateHomeRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HomeResourceTest {

    private static java.util.UUID createdHomeId;

    @Test
    @Order(1)
    public void testCreateHome() {
        CreateHomeRequest request = new CreateHomeRequest("My Test Home");

        String id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/homes")
                .then()
                .statusCode(201)
                .body("name", is("My Test Home"))
                .body("memberUsernames", hasItem("Alice"))
                .extract().path("id");

        createdHomeId = java.util.UUID.fromString(id);
    }

    @Test
    @Order(2)
    public void testGetMyHomes() {
        given()
                .when().get("/api/v1/homes")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @Order(3)
    public void testAddMember() {
        AddMemberRequest request = new AddMemberRequest("bob@example.com");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/homes/" + createdHomeId + "/members")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(4)
    public void testRemoveMember() {
        // Remove Bob from the newly created home
        // 22222222-2222-2222-2222-222222222222 is Bob's UUID
        given()
                .when().delete("/api/v1/homes/" + createdHomeId + "/members/22222222-2222-2222-2222-222222222222")
                .then()
                .statusCode(204);
    }
}
