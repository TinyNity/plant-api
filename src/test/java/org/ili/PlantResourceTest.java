package org.ili;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.CreateRoomRequest;
import org.ili.dto.RegisterRequest;
import org.ili.entity.CareLog;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlantResourceTest {

    private static String ownerAccessToken;
    private static String homeId;
    private static String roomId;
    private static String plantId;

    @Test
    @Order(1)
    public void testCreateFixtureData() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ownerAccessToken = registerUser(
                "plant-owner-" + suffix + "@example.com",
                "plant_owner_" + suffix,
                "Password123!"
        );

        homeId = createHome(ownerAccessToken, "Plant resource home " + suffix);
        roomId = createRoom(ownerAccessToken, homeId, "Salon " + suffix);
        plantId = createPlant(ownerAccessToken, roomId, "Ficus", "Ficus Benjamina", 7, LocalDate.now().minusDays(3));
    }

    @Test
    @Order(2)
    public void testGetRooms() {
        given()
                .header("Authorization", "Bearer " + ownerAccessToken)
                .when().get("/api/v1/homes/" + homeId + "/rooms")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(roomId));
    }

    @Test
    @Order(3)
    public void testCreatePlant() {
        CreatePlantRequest request = new CreatePlantRequest(
                "Monstera",
                "Monstera Deliciosa",
                10,
                LocalDate.now().minusDays(5),
                UUID.fromString(roomId)
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .body(request)
                .when().post("/api/v1/plants")
                .then()
                .statusCode(201)
                .body("name", is("Monstera"))
                .body("roomId", is(roomId));
    }

    @Test
    @Order(4)
    public void testGetPlant() {
        given()
                .header("Authorization", "Bearer " + ownerAccessToken)
                .when().get("/api/v1/plants/" + plantId)
                .then()
                .statusCode(200)
                .body("name", is("Ficus"));
    }

    @Test
    @Order(5)
    public void testAddLog() {
        CreateLogRequest request = new CreateLogRequest(CareLog.CareType.WATERING, "Arrosage abondant");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ownerAccessToken)
                .body(request)
                .when().post("/api/v1/plants/" + plantId + "/logs")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + ownerAccessToken)
                .when().get("/api/v1/plants/" + plantId)
                .then()
                .statusCode(200)
                .body("lastWateredDate", is(LocalDate.now().toString()));
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

    private String createHome(String accessToken, String name) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(new CreateHomeRequest(name))
                .when().post("/api/v1/homes")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String createRoom(String accessToken, String homeId, String name) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(new CreateRoomRequest(name))
                .when().post("/api/v1/homes/" + homeId + "/rooms")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String createPlant(String accessToken, String roomId, String name, String species, int wateringFrequency,
            LocalDate lastWateredDate) {
        CreatePlantRequest request = new CreatePlantRequest(
                name,
                species,
                wateringFrequency,
                lastWateredDate,
                UUID.fromString(roomId)
        );

        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .when().post("/api/v1/plants")
                .then()
                .statusCode(201)
                .extract().path("id");
    }
}
