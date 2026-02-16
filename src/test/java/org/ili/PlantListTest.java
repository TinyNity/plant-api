package org.ili;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class PlantListTest {

    @Test
    public void testGetAllPlants() {
        // Should return at least 11 plants from import.sql
        given()
                .when().get("/plants")
                .then()
                .statusCode(200)
                .body("size()", is(11));
    }
}
