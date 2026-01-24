package org.ili.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.ili.dto.CreateLogRequest;
import org.ili.dto.CreatePlantRequest;
import org.ili.dto.PlantResponse;
import org.ili.dto.RoomResponse;
import org.ili.entity.CareLog;
import org.ili.entity.Plant;
import org.ili.entity.Room;
import org.ili.entity.User;
import org.ili.repository.CareLogRepository;
import org.ili.repository.PlantRepository;
import org.ili.repository.RoomRepository;
import org.ili.repository.UserRepository;

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
    CareLogRepository careLogRepository;

    @Inject
    UserRepository userRepository;

    // Simulation Auth
    private User getCurrentUser() {
        return userRepository.findByIdOptional(1L)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<RoomResponse> getRoomsByHomeId(Long homeId) {
        return roomRepository.findByHomeId(homeId).stream()
                .map(room -> RoomResponse.builder()
                        .id(room.id)
                        .name(room.name)
                        .homeId(room.home.id)
                        .build())
                .collect(Collectors.toList());
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

    public PlantResponse getPlantById(Long id) {
        Plant plant = plantRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Plant not found"));
        return mapToPlantResponse(plant);
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
