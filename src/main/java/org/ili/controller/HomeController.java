package org.ili.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ili.dto.*;
import org.ili.service.HomeService;
import org.ili.service.PlantService;

import java.util.List;
import java.util.UUID;
/**
 * REST controller exposing household, member and room management endpoints.
 */

@Path("/api/v1/homes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"MEMBER", "ADMIN"})
public class HomeController {

    @Inject
    HomeService homeService;

    @Inject
    PlantService plantService;

    /**
     * Lists homes the current authenticated user belongs to.
     *
     * @return homes visible to the current user.
     */
    @GET
    public List<HomeResponse> getMyHomes() {
        return homeService.getMyHomes();
    }

    /**
     * Returns one home by ID.
     *
     * @param homeId the home identifier.
     * @return home details.
     */
    @GET
	@Path("/{id}")
    public HomeResponse getHome(@PathParam("id") UUID homeId) {
        return homeService.getById(homeId);
    }

    /**
     * Creates a new home owned by the current user.
     *
     * @param request creation payload.
     * @return HTTP 201 with the created home.
     */
    @POST
    public Response createHome(CreateHomeRequest request) {
        HomeResponse home = homeService.createHome(request);
        return Response.status(Response.Status.CREATED).entity(home).build();
    }

    /**
     * Deletes a home if the user has sufficient permissions.
     *
     * @param homeId the home identifier.
     * @return HTTP 204 when deletion succeeds.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteHome(@PathParam("id") UUID homeId) {
        homeService.deleteHome(homeId);
        return Response.noContent().build();
    }


    /**
     * Adds a member to a home.
     *
     * @param homeId the home identifier.
     * @param request payload identifying the user and role.
     * @return HTTP 200 when the member is added.
     */
    @POST
    @Path("/{id}/members")
    public Response addMember(@PathParam("id") UUID homeId, AddMemberRequest request) {
        homeService.addMember(homeId, request);
        return Response.ok().build();
    }

    /**
     * Lists all members of one home.
     *
     * @param homeId the home identifier.
     * @return home member summaries.
     */
    @GET
    @Path("/{id}/members")
    public List<HomeMemberResponse> getMembers(@PathParam("id") UUID homeId) {
        return homeService.getMembersByHomeId(homeId);
    }

    /**
     * Removes a member from a home.
     *
     * @param homeId the home identifier.
     * @param userId the user identifier to remove.
     * @return HTTP 204 when removal succeeds.
     */
    @DELETE
    @Path("/{id}/members/{userId}")
    public Response removeMember(@PathParam("id") UUID homeId, @PathParam("userId") UUID userId) {
        homeService.removeMember(homeId, userId);
        return Response.noContent().build();
    }

    /**
     * Lists rooms belonging to one home.
     *
     * @param homeId the home identifier.
     * @return room summaries for the home.
     */
    @GET
    @Path("/{id}/rooms")
    public List<RoomResponse> getRooms(@PathParam("id") UUID homeId) {
        return homeService.getRoomsByHomeId(homeId);
    }

    /**
     * Creates a room in a home.
     *
     * @param homeId the home identifier.
     * @param request payload containing room data.
     * @return HTTP 201 with the created room.
     */
    @POST
    @Path("/{id}/rooms")
    public Response createRoom(@PathParam("id") UUID homeId, CreateRoomRequest request) {
        RoomResponse room = homeService.createRoom(homeId, request);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    /**
     * Deletes a room from a home.
     *
     * @param homeId the home identifier from route context.
     * @param roomId the room identifier.
     * @return HTTP 204 when deletion succeeds.
     */
    @DELETE
    @Path("/{id}/rooms/{roomId}")
    public Response deleteRoom(@PathParam("id") UUID homeId, @PathParam("roomId") UUID roomId) {
        // Note: homeId n'est pas strictement nécessaire pour supprimer la room par ID,
        // mais on pourrait vérifier que la room appartient bien à cette home pour la sécurité.
        homeService.deleteRoom(roomId);
        return Response.noContent().build();
    }
}

