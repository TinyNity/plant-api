package org.ili.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.ili.dto.AddMemberRequest;
import org.ili.dto.CreateHomeRequest;
import org.ili.dto.HomeResponse;
import org.ili.entity.Home;
import org.ili.entity.HomeMember;
import org.ili.entity.HomeMemberId;
import org.ili.entity.User;
import org.ili.repository.HomeMemberRepository;
import org.ili.repository.HomeRepository;
import org.ili.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class HomeService {

    @Inject
    HomeRepository homeRepository;

    @Inject
    HomeMemberRepository homeMemberRepository;

    @Inject
    UserRepository userRepository;

    // Simulation Auth: renvoie l'utilisateur ID 1
    private User getCurrentUser() {
        return userRepository.findByIdOptional(1L)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<HomeResponse> getMyHomes() {
        User currentUser = getCurrentUser();
        List<HomeMember> memberships = homeMemberRepository.findByUserId(currentUser.id);
        
        return memberships.stream()
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
        User currentUser = getCurrentUser();
        
        Home home = Home.builder()
                .name(request.getName())
                .build();
        
        homeRepository.persist(home);

        HomeMember membership = HomeMember.builder()
                .id(new HomeMemberId(home.id, currentUser.id))
                .home(home)
                .user(currentUser)
                .role(HomeMember.Role.OWNER)
                .build();
        
        homeMemberRepository.persist(membership);
        
        // Refresh home to get the updated list of members (which now contains the owner)
        // Or manually construct the response since we know it's just the owner
        return HomeResponse.builder()
                .id(home.id)
                .name(home.name)
                .memberUsernames(List.of(currentUser.username))
                .build();
    }

    @Transactional
    public void deleteHome(Long homeId) {
        Home home = homeRepository.findByIdOptional(homeId)
                .orElseThrow(() -> new NotFoundException("Home not found"));
        
        // TODO: Vérifier si l'utilisateur courant est OWNER avant de supprimer
        // Pour l'instant on suppose que c'est autorisé
        
        homeRepository.delete(home);
    }

    @Transactional
    public void addMember(Long homeId, AddMemberRequest request) {
        Home home = homeRepository.findByIdOptional(homeId)
                .orElseThrow(() -> new NotFoundException("Home not found"));
        
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
                .role(HomeMember.Role.GUEST)
                .build();
        
        homeMemberRepository.persist(membership);
    }

    @Transactional
    public void removeMember(Long homeId, Long userId) {
        HomeMemberId id = new HomeMemberId(homeId, userId);
        HomeMember membership = homeMemberRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Membership not found"));
        
        homeMemberRepository.delete(membership);
    }
}
