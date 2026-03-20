package org.ili.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.ili.PostgresTestResource;
import org.ili.entity.CareLog;
import org.ili.entity.Home;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;
import org.ili.entity.Plant;
import org.ili.entity.RefreshToken;
import org.ili.entity.Room;
import org.ili.entity.User;
import org.ili.enumeration.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class RepositoryQueryTest {

    @Inject
    UserRepository userRepository;

    @Inject
    HomeRepository homeRepository;

    @Inject
    HomeMemberRepository homeMemberRepository;

    @Inject
    RoomRepository roomRepository;

    @Inject
    PlantRepository plantRepository;

    @Inject
    CareLogRepository careLogRepository;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @Test
    @Transactional
    void findByEmailReturnsPersistedUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = User.builder()
                .email("repo-" + suffix + "@example.com")
                .username("repo_user_" + suffix)
                .password("hashed")
                .build();
        userRepository.persist(user);

        var found = userRepository.findByEmail(user.email);

        assertTrue(found.isPresent());
        assertEquals(user.email, found.get().email);
    }

    @Test
    @Transactional
    void refreshTokenQueriesFindAndDeleteByUserId() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = User.builder()
                .email("token-" + suffix + "@example.com")
                .username("token_user_" + suffix)
                .password("hashed")
                .build();
        userRepository.persist(user);

        String tokenValue = "refresh-" + UUID.randomUUID();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        refreshTokenRepository.persist(refreshToken);

        var found = refreshTokenRepository.findByToken(tokenValue);
        long deleted = refreshTokenRepository.deleteAllByUserId(user.id);

        assertTrue(found.isPresent());
        assertEquals(user.id, found.get().user.id);
        assertEquals(1L, deleted);
    }

    @Test
    @Transactional
    void homeMemberQueriesReturnByUserAndHome() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User owner = User.builder()
                .email("owner-" + suffix + "@example.com")
                .username("owner_" + suffix)
                .password("hashed")
                .build();
        userRepository.persist(owner);

        Home home = Home.builder().name("Home " + suffix).build();
        homeRepository.persist(home);

        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(home.id, owner.id))
                .home(home)
                .user(owner)
                .role(Role.ADMIN)
                .build();
        homeMemberRepository.persist(membership);

        List<HomeMember> byUser = homeMemberRepository.findByUserId(owner.id);
        List<HomeMember> byHome = homeMemberRepository.findByHomeId(home.id);

        assertEquals(1, byUser.size());
        assertEquals(1, byHome.size());
        assertEquals(Role.ADMIN, byHome.get(0).role);
    }

    @Test
    @Transactional
    void roomAndPlantQueriesReturnEntitiesByParentIds() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User owner = User.builder()
                .email("plant-" + suffix + "@example.com")
                .username("plant_owner_" + suffix)
                .password("hashed")
                .build();
        userRepository.persist(owner);

        Home home = Home.builder().name("Plant Home " + suffix).build();
        homeRepository.persist(home);

        homeMemberRepository.persist(HomeMember.builder()
                .id(new HomeMemberId(home.id, owner.id))
                .home(home)
                .user(owner)
                .role(Role.ADMIN)
                .build());

        Room room = Room.builder().name("Room " + suffix).home(home).build();
        roomRepository.persist(room);

        Plant plant = Plant.builder()
                .name("Pothos")
                .species("Epipremnum Aureum")
                .wateringFrequency(7)
                .lastWateredDate(LocalDate.now().minusDays(2))
                .room(room)
                .build();
        plantRepository.persist(plant);

        List<Room> rooms = roomRepository.findByHomeId(home.id);
        List<Plant> plants = plantRepository.findByRoomId(room.id);

        assertEquals(1, rooms.size());
        assertEquals(1, plants.size());
        assertEquals(room.id, plants.get(0).room.id);
    }

    @Test
    @Transactional
    void careLogQueryReturnsLogsByPlantId() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User owner = User.builder()
                .email("log-" + suffix + "@example.com")
                .username("log_owner_" + suffix)
                .password("hashed")
                .build();
        userRepository.persist(owner);

        Home home = Home.builder().name("Log Home " + suffix).build();
        homeRepository.persist(home);

        Room room = Room.builder().name("Log Room " + suffix).home(home).build();
        roomRepository.persist(room);

        Plant plant = Plant.builder()
                .name("Ficus")
                .wateringFrequency(5)
                .lastWateredDate(LocalDate.now().minusDays(1))
                .room(room)
                .build();
        plantRepository.persist(plant);

        CareLog log = CareLog.builder()
                .date(LocalDateTime.now())
                .type(CareLog.CareType.WATERING)
                .note("Watered")
                .plant(plant)
                .user(owner)
                .build();
        careLogRepository.persist(log);

        List<CareLog> logs = careLogRepository.findByPlantId(plant.id);

        assertEquals(1, logs.size());
        assertEquals(CareLog.CareType.WATERING, logs.get(0).type);
    }
}
