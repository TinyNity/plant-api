package org.ili.service;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.ili.dto.*;
import org.ili.entity.*;
import org.ili.enumeration.Role;
import org.ili.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * Service handling plant lifecycle and plant care log operations.
 */

@ApplicationScoped
public class PlantService {

    @Inject
    PlantRepository plantRepository;

    @Inject
    RoomRepository roomRepository;

    @Inject
    HomeRepository homeRepository;

    @Inject
    HomeMemberRepository homeMemberRepository;

    @Inject
    CareLogRepository careLogRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    AuthService authService;

    /**
     * Creates a new plant after validating payload and user permissions.
     *
     * @param request plant creation payload.
     * @return the created plant response.
     */
    @Transactional
    public PlantResponse createPlant(CreatePlantRequest request) {
        if (request.getRoomId() == null) {
            throw new BadRequestException("roomId is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("name is required");
        }

        Room room = roomRepository.findByIdOptional(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Role currentUserPermission;
        try {
            currentUserPermission = authService.getUserPermission(room);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Current user does not have access to this home", e, Response.Status.FORBIDDEN);
        }

        if (currentUserPermission == Role.GUEST) {
            throw new ForbiddenException("Current user does not have enough permission");
        }

        Plant plant = Plant.builder()
                .name(request.getName())
                .species(request.getSpecies())
                .nickname(request.getNickname())
                .wateringFrequency(request.getWateringFrequency())
                .lastWateredDate(request.getLastWateredDate())
                .photoUrl(request.getPhotoUrl())
                .pottedDate(request.getPottedDate())
                .deceased(Boolean.TRUE.equals(request.getDeceased()))
                .room(room)
                .build();

        plantRepository.persist(plant);
        return mapToPlantResponse(plant);
    }

    /**
     * Updates an existing plant.
     * <p>
     * Guest users can only update watering date while members/admins can
     * update additional fields.
     *
     * @param id the plant identifier.
     * @param request payload with fields to update.
     * @return the updated plant response.
     */
    @Transactional
    public PlantResponse updatePlant(UUID id, UpdatePlantRequest request) {
        Plant plant = plantRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Plant not found"));

        Role currentUserPermission;
        try {
            currentUserPermission = authService.getUserPermission(plant);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Current user does not have access to this plant", e, Response.Status.FORBIDDEN);
        }

        if (request.getLastWateredDate() != null) {
            plant.lastWateredDate = request.getLastWateredDate();
        }

        if (currentUserPermission == Role.GUEST) {
            plantRepository.persist(plant);
            return mapToPlantResponse(plant);
        }

        if (request.getName() != null) {
            plant.name = request.getName();
        }
        if (request.getSpecies() != null) {
            plant.species = request.getSpecies();
        }
        if (request.getNickname() != null) {
            plant.nickname = request.getNickname();
        }
        if (request.getWateringFrequency() != null) {
            plant.wateringFrequency = request.getWateringFrequency();
        }
        if (request.getPhotoUrl() != null) {
            plant.photoUrl = request.getPhotoUrl();
        }
        if (request.getPottedDate() != null) {
            plant.pottedDate = request.getPottedDate();
        }
        if (request.getDeceased() != null) {
            plant.deceased = request.getDeceased();
        }
        if (request.getRoomId() != null) {
            Room room = roomRepository.findByIdOptional(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));

            try {
                authService.getUserPermission(room);
            } catch (IllegalArgumentException e) {
                throw new WebApplicationException("Current user does not have access to target room", e, Response.Status.FORBIDDEN);
            }

            plant.room = room;
        }

        plantRepository.persist(plant);
        return mapToPlantResponse(plant);
    }

    /**
     * Deletes a plant if the current user has sufficient permissions.
     *
     * @param id the plant identifier.
     */
    @Transactional
    public void deletePlant(UUID id) {
        Plant plant = plantRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Plant not found"));

        Role currentUserPermission;
        try {
            currentUserPermission = authService.getUserPermission(plant);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Current user does not have access to this plant", e, Response.Status.FORBIDDEN);
        }

        if (currentUserPermission == Role.GUEST) {
            throw new ForbiddenException("Current user does not have enough permission");
        }

        plantRepository.delete(plant);
    }

    /**
     * Retrieves one plant by ID and validates current user access.
     *
     * @param id the plant identifier.
     * @return the mapped plant response.
     */
    public PlantResponse getPlantById(UUID id) {
        Plant plant = plantRepository.findByIdOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Plant not found"));
        try {
            authService.getUserPermission(plant);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Current user does not have access to this plant", e, Response.Status.FORBIDDEN);
        }
        return mapToPlantResponse(plant);
    }

    /**
     * Lists all plants accessible by the current user.
     *
     * @return accessible plants.
     */
    public List<PlantResponse> getAllPlants() {
        return plantRepository.listAll().stream()
                .filter(plant -> {
                    try {
                        authService.getUserPermission(plant);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .map(this::mapToPlantResponse)
                .collect(Collectors.toList());
    }

    /**
     * Adds a care log to a plant.
     * <p>
     * If the log type is watering, the plant's last watered date is updated.
     *
     * @param plantId the plant identifier.
     * @param request care log payload.
     */
    @Transactional
    public void addCareLog(UUID plantId, CreateLogRequest request) {
        Plant plant = plantRepository.findByIdOptional(plantId)
                .orElseThrow(() -> new IllegalArgumentException("Plant not found"));

        try {
            authService.getUserPermission(plant);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Current user does not have access to this plant", e, Response.Status.FORBIDDEN);
        }

        User currentUser = authService.getCurrentUser();

        CareLog log = CareLog.builder()
                .plant(plant)
                .user(currentUser)
                .type(request.getType())
                .note(request.getNote())
                .date(LocalDateTime.now())
                .build();

        careLogRepository.persist(log);

        if (request.getType() == CareLog.CareType.WATERING) {
            plant.lastWateredDate = log.date.toLocalDate();
            plantRepository.persist(plant);
        }
    }

    /**
     * Lists care logs for a plant accessible by the current user.
     *
     * @param plantId the plant identifier.
     * @return plant care logs.
     */
    public List<CareLogResponse> getCareLogs(UUID plantId) {
        Plant plant = plantRepository.findByIdOptional(plantId)
                .orElseThrow(() -> new IllegalArgumentException("Plant not found"));

        try {
            authService.getUserPermission(plant);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Current user does not have access to this plant", e, Response.Status.FORBIDDEN);
        }

        return careLogRepository.findByPlantId(plantId).stream()
                .map(log -> CareLogResponse.builder()
                        .id(log.id)
                        .type(log.type)
                        .note(log.note)
                        .date(log.date)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Maps a {@link Plant} entity to API response format.
     *
     * @param plant plant entity to map.
     * @return mapped response.
     */
    private PlantResponse mapToPlantResponse(Plant plant) {
        LocalDate nextWateringDate = computeNextWateringDate(plant);
        Boolean needsWatering = nextWateringDate != null && !nextWateringDate.isAfter(LocalDate.now());

        return PlantResponse.builder()
                .id(plant.id)
                .name(plant.name)
                .species(plant.species)
                .nickname(plant.nickname)
                .wateringFrequency(plant.wateringFrequency)
                .lastWateredDate(plant.lastWateredDate)
                .photoUrl(plant.photoUrl)
                .pottedDate(plant.pottedDate)
                .deceased(Boolean.TRUE.equals(plant.deceased))
                .nextWateringDate(nextWateringDate)
                .needsWatering(needsWatering)
                .roomId(plant.room.id)
                .roomName(plant.room.name)
                .homeId(plant.room.home.id)
                .build();
    }

    /**
     * Computes the next watering date based on the plant history.
     *
     * @param plant plant entity.
     * @return next watering date or {@code null} when unknown.
     */
    private LocalDate computeNextWateringDate(Plant plant) {
        if (plant.wateringFrequency == null || plant.wateringFrequency <= 0) {
            return null;
        }

        if (plant.lastWateredDate != null) {
            return plant.lastWateredDate.plusDays(plant.wateringFrequency);
        }

        if (plant.pottedDate != null) {
            return plant.pottedDate.plusDays(plant.wateringFrequency);
        }

        return null;
    }
}

