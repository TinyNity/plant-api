package org.ili.service.dev;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.datafaker.Faker;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.ili.dto.dev.DevSeedRequest;
import org.ili.dto.dev.DevSeedResponse;
import org.ili.dto.dev.SeededUserCredential;
import org.ili.entity.CareLog;
import org.ili.entity.Home;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;
import org.ili.entity.Plant;
import org.ili.entity.Room;
import org.ili.entity.User;
import org.ili.enumeration.Role;
import org.ili.repository.CareLogRepository;
import org.ili.repository.HomeMemberRepository;
import org.ili.repository.HomeRepository;
import org.ili.repository.PlantRepository;
import org.ili.repository.RefreshTokenRepository;
import org.ili.repository.RoomRepository;
import org.ili.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
/**
 * Seeds development data for local environments.
 */

@ApplicationScoped
public class DevDataSeederService {

    private static final String[] ROOM_NAMES = {
            "Salon", "Cuisine", "Chambre", "Bureau", "Salle de bain", "Balcon", "Entrée", "Véranda"
    };

    private static final String[] PLANT_NAMES = {
            "Monstera", "Ficus", "Pothos", "Enculus", "Calathea", "Aloe", "Yucca", "Philodendron", "Dracaena"
    };

    private static final String[] PLANT_SPECIES = {
            "Monstera Deliciosa", "Ficus Benjamina", "Epipremnum Aureum", "Calathea Orbifolia",
            "Aloe Barbadensis", "Yucca Elephantipes", "Philodendron Brasil", "Dracaena Marginata"
    };

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

    @ConfigProperty(name = "plante.dev-seeder.default-user-count", defaultValue = "12")
    int defaultUserCount;

    @ConfigProperty(name = "plante.dev-seeder.default-homes-per-user", defaultValue = "2")
    int defaultHomesPerUser;

    @ConfigProperty(name = "plante.dev-seeder.default-additional-members-per-home", defaultValue = "2")
    int defaultAdditionalMembersPerHome;

    @ConfigProperty(name = "plante.dev-seeder.default-rooms-per-home", defaultValue = "3")
    int defaultRoomsPerHome;

    @ConfigProperty(name = "plante.dev-seeder.default-plants-per-room", defaultValue = "6")
    int defaultPlantsPerRoom;

    @ConfigProperty(name = "plante.dev-seeder.default-logs-per-plant", defaultValue = "4")
    int defaultLogsPerPlant;

    @ConfigProperty(name = "plante.dev-seeder.default-user-password", defaultValue = "Password123!")
    String defaultUserPassword;

    /**
     * Generates a full dataset (users, homes, rooms, plants and care logs).
     *
     * @param request optional seeding request overriding defaults.
     * @return summary of created records and sample credentials.
     */
    @Transactional
    public DevSeedResponse seed(DevSeedRequest request) {
        SeedPlan plan = SeedPlan.from(request, defaultUserCount, defaultHomesPerUser, defaultAdditionalMembersPerHome,
                defaultRoomsPerHome, defaultPlantsPerRoom, defaultLogsPerPlant);

        if (plan.replaceExisting()) {
            clearExistingData();
        }

        Faker faker = new Faker(Locale.FRANCE);
        Set<String> usernames = new HashSet<>();
        Set<String> emails = new HashSet<>();
        List<SeededUserCredential> sampleUsers = new ArrayList<>();
        List<User> users = createUsers(plan.userCount(), faker, usernames, emails, sampleUsers);

        int homesCreated = 0;
        int membershipsCreated = 0;
        int roomsCreated = 0;
        int plantsCreated = 0;
        int careLogsCreated = 0;

        for (User owner : users) {
            for (int homeIndex = 0; homeIndex < plan.homesPerUser(); homeIndex++) {
                Home home = Home.builder()
                        .name(buildHomeName(faker, homeIndex))
                        .build();
                homeRepository.persist(home);
                homesCreated++;

                membershipsCreated += createOwnerMembership(home, owner);
                List<User> homeUsers = new ArrayList<>();
                homeUsers.add(owner);
                membershipsCreated += addAdditionalMembers(home, owner, users, homeUsers, plan.additionalMembersPerHome());

                for (int roomIndex = 0; roomIndex < plan.roomsPerHome(); roomIndex++) {
                    Room room = Room.builder()
                            .name(buildRoomName(faker, roomIndex))
                            .home(home)
                            .build();
                    roomRepository.persist(room);
                    roomsCreated++;

                    for (int plantIndex = 0; plantIndex < plan.plantsPerRoom(); plantIndex++) {
                        Plant plant = buildPlant(faker, room, plantIndex);
                        plantRepository.persist(plant);
                        plantsCreated++;

                        careLogsCreated += createCareLogs(faker, plant, homeUsers, plan.logsPerPlant());
                    }
                }
            }
        }

        return DevSeedResponse.builder()
                .replacedExisting(plan.replaceExisting())
                .userCount(users.size())
                .homesCreated(homesCreated)
                .membershipsCreated(membershipsCreated)
                .roomsCreated(roomsCreated)
                .plantsCreated(plantsCreated)
                .careLogsCreated(careLogsCreated)
                .defaultPassword(defaultUserPassword)
                .sampleUsers(sampleUsers)
                .build();
    }

