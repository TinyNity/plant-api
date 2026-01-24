package org.ili.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.PlantResponse;
import org.ili.dto.RoomResponse;
import org.ili.service.PlantService;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlantController {

    @Inject
    PlantService plantService;

    @GET
    @Path("homes/{id}/rooms")
    public List<RoomResponse> getRooms(@PathParam("id") Long homeId) {
        return plantService.getRoomsByHomeId(homeId);
    }

    @POST
    @Path("plants")
    public Response createPlant(CreatePlantRequest request) {
        PlantResponse plant = plantService.createPlant(request);
        return Response.status(Response.Status.CREATED).entity(plant).build();
    }

    @GET
    @Path("plants/{id}")
    public PlantResponse getPlant(@PathParam("id") Long id) {
        return plantService.getPlantById(id);
    }

    @POST
    @Path("plants/{id}/logs")
    public Response addLog(@PathParam("id") Long plantId, CreateLogRequest request) {
        plantService.addCareLog(plantId, request);
        return Response.status(Response.Status.CREATED).build();
    }
}
