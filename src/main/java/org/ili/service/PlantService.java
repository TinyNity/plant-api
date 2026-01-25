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
    public RoomResponse createRoom(Long homeId, CreateRoomRequest request) {
        Home home = homeRepository.findByIdOptional(homeId)
                .orElseThrow(() -> new NotFoundException("Home not found"));

        Room room = Room.builder()
                .name(request.getName())
                .home(home)
                .build();

        roomRepository.persist(room);

        return RoomResponse.builder()
                .id(room.id)
                .name(room.name)
                .homeId(home.id)
                .build();
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findByIdOptional(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        
        // La suppression en cascade des plantes dépend de la config JPA.
        // Si CascadeType.ALL n'est pas mis sur la relation OneToMany dans Room (ce qui n'est pas le cas ici car la relation est dans Plant),
        // il faut supprimer les plantes manuellement ou compter sur la FK constraint ON DELETE CASCADE de la DB.
        // Ici, on va laisser Hibernate gérer si possible, sinon il faudra supprimer les plantes avant.
        // Pour l'instant, on tente la suppression directe.
        
        roomRepository.delete(room);
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
