package org.ili.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.ili.dto.AddMemberRequest;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.CreateRoomRequest;
import org.ili.dto.HomeMemberResponse;
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
import io.quarkus.security.ForbiddenException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class HomeService {

	@Inject
	HomeRepository homeRepository;

	@Inject
	RoomRepository roomRepository;

	@Inject
	HomeMemberRepository homeMemberRepository;

	@Inject
	UserRepository userRepository;

	@Inject
	AuthService authService;

	public HomeResponse getById(UUID id) {
		Home ret = homeRepository.findByIdOptional(id).orElseThrow(() -> new NotFoundException(String.format(id.toString())));
        List<Room> rooms = roomRepository.findByHomeId(ret.id);

		return HomeResponse.builder()
			.memberUsernames(ret.members.stream().map(x -> x.user.username).collect(Collectors.toList()))
			.name(ret.name)
			.id(ret.id)
			.rooms(rooms)
			.build();
	}

	public List<HomeResponse> getMyHomes() {
		User currentUser = authService.getCurrentUser();
		List<HomeMember> homeMembers;
		try {
			homeMembers = homeMemberRepository.findByUserId(currentUser.id);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error occurred while fetching home homeMembers", e);
		}

		return homeMembers.stream()
				.map(member -> {
					Home home = member.home;
					List<String> memberNames = home.members.stream()
							.map(m -> m.user.username)
							.collect(Collectors.toList());
					return HomeResponse.builder()
							.id(home.id)
							.name(home.name)
							.memberUsernames(memberNames)
							.build();
				})
				.collect(Collectors.toList());
	}

	@Transactional
	public HomeResponse createHome(CreateHomeRequest request) {

		User currentUser = authService.getCurrentUser();

		Home home = Home.builder()
				.name(request.getName())
				.build();

		homeRepository.persist(home);

		HomeMember membership = HomeMember.builder()
				.id(new HomeMemberId(home.id, currentUser.getId()))
				.home(home)
				.user(currentUser)
				.role(Role.ADMIN)
				.build();

		homeMemberRepository.persist(membership);

		// Refresh home to get the updated list of members (which now contains the
		// owner)
		// Or manually construct the response since we know it's just the owner
		return HomeResponse.builder()
				.id(home.id)
				.name(home.name)
				.memberUsernames(List.of(currentUser.username))
				.build();
	}

	@Transactional
	public void deleteHome(UUID homeId) {
		Home home = homeRepository.findByIdOptional(homeId)
				.orElseThrow(() -> new NotFoundException("Home not found"));

		Role currentUserPermission = authService.getUserPermission(home);
		if (currentUserPermission == Role.ADMIN) {
			homeRepository.delete(home);
			return;
		}
		throw new ForbiddenException("Current user does not have enough permission");
	}

	@Transactional
	public void addMember(UUID homeId, AddMemberRequest request) {
		Home home = homeRepository.findByIdOptional(homeId)
				.orElseThrow(() -> new NotFoundException("Home not found"));

		Role currentUserPermission = authService.getUserPermission(home);

		if (currentUserPermission != Role.ADMIN) {
			throw new ForbiddenException("Current user does not have enough permission");
		}

		User userToAdd = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new NotFoundException("User with email " + request.getEmail() + " not found"));

		// Check if already member
		if (home.members.stream().anyMatch(m -> m.user.id.equals(userToAdd.id))) {
			throw new IllegalArgumentException("User is already a member of this home");
		}

		HomeMember membership = HomeMember.builder()
				.id(new HomeMemberId(home.id, userToAdd.id))
				.home(home)
				.user(userToAdd)
				.role(request.getRole())
				.build();

		homeMemberRepository.persist(membership);
	}

	@Transactional
	public void removeMember(UUID homeId, UUID userId) {
		HomeMemberId id = new HomeMemberId(homeId, userId);
		HomeMember membership = homeMemberRepository.findByIdOptional(id)
				.orElseThrow(() -> new NotFoundException("Membership not found"));

		Role userPermission = authService.getUserPermission(homeId, userId);
		Role currentUserPermission = authService.getUserPermission(homeId, authService.getCurrentUser().id);

		if (currentUserPermission.compareTo(userPermission) <= 0) {
			throw new ForbiddenException("Current user does not have enough permission");
		}

		homeMemberRepository.delete(membership);
	}

	public List<HomeMemberResponse> getMembersByHomeId(UUID homeId) {
		homeRepository.findByIdOptional(homeId)
				.orElseThrow(() -> new NotFoundException("Home not found"));

		return homeMemberRepository.findByHomeId(homeId).stream()
				.map(member -> HomeMemberResponse.builder()
						.username(member.user.username)
						.role(member.role)
						.userId(member.user.id)
						.build())
				.collect(Collectors.toList());
	}

	public List<RoomResponse> getRoomsByHomeId(UUID homeId) {
		return roomRepository.findByHomeId(homeId).stream()
				.map(room -> RoomResponse.builder()
						.id(room.id)
						.name(room.name)
						.homeId(room.home.id)
						.build())
				.collect(Collectors.toList());
	}

	@Transactional
	public RoomResponse createRoom(UUID homeId, CreateRoomRequest request) {
		Home home = homeRepository.findByIdOptional(homeId)
				.orElseThrow(() -> new NotFoundException("Home not found"));
		Role currentUserPermission = authService.getUserPermission(home);
		if (currentUserPermission == Role.MEMBER) {
			throw new ForbiddenException("Current user does not have enough permission");
		}

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
	public void deleteRoom(UUID roomId) {
		Room room = roomRepository.findByIdOptional(roomId)
				.orElseThrow(() -> new NotFoundException("Room not found"));

		Role currentUserPermission = authService.getUserPermission(room);
		if (currentUserPermission == Role.MEMBER) {
			throw new ForbiddenException("Current user does not have enough permission");
		}
		// La suppression en cascade des plantes dépend de la config JPA.
		// Si CascadeType.ALL n'est pas mis sur la relation OneToMany dans Room (ce qui
		// n'est pas le cas ici car la relation est dans Plant),
		// il faut supprimer les plantes manuellement ou compter sur la FK constraint ON
		// DELETE CASCADE de la DB.
		// Ici, on va laisser Hibernate gérer si possible, sinon il faudra supprimer les
		// plantes avant.
		// Pour l'instant, on tente la suppression directe.
		roomRepository.delete(room);
	}

}
