package org.ili.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ili.dto.AddMemberRequest;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.HomeResponse;
import org.ili.dto.RoomResponse;
import org.ili.service.HomeService;
import org.ili.service.PlantService;

import java.util.List;

@Path("/homes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HomeController {

    @Inject
    HomeService homeService;

    @Inject
    PlantService plantService;

    @GET
    public List<HomeResponse> getMyHomes() {
        return homeService.getMyHomes();
    }

    @POST
    public Response createHome(CreateHomeRequest request) {
        HomeResponse home = homeService.createHome(request);
        return Response.status(Response.Status.CREATED).entity(home).build();
    }

    @POST
    @Path("/{id}/members")
    public Response addMember(@PathParam("id") Long homeId, AddMemberRequest request) {
        homeService.addMember(homeId, request);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}/members/{userId}")
    public Response removeMember(@PathParam("id") Long homeId, @PathParam("userId") Long userId) {
        homeService.removeMember(homeId, userId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/rooms")
    public List<RoomResponse> getRooms(@PathParam("id") Long homeId) {
        return plantService.getRoomsByHomeId(homeId);
    }
}
