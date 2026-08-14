package com.example.Ecommerce_Muebleria.BackProfiles.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🚀 NEXO VITAL CORRECTO: ID único de Auth0
    @Column(unique = true, nullable = false)
    private String auth0Id;

    // (Opcional) Podemos guardar el email solo como dato de contacto
    private String email;

    // Datos Personales
    private String firstName;
    private String lastName;
    private String phone;
    private String documentNumber; // DNI o CUIT

    // Datos de Envío por defecto
    private String defaultShippingAddress;
    private String defaultZipCode;
    private String defaultCity;
    private String defaultBetweenStreets;
    private String defaultReferencesInfo;
}