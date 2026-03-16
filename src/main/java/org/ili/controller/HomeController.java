package org.ili.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ili.dto.*;
import org.ili.service.HomeService;
import org.ili.service.PlantService;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/homes")
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

    @GET
	@Path("/{id}")
    public HomeResponse getHome(@PathParam("id") UUID homeId) {
        return homeService.getById(homeId);
    }

    @POST
    public Response createHome(CreateHomeRequest request) {
        HomeResponse home = homeService.createHome(request);
        return Response.status(Response.Status.CREATED).entity(home).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteHome(@PathParam("id") UUID homeId) {
        homeService.deleteHome(homeId);
        return Response.noContent().build();
    }


    @POST
    @Path("/{id}/members")
    public Response addMember(@PathParam("id") UUID homeId, AddMemberRequest request) {
        homeService.addMember(homeId, request);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/members")
    public List<HomeMemberResponse> getMembers(@PathParam("id") UUID homeId) {
        return homeService.getMembersByHomeId(homeId);
    }

    @DELETE
    @Path("/{id}/members/{userId}")
    public Response removeMember(@PathParam("id") UUID homeId, @PathParam("userId") UUID userId) {
        homeService.removeMember(homeId, userId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/rooms")
    public List<RoomResponse> getRooms(@PathParam("id") UUID homeId) {
        return homeService.getRoomsByHomeId(homeId);
    }

    @POST
    @Path("/{id}/rooms")
    public Response createRoom(@PathParam("id") UUID homeId, CreateRoomRequest request) {
        RoomResponse room = homeService.createRoom(homeId, request);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @DELETE
    @Path("/{id}/rooms/{roomId}")
    public Response deleteRoom(@PathParam("id") UUID homeId, @PathParam("roomId") UUID roomId) {
        // Note: homeId n'est pas strictement nécessaire pour supprimer la room par ID,
        // mais on pourrait vérifier que la room appartient bien à cette home pour la sécurité.
        homeService.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
