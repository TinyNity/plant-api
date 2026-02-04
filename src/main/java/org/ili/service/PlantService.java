package org.ili.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.ili.dto.*;
import org.ili.entity.*;
import org.ili.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PlantService {

    @Inject
    PlantRepository plantRepository;

    @Inject
    RoomRepository roomRepository;

    @Inject
    HomeRepository homeRepository;

    @Inject
    CareLogRepository careLogRepository;

    @Inject
    UserRepository userRepository;

    // Simulation Auth
    private User getCurrentUser() {
        return userRepository.findByIdOptional(1L)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public PlantResponse createPlant(CreatePlantRequest request) {
        Room room = roomRepository.findByIdOptional(request.getRoomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));

        Plant plant = Plant.builder()
                .name(request.getName())
                .species(request.getSpecies())
                .wateringFrequency(request.getWateringFrequency())
                .lastWateredDate(request.getLastWateredDate())
                .room(room)
                .build();

        plantRepository.persist(plant);

        return mapToPlantResponse(plant);
    }

    @Transactional
    public PlantResponse updatePlant(Long id, UpdatePlantRequest request) {
        Plant plant = plantRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Plant not found"));

        if (request.getName() != null) {
            plant.name = request.getName();
        }
        if (request.getSpecies() != null) {
            plant.species = request.getSpecies();
        }
        if (request.getWateringFrequency() != null) {
            plant.wateringFrequency = request.getWateringFrequency();
        }
        if (request.getLastWateredDate() != null) {
            plant.lastWateredDate = request.getLastWateredDate();
        }
        if (request.getRoomId() != null) {
            Room room = roomRepository.findByIdOptional(request.getRoomId())
                    .orElseThrow(() -> new NotFoundException("Room not found"));
            plant.room = room;
        }

        plantRepository.persist(plant);
        return mapToPlantResponse(plant);
    }

    public PlantResponse getPlantById(Long id) {
        Plant plant = plantRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Plant not found"));
        return mapToPlantResponse(plant);
    }

    public List<PlantResponse> getAllPlants() {
        return plantRepository.listAll().stream()
                .map(this::mapToPlantResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addCareLog(Long plantId, CreateLogRequest request) {
        Plant plant = plantRepository.findByIdOptional(plantId)
                .orElseThrow(() -> new NotFoundException("Plant not found"));
        
        User currentUser = getCurrentUser();

        CareLog log = CareLog.builder()
                .plant(plant)
                .user(currentUser)
                .type(request.getType())
                .note(request.getNote())
                .date(LocalDateTime.now())
                .build();

        careLogRepository.persist(log);

        // Si c'est un arrosage, on met à jour la date de dernier arrosage de la plante
        if (request.getType() == CareLog.CareType.WATERING) {
            plant.lastWateredDate = log.date.toLocalDate();
            plantRepository.persist(plant); // Update plant
        }
    }

    private PlantResponse mapToPlantResponse(Plant plant) {
        return PlantResponse.builder()
                .id(plant.id)
                .name(plant.name)
                .species(plant.species)
                .wateringFrequency(plant.wateringFrequency)
                .lastWateredDate(plant.lastWateredDate)
                .roomId(plant.room.id)
                .roomName(plant.room.name)
                .build();
    }
}
