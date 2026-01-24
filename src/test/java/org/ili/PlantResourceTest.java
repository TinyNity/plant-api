package org.ili;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.entity.CareLog;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlantResourceTest {

    @Test
    @Order(1)
    public void testGetRooms() {
        // Home ID 1 from import.sql
        given()
                .when().get("/homes/1/rooms")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("Salon"));
    }

    @Test
    @Order(2)
    public void testCreatePlant() {
        CreatePlantRequest request = new CreatePlantRequest(
                "Monstera",
                "Monstera Deliciosa",
                10,
                LocalDate.now().minusDays(5),
                1L // Room ID 1 from import.sql
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/plants")
                .then()
                .statusCode(201)
                .body("name", is("Monstera"))
                .body("roomId", is(1));
    }

    @Test
    @Order(3)
    public void testGetPlant() {
        // Plant ID 1 from import.sql
        given()
                .when().get("/plants/1")
                .then()
                .statusCode(200)
                .body("name", is("Ficus"));
    }

    @Test
    @Order(4)
    public void testAddLog() {
        // Add log to Plant ID 1
        CreateLogRequest request = new CreateLogRequest(CareLog.CareType.WATERING, "Arrosage abondant");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/plants/1/logs")
                .then()
                .statusCode(201);
        
        // Verify lastWateredDate is updated
        given()
                .when().get("/plants/1")
                .then()
                .statusCode(200)
                .body("lastWateredDate", is(LocalDate.now().toString()));
    }
}
