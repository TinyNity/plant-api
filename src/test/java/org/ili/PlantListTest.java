package org.ili;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.CreateRoomRequest;
import org.ili.dto.RegisterRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class PlantListTest {

    @Test
    public void testGetAllPlants() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String accessToken = registerUser(
        "plants-" + suffix + "@example.com",
        "plants_" + suffix,
        "Password123!"
    );
    String homeId = createHome(accessToken, "Plant list home " + suffix);
    String roomId = createRoom(accessToken, homeId, "Living room " + suffix);

    createPlant(accessToken, roomId, "Monstera", "Monstera Deliciosa", 7);
    createPlant(accessToken, roomId, "Pothos", "Epipremnum Aureum", 5);

        given()
        .header("Authorization", "Bearer " + accessToken)
                .when().get("/api/v1/plants")
                .then()
                .statusCode(200)
        .body("size()", is(2));
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

    private void createPlant(String accessToken, String roomId, String name, String species, int wateringFrequency) {
    CreatePlantRequest request = new CreatePlantRequest(
        name,
        species,
        wateringFrequency,
        LocalDate.now().minusDays(2),
        UUID.fromString(roomId)
    );

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + accessToken)
        .body(request)
        .when().post("/api/v1/plants")
        .then()
        .statusCode(201);
    }
}
