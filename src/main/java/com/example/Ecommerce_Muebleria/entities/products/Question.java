package com.example.Ecommerce_Muebleria.entities.products;

import com.example.Ecommerce_Muebleria.entities.commons.Product;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String answer;

    // 🚀 Estos nombres deben ser IGUALES a los del QuestionDTO
    private String userId;
    private String userName;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}