    /**
     * Creates users and stores the first credentials as sample output.
     */
    private List<User> createUsers(int userCount, Faker faker, Set<String> usernames, Set<String> emails,
            List<SeededUserCredential> sampleUsers) {
        List<User> users = new ArrayList<>(userCount);
        for (int index = 0; index < userCount; index++) {
            String username = nextUniqueUsername(faker, usernames);
            String email = nextUniqueEmail(faker, emails, username);

            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(BcryptUtil.bcryptHash(defaultUserPassword))
                    .build();
            userRepository.persist(user);
            users.add(user);

            if (sampleUsers.size() < 5) {
                sampleUsers.add(SeededUserCredential.builder()
                        .username(username)
                        .email(email)
                        .build());
            }
        }
        return users;
    }

    /**
     * Creates the owner membership for a newly created home.
     */
    private int createOwnerMembership(Home home, User owner) {
        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(home.id, owner.id))
                .home(home)
                .user(owner)
                .role(Role.ADMIN)
                .build();
        homeMemberRepository.persist(membership);
        return 1;
    }

    /**
     * Adds additional members to a home from available users.
     */
    private int addAdditionalMembers(Home home, User owner, List<User> users, List<User> homeUsers, int additionalMembers) {
        List<User> candidates = new ArrayList<>(users);
        candidates.removeIf(candidate -> candidate.id.equals(owner.id));
        Collections.shuffle(candidates);

        int membersToAdd = Math.min(additionalMembers, candidates.size());
        for (int index = 0; index < membersToAdd; index++) {
            User member = candidates.get(index);
            HomeMember membership = HomeMember.builder()
                    .id(new HomeMemberId(home.id, member.id))
                    .home(home)
                    .user(member)
                    .role(Role.MEMBER)
                    .build();
            homeMemberRepository.persist(membership);
            homeUsers.add(member);
        }
        return membersToAdd;
    }

    /**
     * Builds a randomized plant assigned to a room.
     */
    private Plant buildPlant(Faker faker, Room room, int plantIndex) {
        int speciesIndex = ThreadLocalRandom.current().nextInt(PLANT_SPECIES.length);
        int wateringFrequency = ThreadLocalRandom.current().nextInt(2, 22);
        LocalDate lastWateredDate = LocalDate.now().minusDays(ThreadLocalRandom.current().nextLong(1, wateringFrequency + 1L));

        return Plant.builder()
                .name(PLANT_NAMES[plantIndex % PLANT_NAMES.length] + " " + faker.number().digits(3))
                .species(PLANT_SPECIES[speciesIndex])
                .wateringFrequency(wateringFrequency)
                .lastWateredDate(lastWateredDate)
                .room(room)
                .build();
    }

    /**
     * Creates random care logs for one plant and updates last watering date.
     */
    private int createCareLogs(Faker faker, Plant plant, List<User> homeUsers, int logsPerPlant) {
        LocalDate latestWateringDate = plant.lastWateredDate;

        for (int index = 0; index < logsPerPlant; index++) {
            LocalDateTime logDate = LocalDateTime.now()
                    .minusDays(ThreadLocalRandom.current().nextLong(0, 90))
                    .minusHours(ThreadLocalRandom.current().nextLong(0, 24));
            CareLog.CareType type = randomCareType();
            User actor = homeUsers.get(ThreadLocalRandom.current().nextInt(homeUsers.size()));

            CareLog careLog = CareLog.builder()
                    .date(logDate)
                    .type(type)
                    .note(buildCareNote(faker, type))
                    .plant(plant)
                    .user(actor)
                    .build();
            careLogRepository.persist(careLog);

            if (type == CareLog.CareType.WATERING && logDate.toLocalDate().isAfter(latestWateringDate)) {
                latestWateringDate = logDate.toLocalDate();
            }
        }

        plant.lastWateredDate = latestWateringDate;
        plantRepository.persist(plant);
        return logsPerPlant;
    }

    /**
     * Removes all persisted data in dependency order.
     */
    private void clearExistingData() {
        careLogRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        homeMemberRepository.deleteAll();
        plantRepository.deleteAll();
        roomRepository.deleteAll();
        homeRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Generates a display name for a seeded home.
     */
    private String buildHomeName(Faker faker, int homeIndex) {
        return "Maison " + faker.color().name() + " " + (homeIndex + 1);
    }

    /**
     * Generates a display name for a seeded room.
     */
    private String buildRoomName(Faker faker, int roomIndex) {
        return ROOM_NAMES[roomIndex % ROOM_NAMES.length] + " " + faker.number().digit();
    }

    /**
     * Generates a readable note matching the care log type.
     */
    private String buildCareNote(Faker faker, CareLog.CareType type) {
        return switch (type) {
            case WATERING -> "Arrosage: " + faker.lorem().sentence(4);
            case FERTILIZING -> "Engrais: " + faker.lorem().sentence(4);
            case PRUNING -> "Taille: " + faker.lorem().sentence(4);
            case MISTING -> "Brumisation: " + faker.lorem().sentence(4);
            case OTHER -> faker.lorem().sentence(5);
        };
    }

    /**
     * Picks a random care type.
     */
    private CareLog.CareType randomCareType() {
        CareLog.CareType[] values = CareLog.CareType.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    /**
     * Generates a unique username not already persisted.
     */
    private String nextUniqueUsername(Faker faker, Set<String> usernames) {
        while (true) {
            String candidate = sanitize(faker.name().firstName()) + "_" + sanitize(faker.name().lastName())
                    + ThreadLocalRandom.current().nextInt(100, 999);
            if (!candidate.isBlank() && usernames.add(candidate)
                    && userRepository.find("username", candidate).firstResultOptional().isEmpty()) {
                return candidate.toLowerCase(Locale.ROOT);
            }
        }
    }

    /**
     * Generates a unique email not already persisted.
     */
    private String nextUniqueEmail(Faker faker, Set<String> emails, String username) {
        while (true) {
            String candidate = username + "." + sanitize(faker.number().digits(4)) + "@example.test";
            if (emails.add(candidate) && userRepository.findByEmail(candidate).isEmpty()) {
                return candidate.toLowerCase(Locale.ROOT);
            }
        }
    }

    /**
     * Removes non alphanumeric characters from generated text.
     */
    private String sanitize(String value) {
        return value == null ? "user" : value.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Immutable seeding plan derived from request and defaults.
     */
    private record SeedPlan(int userCount, int homesPerUser, int additionalMembersPerHome, int roomsPerHome,
                            int plantsPerRoom, int logsPerPlant, boolean replaceExisting) {

        /**
         * Builds a normalized plan from an optional request.
         */
        private static SeedPlan from(DevSeedRequest request, int defaultUserCount, int defaultHomesPerUser,
                int defaultAdditionalMembersPerHome, int defaultRoomsPerHome, int defaultPlantsPerRoom,
                int defaultLogsPerPlant) {
            DevSeedRequest seedRequest = request == null ? new DevSeedRequest() : request;
            return new SeedPlan(
                    positiveOrDefault(seedRequest.getUserCount(), defaultUserCount),
                    positiveOrDefault(seedRequest.getHomesPerUser(), defaultHomesPerUser),
                    nonNegativeOrDefault(seedRequest.getAdditionalMembersPerHome(), defaultAdditionalMembersPerHome),
                    positiveOrDefault(seedRequest.getRoomsPerHome(), defaultRoomsPerHome),
                    positiveOrDefault(seedRequest.getPlantsPerRoom(), defaultPlantsPerRoom),
                    nonNegativeOrDefault(seedRequest.getLogsPerPlant(), defaultLogsPerPlant),
                    Boolean.TRUE.equals(seedRequest.getReplaceExisting())
            );
        }

        /**
         * Returns a strictly positive value, or fallback when null.
         */
        private static int positiveOrDefault(Integer requested, int fallback) {
            return requested == null ? fallback : Math.max(1, requested);
        }

        /**
         * Returns a non-negative value, or fallback when null.
         */
        private static int nonNegativeOrDefault(Integer requested, int fallback) {
            return requested == null ? fallback : Math.max(0, requested);
        }
    }
}

