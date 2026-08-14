package com.example.Ecommerce_Muebleria.BackProfiles.repositories;

import com.example.Ecommerce_Muebleria.BackProfiles.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // Busca el perfil basándose en el email de Auth0
    Optional<UserProfile> findByEmail(String email);
    Optional<UserProfile> findByAuth0Id(String auth0Id);
}