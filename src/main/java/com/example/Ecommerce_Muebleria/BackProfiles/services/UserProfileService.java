package com.example.Ecommerce_Muebleria.BackProfiles.services;

import com.example.Ecommerce_Muebleria.BackProfiles.entities.UserProfile;
import com.example.Ecommerce_Muebleria.BackProfiles.repositories.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository repository;

    public UserProfile getProfileByAuth0Id(String auth0Id) {
        // Buscamos el perfil por su ID técnico. Si no está, lo instanciamos.
        return repository.findByAuth0Id(auth0Id).orElseGet(() -> {
            UserProfile newProfile = new UserProfile();
            newProfile.setAuth0Id(auth0Id);
            return newProfile;
        });
    }


    public void saveOrUpdateProfile(UserProfile profile) {
        repository.save(profile);
    }
}