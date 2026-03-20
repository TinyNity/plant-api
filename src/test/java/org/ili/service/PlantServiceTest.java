package org.ili.service;

import io.quarkus.security.ForbiddenException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.PlantResponse;
import org.ili.dto.UpdatePlantRequest;
import org.ili.entity.CareLog;
import org.ili.entity.Home;
import org.ili.entity.Plant;
import org.ili.entity.Room;
import org.ili.entity.User;
import org.ili.enumeration.Role;
import org.ili.repository.CareLogRepository;
import org.ili.repository.HomeMemberRepository;
import org.ili.repository.HomeRepository;
import org.ili.repository.PlantRepository;
import org.ili.repository.RoomRepository;
import org.ili.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

    @Mock
    PlantRepository plantRepository;

    @Mock
    RoomRepository roomRepository;

    @Mock
    HomeRepository homeRepository;

    @Mock
    HomeMemberRepository homeMemberRepository;

    @Mock
    CareLogRepository careLogRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthService authService;

    private PlantService plantService;

    @BeforeEach
    void setUp() {
        plantService = new PlantService();
        plantService.plantRepository = plantRepository;
        plantService.roomRepository = roomRepository;
        plantService.homeRepository = homeRepository;
        plantService.homeMemberRepository = homeMemberRepository;
        plantService.careLogRepository = careLogRepository;
        plantService.userRepository = userRepository;
        plantService.authService = authService;
    }

    @Test
    void createPlantRequiresRoomId() {
        CreatePlantRequest request = new CreatePlantRequest();
        request.setName("Ficus");

        assertThrows(BadRequestException.class, () -> plantService.createPlant(request));
    }

    @Test
    void createPlantRequiresName() {
        CreatePlantRequest request = new CreatePlantRequest();
        request.setRoomId(UUID.randomUUID());
        request.setName(" ");

        assertThrows(BadRequestException.class, () -> plantService.createPlant(request));
    }

    @Test
    void createPlantThrowsForbiddenWhenCurrentUserIsGuest() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder().id(roomId).name("Living Room").home(Home.builder().id(UUID.randomUUID()).build()).build();

        CreatePlantRequest request = new CreatePlantRequest();
        request.setRoomId(roomId);
        request.setName("Monstera");

        when(roomRepository.findByIdOptional(roomId)).thenReturn(Optional.of(room));
        when(authService.getUserPermission(room)).thenReturn(Role.GUEST);

        assertThrows(ForbiddenException.class, () -> plantService.createPlant(request));
    }

    @Test
    void createPlantPersistsAndReturnsMappedResponse() {
        UUID homeId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .id(roomId)
                .name("Living Room")
                .home(Home.builder().id(homeId).name("Home").build())
                .build();

        LocalDate lastWatered = LocalDate.now().minusDays(2);
        CreatePlantRequest request = new CreatePlantRequest("Monstera", "Deliciosa", 1, lastWatered, roomId);

        when(roomRepository.findByIdOptional(roomId)).thenReturn(Optional.of(room));
        when(authService.getUserPermission(room)).thenReturn(Role.ADMIN);

        PlantResponse response = plantService.createPlant(request);

        assertEquals("Monstera", response.getName());
        assertEquals(roomId, response.getRoomId());
        assertEquals(homeId, response.getHomeId());
        assertTrue(response.getNeedsWatering());
        verify(plantRepository).persist(any(Plant.class));
    }

    @Test
    void updatePlantAsGuestOnlyUpdatesLastWateredDate() {
        UUID homeId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID plantId = UUID.randomUUID();

        Room room = Room.builder()
                .id(roomId)
                .name("Living Room")
                .home(Home.builder().id(homeId).build())
                .build();

        Plant plant = Plant.builder()
                .id(plantId)
                .name("Old Name")
                .species("Old Species")
                .wateringFrequency(7)
                .room(room)
                .build();

        UpdatePlantRequest request = new UpdatePlantRequest();
        request.setName("New Name");
        request.setLastWateredDate(LocalDate.now());

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenReturn(Role.GUEST);

        PlantResponse response = plantService.updatePlant(plantId, request);

        assertEquals("Old Name", response.getName());
        assertEquals(LocalDate.now(), response.getLastWateredDate());
        verify(plantRepository).persist(plant);
    }

    @Test
    void deletePlantThrowsForbiddenForGuest() {
        UUID plantId = UUID.randomUUID();
        Plant plant = Plant.builder().id(plantId).room(Room.builder().home(Home.builder().id(UUID.randomUUID()).build()).build()).build();

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenReturn(Role.GUEST);

        assertThrows(ForbiddenException.class, () -> plantService.deletePlant(plantId));
    }

    @Test
    void deletePlantDeletesForAdmin() {
        UUID plantId = UUID.randomUUID();
        Plant plant = Plant.builder().id(plantId).room(Room.builder().home(Home.builder().id(UUID.randomUUID()).build()).build()).build();

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenReturn(Role.ADMIN);

        plantService.deletePlant(plantId);

        verify(plantRepository).delete(plant);
    }

    @Test
    void getPlantByIdThrowsForbiddenWhenUserHasNoAccess() {
        UUID plantId = UUID.randomUUID();
        Plant plant = Plant.builder()
                .id(plantId)
                .room(Room.builder().home(Home.builder().id(UUID.randomUUID()).build()).build())
                .build();

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenThrow(new IllegalArgumentException("No access"));

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> plantService.getPlantById(plantId));

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void getPlantByIdThrowsWhenPlantNotFound() {
        UUID plantId = UUID.randomUUID();
        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> plantService.getPlantById(plantId));
    }

    @Test
    void updatePlantThrowsNotFoundWhenPlantMissing() {
        UUID plantId = UUID.randomUUID();
        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> plantService.updatePlant(plantId, new UpdatePlantRequest()));
    }

    @Test
    void updatePlantThrowsForbiddenWhenTargetRoomNotAccessible() {
        UUID homeId = UUID.randomUUID();
        UUID sourceRoomId = UUID.randomUUID();
        UUID targetRoomId = UUID.randomUUID();
        UUID plantId = UUID.randomUUID();

        Room sourceRoom = Room.builder().id(sourceRoomId).home(Home.builder().id(homeId).build()).build();
        Room targetRoom = Room.builder().id(targetRoomId).home(Home.builder().id(UUID.randomUUID()).build()).build();
        Plant plant = Plant.builder().id(plantId).name("Plant").room(sourceRoom).build();

        UpdatePlantRequest request = new UpdatePlantRequest();
        request.setRoomId(targetRoomId);

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenReturn(Role.ADMIN);
        when(roomRepository.findByIdOptional(targetRoomId)).thenReturn(Optional.of(targetRoom));
        when(authService.getUserPermission(targetRoom)).thenThrow(new IllegalArgumentException("No access"));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> plantService.updatePlant(plantId, request));

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void updatePlantUpdatesEditableFieldsForAdmin() {
        UUID homeId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID plantId = UUID.randomUUID();

        Room room = Room.builder().id(roomId).name("Room").home(Home.builder().id(homeId).build()).build();
        Plant plant = Plant.builder()
                .id(plantId)
                .name("Old")
                .species("OldSpecies")
                .wateringFrequency(3)
                .photoUrl("old.png")
                .room(room)
                .build();

        UpdatePlantRequest request = new UpdatePlantRequest();
        request.setName("NewName");
        request.setSpecies("Monstera");
        request.setWateringFrequency(9);
        request.setPhotoUrl("new.png");
        request.setPottedDate(LocalDate.now().minusDays(10));
        request.setDeceased(true);

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenReturn(Role.ADMIN);

        PlantResponse response = plantService.updatePlant(plantId, request);

        assertEquals("NewName", response.getName());
        assertEquals("Monstera", response.getSpecies());
        assertEquals(9, response.getWateringFrequency());
        assertEquals("new.png", response.getPhotoUrl());
        assertTrue(response.getDeceased());
    }

    @Test
    void getAllPlantsFiltersInaccessiblePlants() {
        UUID allowedId = UUID.randomUUID();
        UUID deniedId = UUID.randomUUID();

        Room room = Room.builder().id(UUID.randomUUID()).name("Room").home(Home.builder().id(UUID.randomUUID()).build()).build();
        Plant allowedPlant = Plant.builder().id(allowedId).name("Allowed").wateringFrequency(1).lastWateredDate(LocalDate.now().minusDays(2)).room(room).build();
        Plant deniedPlant = Plant.builder().id(deniedId).name("Denied").wateringFrequency(1).lastWateredDate(LocalDate.now()).room(room).build();

        when(plantRepository.listAll()).thenReturn(List.of(allowedPlant, deniedPlant));
        when(authService.getUserPermission(allowedPlant)).thenReturn(Role.ADMIN);
        when(authService.getUserPermission(deniedPlant)).thenThrow(new IllegalArgumentException("No access"));

        List<PlantResponse> visiblePlants = plantService.getAllPlants();

        assertEquals(1, visiblePlants.size());
        assertEquals("Allowed", visiblePlants.get(0).getName());
    }

    @Test
    void addCareLogWithWateringUpdatesLastWateredDate() {
        UUID plantId = UUID.randomUUID();
        Plant plant = Plant.builder()
                .id(plantId)
                .name("Ficus")
                .wateringFrequency(7)
                .room(Room.builder().home(Home.builder().id(UUID.randomUUID()).build()).build())
                .build();

        User currentUser = User.builder().id(UUID.randomUUID()).username("owner").build();

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getCurrentUser()).thenReturn(currentUser);

        CreateLogRequest request = new CreateLogRequest(CareLog.CareType.WATERING, "Watered");

        plantService.addCareLog(plantId, request);

        assertEquals(LocalDate.now(), plant.getLastWateredDate());
        verify(careLogRepository).persist(any(CareLog.class));
        verify(plantRepository).persist(plant);
    }

    @Test
    void getCareLogsWrapsPermissionErrorAsForbiddenResponse() {
        UUID plantId = UUID.randomUUID();
        Plant plant = Plant.builder().id(plantId).room(Room.builder().home(Home.builder().id(UUID.randomUUID()).build()).build()).build();

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenThrow(new IllegalArgumentException("No access"));

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> plantService.getCareLogs(plantId));

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void getCareLogsMapsRepositoryResults() {
        UUID plantId = UUID.randomUUID();
        Plant plant = Plant.builder().id(plantId).room(Room.builder().home(Home.builder().id(UUID.randomUUID()).build()).build()).build();

        CareLog log = CareLog.builder()
                .id(UUID.randomUUID())
                .type(CareLog.CareType.PRUNING)
                .note("Trimmed leaves")
                .build();

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenReturn(Role.MEMBER);
        when(careLogRepository.findByPlantId(plantId)).thenReturn(List.of(log));

        var responses = plantService.getCareLogs(plantId);

        assertEquals(1, responses.size());
        assertEquals(CareLog.CareType.PRUNING, responses.get(0).getType());
        assertEquals("Trimmed leaves", responses.get(0).getNote());
    }

    @Test
    void addCareLogWithoutPermissionWrapsAsForbiddenResponse() {
        UUID plantId = UUID.randomUUID();
        Plant plant = Plant.builder().id(plantId).room(Room.builder().home(Home.builder().id(UUID.randomUUID()).build()).build()).build();

        when(plantRepository.findByIdOptional(plantId)).thenReturn(Optional.of(plant));
        when(authService.getUserPermission(plant)).thenThrow(new IllegalArgumentException("No access"));

        CreateLogRequest request = new CreateLogRequest(CareLog.CareType.OTHER, "Note");

        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> plantService.addCareLog(plantId, request));

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    void createPlantDefaultsDeceasedToFalseWhenNullInRequest() {
        UUID homeId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .id(roomId)
                .name("Living Room")
                .home(Home.builder().id(homeId).name("Home").build())
                .build();

        CreatePlantRequest request = new CreatePlantRequest();
        request.setRoomId(roomId);
        request.setName("Pothos");
        request.setDeceased(null);

        when(roomRepository.findByIdOptional(roomId)).thenReturn(Optional.of(room));
        when(authService.getUserPermission(room)).thenReturn(Role.ADMIN);

        plantService.createPlant(request);

        ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
        verify(plantRepository).persist(captor.capture());
        assertNotNull(captor.getValue().getDeceased());
        assertEquals(false, captor.getValue().getDeceased());
    }
}
