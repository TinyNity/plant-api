package org.ili.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import org.ili.entity.User;
import org.ili.service.AuthService;
import jakarta.ws.rs.PUT;
import org.ili.dto.UpdateEmailRequest;
import org.ili.dto.UpdatePasswordRequest;
import org.ili.dto.UpdateUsernameRequest;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Controller exposing REST endpoints for user authentication.
 * <p>
 * This block contains operations for registering new users, logging in,
 * refreshing access tokens, and logging out. It relies on {@link AuthService}
 * for the underlying business logic.
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    /**
     * Registers a new user with the provided credentials.
     * <p>
     * If the registration is successful, this method returns an
     * {@link AuthResponse} containing a fresh access token and refresh token
     * for the user.
     *
     * @param request The registration details (email, username, password).
     * @return A 201 Created response containing the authentication tokens.
     */
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

    /**
     * Validates the provided credentials against the stored hash in the
     * database. If valid, it returns a new pair of access and refresh tokens.
     *
     * @param request The login credentials (email, password).
     * @return A 200 OK response containing the authentication tokens.
     */
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

    /**
     * Refreshes an expired access token using a valid refresh token.
     * <p>
     * The provided refresh token is parsed and validated. If it's valid, not
     * expired, and the associated user still exists, a new pair of tokens is
     * generated and returned.
     *
     * @param request The request containing the refresh token.
     * @return A 200 OK response containing the new authentication tokens.
     */
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

    /**
     * Logs out the currently authenticated user.
     * <p>
     * Revokes access by handling token invalidation if implemented in a
     * stateful setup. Requires the user to be authenticated (having a valid
     * "MEMBER" or "ADMIN" role).
     *
     * @return A 204 No Content response on successful logout.
     */
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

    /**
     * Retrieves the profile information of the currently authenticated user.
     * <p>
     * It uses the 'subject' claim from the provided JWT token to find the user
     * in the database.
     *
     * @return A 200 OK response containing a {@link UserResponse} object with
     * the user's details.
     */
    @GET
    @Path("/me")
    @RolesAllowed({"MEMBER", "ADMIN"})
    @Operation(summary = "Get current user", description = "Returns the profile of the authenticated user")
    @APIResponse(responseCode = "200", description = "User profile",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @APIResponse(responseCode = "401", description = "Not authenticated")
    public Response getCurrentUser() {
        User user = authService.getCurrentUser();
        UserResponse response = UserResponse.from(user);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/me/username")
    @RolesAllowed({"MEMBER", "ADMIN"})
    @Operation(summary = "Update username", description = "Updates the username of the authenticated user")
    @APIResponse(responseCode = "200", description = "Username updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @APIResponse(responseCode = "401", description = "Not authenticated")
    @APIResponse(responseCode = "409", description = "Username already exists")
    public Response updateUsername(@Valid UpdateUsernameRequest request) {
        User user = authService.updateUsername(request);
        UserResponse response = UserResponse.from(user);
        return Response.ok(response).build();
    }
    
    @PUT
    @Path("/me/email")
    @RolesAllowed({"MEMBER", "ADMIN"})
    @Operation(summary = "Update email", description = "Updates the email of the authenticated user after password confirmation")
    @APIResponse(responseCode = "200", description = "Email updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @APIResponse(responseCode = "401", description = "Current password is incorrect or not authenticated")
    @APIResponse(responseCode = "409", description = "Email already exists")
    public Response updateEmail(@Valid UpdateEmailRequest request) {
        User user = authService.updateEmail(request);
        UserResponse response = UserResponse.from(user);
        return Response.ok(response).build();
    }
    
    @PUT
    @Path("/me/password")
    @RolesAllowed({"MEMBER", "ADMIN"})
    @Operation(summary = "Update password", description = "Updates the password of the authenticated user after verifying the current password")
    @APIResponse(responseCode = "204", description = "Password updated successfully")
    @APIResponse(responseCode = "401", description = "Current password is incorrect or not authenticated")
    @APIResponse(responseCode = "400", description = "New password is invalid")
    public Response updatePassword(@Valid UpdatePasswordRequest request) {
        authService.updatePassword(request);
        return Response.noContent().build();
    }    
}
