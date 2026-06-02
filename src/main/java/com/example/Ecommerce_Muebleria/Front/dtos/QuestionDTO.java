package com.example.Ecommerce_Muebleria.Front.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private Long productId;
    private String text;
    private String answer;
    private String userId;   // El "sub" de Auth0 (ej: auth0|65f...)
    private String userName; // El nickname o nombre para mostrar (ej: osmi)
    private LocalDateTime createdAt;
}