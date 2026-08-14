package com.example.Ecommerce_Muebleria.BackProfiles.controllers;

import com.example.Ecommerce_Muebleria.BackProfiles.entities.UserProfile;
import com.example.Ecommerce_Muebleria.BackProfiles.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/";
        }

        // 🚀 Obtenemos el ID único técnico (Suele venir en el atributo 'sub' o usando getName())
        String auth0Id = principal.getAttribute("sub");
        if (auth0Id == null) {
            auth0Id = principal.getName();
        }

        UserProfile userProfile = userProfileService.getProfileByAuth0Id(auth0Id);

        model.addAttribute("usuarioAuth0", principal.getAttributes());
        model.addAttribute("userProfile", userProfile);

        return "profile/profile";
    }

    @PostMapping("/profile/save")
    public String saveProfile(@ModelAttribute UserProfile userProfile,
                              @AuthenticationPrincipal OAuth2User principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/";
        }

        // Obtenemos el ID y el email de Auth0
        String auth0Id = principal.getAttribute("sub");
        if (auth0Id == null) {
            auth0Id = principal.getName();
        }

        String emailAuth0 = principal.getAttribute("email");

        // Buscamos si ya existía en la BD
        UserProfile existingProfile = userProfileService.getProfileByAuth0Id(auth0Id);
        if (existingProfile.getId() != null) {
            userProfile.setId(existingProfile.getId());
        }

        // Protegemos los datos inmutables desde Auth0
        userProfile.setAuth0Id(auth0Id);
        userProfile.setEmail(emailAuth0); // Opcional, pero te sirve para tenerlo registrado

        userProfileService.saveOrUpdateProfile(userProfile);

        redirectAttributes.addFlashAttribute("mensaje", "Tus datos se actualizaron correctamente.");
        redirectAttributes.addFlashAttribute("clase", "success");

        return "redirect:/profile";
    }
}