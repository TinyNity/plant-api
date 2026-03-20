package org.ili.service;

import io.quarkus.security.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.ili.dto.AddMemberRequest;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.CreateRoomRequest;
import org.ili.dto.HomeResponse;
import org.ili.dto.RoomResponse;
import org.ili.entity.Home;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;
import org.ili.entity.Room;
import org.ili.entity.User;
import org.ili.enumeration.Role;
import org.ili.repository.HomeMemberRepository;
import org.ili.repository.HomeRepository;
import org.ili.repository.RoomRepository;
import org.ili.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock
    HomeRepository homeRepository;

    @Mock
    RoomRepository roomRepository;

    @Mock
    HomeMemberRepository homeMemberRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthService authService;

    private HomeService homeService;

    @BeforeEach
    void setUp() {
        homeService = new HomeService();
        homeService.homeRepository = homeRepository;
        homeService.roomRepository = roomRepository;
        homeService.homeMemberRepository = homeMemberRepository;
        homeService.userRepository = userRepository;
        homeService.authService = authService;
    }

    @Test
    void createHomeCreatesAdminMembershipForCurrentUser() {
        UUID currentUserId = UUID.randomUUID();
        User currentUser = User.builder()
                .id(currentUserId)
                .username("owner")
                .email("owner@example.com")
                .build();

        when(authService.getCurrentUser()).thenReturn(currentUser);

        HomeResponse response = homeService.createHome(new CreateHomeRequest("My Home"));

        assertEquals("My Home", response.getName());
        assertTrue(response.getMemberUsernames().contains("owner"));
        verify(homeRepository).persist(org.mockito.ArgumentMatchers.any(Home.class));
        verify(homeMemberRepository).persist(org.mockito.ArgumentMatchers.any(HomeMember.class));
    }

    @Test
    void deleteHomeThrowsForbiddenWhenCurrentUserIsNotAdmin() {
        UUID homeId = UUID.randomUUID();
        Home home = Home.builder().id(homeId).name("Home").build();

        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.of(home));
        when(authService.getUserPermission(home)).thenReturn(Role.MEMBER);

        assertThrows(ForbiddenException.class, () -> homeService.deleteHome(homeId));
    }

    @Test
    void deleteHomeDeletesWhenCurrentUserIsAdmin() {
        UUID homeId = UUID.randomUUID();
        Home home = Home.builder().id(homeId).name("Home").build();

        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.of(home));
        when(authService.getUserPermission(home)).thenReturn(Role.ADMIN);

        homeService.deleteHome(homeId);

        verify(homeRepository).delete(home);
    }

    @Test
    void addMemberThrowsForbiddenWhenCurrentUserIsNotAdmin() {
        UUID homeId = UUID.randomUUID();
        Home home = Home.builder().id(homeId).name("Home").members(List.of()).build();

        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.of(home));
        when(authService.getUserPermission(home)).thenReturn(Role.MEMBER);

        assertThrows(ForbiddenException.class,
                () -> homeService.addMember(homeId, new AddMemberRequest("user@example.com")));
    }

    @Test
    void addMemberThrowsWhenUserAlreadyMember() {
        UUID homeId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        User existing = User.builder().id(memberId).email("member@example.com").username("member").build();

        HomeMember member = HomeMember.builder()
                .id(new HomeMemberId(homeId, memberId))
                .user(existing)
                .role(Role.MEMBER)
                .build();

        Home home = Home.builder().id(homeId).name("Home").members(List.of(member)).build();

        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.of(home));
        when(authService.getUserPermission(home)).thenReturn(Role.ADMIN);
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> homeService.addMember(homeId, new AddMemberRequest("member@example.com")));
    }

    @Test
    void addMemberPersistsMembershipWhenAdminAndUserNotMember() {
        UUID homeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User userToAdd = User.builder().id(userId).email("new@example.com").username("new_user").build();
        Home home = Home.builder().id(homeId).name("Home").members(List.of()).build();

        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.of(home));
        when(authService.getUserPermission(home)).thenReturn(Role.ADMIN);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(userToAdd));

        homeService.addMember(homeId, new AddMemberRequest("new@example.com"));

        verify(homeMemberRepository).persist(any(HomeMember.class));
    }

    @Test
    void removeMemberThrowsForbiddenWhenCurrentUserHasNotHigherRole() {
        UUID homeId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(homeId, targetUserId))
                .role(Role.MEMBER)
                .build();

        User currentUser = User.builder().id(currentUserId).build();

        when(homeMemberRepository.findByIdOptional(new HomeMemberId(homeId, targetUserId)))
                .thenReturn(Optional.of(membership));
        when(authService.getUserPermission(homeId, targetUserId)).thenReturn(Role.MEMBER);
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(authService.getUserPermission(homeId, currentUserId)).thenReturn(Role.MEMBER);

        assertThrows(ForbiddenException.class, () -> homeService.removeMember(homeId, targetUserId));
    }

    @Test
    void removeMemberDeletesWhenCurrentUserHasHigherRole() {
        UUID homeId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(homeId, targetUserId))
                .role(Role.MEMBER)
                .build();

        User currentUser = User.builder().id(currentUserId).build();

        when(homeMemberRepository.findByIdOptional(new HomeMemberId(homeId, targetUserId)))
                .thenReturn(Optional.of(membership));
        when(authService.getUserPermission(homeId, targetUserId)).thenReturn(Role.GUEST);
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(authService.getUserPermission(homeId, currentUserId)).thenReturn(Role.ADMIN);

        homeService.removeMember(homeId, targetUserId);

        verify(homeMemberRepository).delete(membership);
    }

    @Test
    void createRoomThrowsForbiddenForMemberRole() {
        UUID homeId = UUID.randomUUID();
        Home home = Home.builder().id(homeId).name("Home").build();

        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.of(home));
        when(authService.getUserPermission(home)).thenReturn(Role.MEMBER);

        assertThrows(ForbiddenException.class,
                () -> homeService.createRoom(homeId, new CreateRoomRequest("Kitchen")));
    }

    @Test
    void createRoomPersistsAndReturnsResponseForAdmin() {
        UUID homeId = UUID.randomUUID();
        Home home = Home.builder().id(homeId).name("Home").build();

        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.of(home));
        when(authService.getUserPermission(home)).thenReturn(Role.ADMIN);

        RoomResponse response = homeService.createRoom(homeId, new CreateRoomRequest("Kitchen"));

        assertEquals("Kitchen", response.getName());
        assertEquals(homeId, response.getHomeId());
        verify(roomRepository).persist(org.mockito.ArgumentMatchers.any(Room.class));
    }

    @Test
    void deleteRoomDeletesWhenCurrentUserHasPermission() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder().id(roomId).home(Home.builder().id(UUID.randomUUID()).build()).build();

        when(roomRepository.findByIdOptional(roomId)).thenReturn(Optional.of(room));
        when(authService.getUserPermission(room)).thenReturn(Role.ADMIN);

        homeService.deleteRoom(roomId);

        verify(roomRepository).delete(room);
    }

    @Test
    void getRoomsByHomeIdReturnsMappedRoomResponses() {
        UUID homeId = UUID.randomUUID();
        Room room = Room.builder()
                .id(UUID.randomUUID())
                .name("Kitchen")
                .home(Home.builder().id(homeId).build())
                .build();

        when(roomRepository.findByHomeId(homeId)).thenReturn(List.of(room));

        List<RoomResponse> responses = homeService.getRoomsByHomeId(homeId);

        assertEquals(1, responses.size());
        assertEquals("Kitchen", responses.get(0).getName());
        assertEquals(homeId, responses.get(0).getHomeId());
    }

    @Test
    void getMyHomesReturnsMappedHomes() {
        UUID userId = UUID.randomUUID();
        User currentUser = User.builder().id(userId).username("owner").build();

        User memberUser = User.builder().id(UUID.randomUUID()).username("bob").build();
        Home home = Home.builder().id(UUID.randomUUID()).name("Home").build();

        HomeMember ownerMembership = HomeMember.builder()
                .id(new HomeMemberId(home.id, userId))
                .home(home)
                .user(currentUser)
                .role(Role.ADMIN)
                .build();
        HomeMember bobMembership = HomeMember.builder()
                .id(new HomeMemberId(home.id, memberUser.id))
                .home(home)
                .user(memberUser)
                .role(Role.MEMBER)
                .build();

        home.members = List.of(ownerMembership, bobMembership);

        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(homeMemberRepository.findByUserId(userId)).thenReturn(List.of(ownerMembership));

        List<HomeResponse> responses = homeService.getMyHomes();

        assertEquals(1, responses.size());
        assertEquals("Home", responses.get(0).getName());
        assertTrue(responses.get(0).getMemberUsernames().contains("owner"));
        assertTrue(responses.get(0).getMemberUsernames().contains("bob"));
    }

    @Test
    void getByIdThrowsWhenHomeDoesNotExist() {
        UUID homeId = UUID.randomUUID();
        when(homeRepository.findByIdOptional(homeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> homeService.getById(homeId));
    }
}
