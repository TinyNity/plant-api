package org.ili.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ili.dto.CareLogResponse;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.PlantResponse;
import org.ili.dto.UpdatePlantRequest;
import org.ili.service.PlantService;

import java.util.List;
import java.util.UUID;
/**
 * REST controller exposing plant and care log operations.
 */

@Path("/api/v1/plants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"MEMBER", "ADMIN"})
public class PlantController {

    @Inject
    PlantService plantService;

    /**
     * Retrieves all plants visible to the current authenticated user.
     *
     * @return the list of accessible plants.
     */
    @GET
    public List<PlantResponse> getAllPlants() {
        return plantService.getAllPlants();
    }

    /**
     * Retrieves one plant by ID if the current user can access it.
     *
     * @param id the plant identifier.
     * @return the matching plant.
     */
    @GET
    @Path("/{id}")
    public PlantResponse getPlant(@PathParam("id") UUID id) {
        return plantService.getPlantById(id);
    }

    /**
     * Creates a new plant in the requested room.
     *
     * @param request payload containing plant creation data.
     * @return HTTP 201 with the created plant.
     */
    @POST
    public Response createPlant(CreatePlantRequest request) {
        PlantResponse plant = plantService.createPlant(request);
        return Response.status(Response.Status.CREATED).entity(plant).build();
    }

    /**
     * Updates an existing plant.
     *
     * @param id the plant identifier.
     * @param request payload containing updatable plant fields.
     * @return HTTP 200 with the updated plant.
     */
    @PUT
    @Path("/{id}")
    public Response updatePlant(@PathParam("id") UUID id, UpdatePlantRequest request) {
        PlantResponse plant = plantService.updatePlant(id, request);
        return Response.ok(plant).build();
    }

    /**
     * Deletes a plant.
     *
     * @param id the plant identifier.
     * @return HTTP 204 when deletion succeeds.
     */
    @DELETE
    @Path("/{id}")
    public Response deletePlant(@PathParam("id") UUID id) {
        plantService.deletePlant(id);
        return Response.noContent().build();
    }

    /**
     * Adds a care log entry to a plant.
     *
     * @param plantId the plant identifier.
     * @param request payload describing the care operation.
     * @return HTTP 201 when the log is created.
     */
    @POST
    @Path("/{id}/logs")
    public Response addLog(@PathParam("id") UUID plantId, CreateLogRequest request) {
        plantService.addCareLog(plantId, request);
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Lists care logs for one plant.
     *
     * @param plantId the plant identifier.
     * @return care logs ordered according to repository behavior.
     */
    @GET
    @Path("/{id}/logs")
    public List<CareLogResponse> getLogs(@PathParam("id") UUID plantId) {
        return plantService.getCareLogs(plantId);
    }
}

