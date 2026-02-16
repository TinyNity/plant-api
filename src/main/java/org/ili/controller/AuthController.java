package org.ili.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.ili.dto.AuthResponse;
import org.ili.dto.LoginRequest;
import org.ili.dto.RefreshRequest;
import org.ili.dto.RegisterRequest;
import org.ili.dto.UserResponse;
import org.ili.service.AuthService;

import jakarta.validation.Valid;
import java.util.UUID;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/register")
    @PermitAll
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns authentication tokens")
    @APIResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @APIResponse(responseCode = "409", description = "Email already exists")
    @APIResponse(responseCode = "400", description = "Invalid request data")
    public Response register(RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Login user", description = "Authenticates user and returns access and refresh tokens")
    @APIResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @APIResponse(responseCode = "401", description = "Invalid credentials")
    public Response login(@Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    @Operation(summary = "Refresh tokens", description = "Exchanges a valid refresh token for new access and refresh tokens")
    @APIResponse(responseCode = "200", description = "Tokens refreshed successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @APIResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public Response refresh(@Valid RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return Response.ok(response).build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed({"MEMBER", "ADMIN"})
    @Operation(summary = "Logout user", description = "Revokes all refresh tokens for the current user")
    @APIResponse(responseCode = "204", description = "Logout successful")
    @APIResponse(responseCode = "401", description = "Not authenticated")
    public Response logout() {
        UUID userId = UUID.fromString(jwt.getSubject());
        authService.logout(userId);
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"MEMBER", "ADMIN"})
    @Operation(summary = "Get current user", description = "Returns the profile of the authenticated user")
    @APIResponse(responseCode = "200", description = "User profile",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @APIResponse(responseCode = "401", description = "Not authenticated")
    public Response getCurrentUser() {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserResponse response = authService.getCurrentUser(userId);
        return Response.ok(response).build();
    }
}
