package org.ili.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.PlantResponse;
import org.ili.dto.UpdatePlantRequest;
import org.ili.service.PlantService;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/plants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlantController {

    @Inject
    PlantService plantService;

    @GET
    public List<PlantResponse> getAllPlants() {
        return plantService.getAllPlants();
    }

    @POST
    public Response createPlant(CreatePlantRequest request) {
        PlantResponse plant = plantService.createPlant(request);
        return Response.status(Response.Status.CREATED).entity(plant).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePlant(@PathParam("id") UUID id, UpdatePlantRequest request) {
        PlantResponse plant = plantService.updatePlant(id, request);
        return Response.ok(plant).build();
    }

    @GET
    @Path("/{id}")
    public PlantResponse getPlant(@PathParam("id") UUID id) {
        return plantService.getPlantById(id);
    }

    @POST
    @Path("/{id}/logs")
    public Response addLog(@PathParam("id") UUID plantId, CreateLogRequest request) {
        plantService.addCareLog(plantId, request);
        return Response.status(Response.Status.CREATED).build();
    }
}
