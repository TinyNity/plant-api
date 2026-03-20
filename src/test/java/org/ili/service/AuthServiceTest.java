package org.ili.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.ili.entity.Home;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;
import org.ili.entity.Plant;
import org.ili.entity.RefreshToken;
import org.ili.entity.Room;
import org.ili.entity.User;
import org.ili.enumeration.Role;
import org.ili.repository.HomeMemberRepository;
import org.ili.repository.RefreshTokenRepository;
import org.ili.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    HomeMemberRepository homeMemberRepository;

    @Mock
    io.smallrye.jwt.auth.principal.JWTParser jwtParser;

    @Mock
    JsonWebToken jwt;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        authService.userRepository = userRepository;
        authService.refreshTokenRepository = refreshTokenRepository;
        authService.homeMemberRepository = homeMemberRepository;
        authService.jwtParser = jwtParser;
        authService.jwt = jwt;
        authService.issuer = "https://test-issuer";
    }

    @Test
    void loginWithUnknownEmailThrowsUnauthorized() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> authService.login(org.ili.dto.LoginRequest.builder()
                        .email("missing@example.com")
                        .password("Password123!")
                        .build()));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void loginWithBadPasswordThrowsUnauthorized() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .username("user")
                .password(BcryptUtil.bcryptHash("CorrectPassword123!"))
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> authService.login(org.ili.dto.LoginRequest.builder()
                        .email("user@example.com")
                        .password("WrongPassword123!")
                        .build()));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void refreshWithInvalidJwtThrowsUnauthorized() throws Exception {
        when(jwtParser.parse("invalid-refresh-token")).thenThrow(new RuntimeException("invalid token"));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> authService.refresh("invalid-refresh-token"));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void refreshWithRevokedTokenThrowsUnauthorized() throws Exception {
        UUID userId = UUID.randomUUID();
        JsonWebToken parsedToken = mock(JsonWebToken.class);

        when(jwtParser.parse("revoked-token")).thenReturn(parsedToken);
        when(parsedToken.getSubject()).thenReturn(userId.toString());
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> authService.refresh("revoked-token"));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void logoutDeletesAllRefreshTokensForUser() {
        UUID userId = UUID.randomUUID();

        authService.logout(userId);

        verify(refreshTokenRepository).deleteAllByUserId(userId);
    }

    @Test
    void getCurrentUserIdReturnsJwtSubjectAsUuid() {
        UUID currentUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(currentUserId.toString());

        UUID resolved = authService.getCurrentUserId();

        assertEquals(currentUserId, resolved);
    }

    @Test
    void getCurrentUserThrowsWhenUserDoesNotExist() {
        UUID currentUserId = UUID.randomUUID();
        when(jwt.getSubject()).thenReturn(currentUserId.toString());
        when(userRepository.findByIdOptional(currentUserId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUser());
    }

    @Test
    void getCurrentUserReturnsUserWhenPresent() {
        UUID currentUserId = UUID.randomUUID();
        User user = User.builder().id(currentUserId).username("alex").build();

        when(jwt.getSubject()).thenReturn(currentUserId.toString());
        when(userRepository.findByIdOptional(currentUserId)).thenReturn(Optional.of(user));

        User currentUser = authService.getCurrentUser();

        assertEquals(currentUserId, currentUser.getId());
        assertEquals("alex", currentUser.getUsername());
    }

    @Test
    void getUserPermissionByHomeAndUserReturnsMembershipRole() {
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(homeId, userId))
                .role(Role.ADMIN)
                .build();

        when(homeMemberRepository.findByIdOptional(new HomeMemberId(homeId, userId)))
                .thenReturn(Optional.of(membership));

        Role role = authService.getUserPermission(homeId, userId);

        assertEquals(Role.ADMIN, role);
    }

    @Test
    void getUserPermissionByHomeAndUserThrowsWhenMembershipMissing() {
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(homeMemberRepository.findByIdOptional(new HomeMemberId(homeId, userId)))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.getUserPermission(homeId, userId));

        assertTrue(ex.getMessage().contains("not a member"));
    }

    @Test
    void getUserPermissionForHomeResolvesUsingCurrentUser() {
        UUID homeId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        Home home = Home.builder().id(homeId).build();
        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(homeId, currentUserId))
                .role(Role.MEMBER)
                .build();

        when(jwt.getSubject()).thenReturn(currentUserId.toString());
        when(homeMemberRepository.findByIdOptional(new HomeMemberId(homeId, currentUserId)))
                .thenReturn(Optional.of(membership));

        Role role = authService.getUserPermission(home);

        assertEquals(Role.MEMBER, role);
    }

    @Test
    void getUserPermissionForPlantDelegatesToRoom() {
        UUID homeId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        Home home = Home.builder().id(homeId).build();
        Room room = Room.builder().home(home).build();
        Plant plant = Plant.builder().room(room).build();

        User currentUser = User.builder().id(currentUserId).build();
        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(homeId, currentUserId))
                .role(Role.GUEST)
                .build();

        when(jwt.getSubject()).thenReturn(currentUserId.toString());
        when(userRepository.findByIdOptional(currentUserId)).thenReturn(Optional.of(currentUser));
        when(homeMemberRepository.findByIdOptional(new HomeMemberId(homeId, currentUserId)))
                .thenReturn(Optional.of(membership));

        Role role = authService.getUserPermission(plant);

        assertEquals(Role.GUEST, role);
    }

    @Test
    void refreshWithUnknownUserThrowsUnauthorized() throws Exception {
        UUID userId = UUID.randomUUID();
        JsonWebToken parsedToken = mock(JsonWebToken.class);

        when(jwtParser.parse("valid-but-user-missing")).thenReturn(parsedToken);
        when(parsedToken.getSubject()).thenReturn(userId.toString());
        when(refreshTokenRepository.findByToken("valid-but-user-missing"))
                .thenReturn(Optional.of(RefreshToken.builder().token("valid-but-user-missing").build()));
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> authService.refresh("valid-but-user-missing"));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());
    }
}
