package com.example.Ecommerce_Muebleria.Front.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRolesPermissionsDTO {
    private String username;
    private String rol;
}