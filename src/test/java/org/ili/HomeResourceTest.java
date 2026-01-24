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

    @Test
    @Order(1)
    public void testCreateHome() {
        CreateHomeRequest request = new CreateHomeRequest("My Test Home");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/homes")
                .then()
                .statusCode(201)
                .body("name", is("My Test Home"))
                .body("memberUsernames", hasItem("Alice")); // Alice is user ID 1
    }

    @Test
    @Order(2)
    public void testGetMyHomes() {
        given()
                .when().get("/homes")
                .then()
                .statusCode(200)
                .body("size()", is(2)); // 1 from import.sql + 1 from testCreateHome
    }

    @Test
    @Order(3)
    public void testAddMember() {
        // Add Bob (user ID 2) to the home created in testCreateHome (ID 2, since ID 1 is in import.sql)
        AddMemberRequest request = new AddMemberRequest("bob@example.com");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/homes/2/members")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(4)
    public void testRemoveMember() {
        // Remove Bob (ID 2) from home ID 2
        given()
                .when().delete("/homes/2/members/2")
                .then()
                .statusCode(204);
    }
}
