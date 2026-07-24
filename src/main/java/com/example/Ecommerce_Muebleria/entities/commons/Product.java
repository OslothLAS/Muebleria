package com.example.Ecommerce_Muebleria.entities.commons;

import com.example.Ecommerce_Muebleria.entities.products.Question;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class) // Habilita auditoría
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;

    // --- NUEVOS CAMPOS TÉCNICOS (Para la tabla de especificaciones) ---

    private String model;              // Modelo
    private String materialStructure;  // Material Estructura
    private String materialUpholstery; // Material Tapizado
    private String height;             // Altura (String para permitir "95 cm")
    private String width;              // Ancho
    private String depth;              // Profundidad
    private String maxWeight;          // Peso máximo
    private String warranty;           // Garantía
    private Integer discountPercentage = 0;
    private String color;
    private String extraImage1;
    private String extraImage2;
    private String extraImage3;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "es_destacado")
    private Boolean esDestacado;

    @Column(name = "is_new_product")
    private boolean newProduct;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private Integer installments = 1; // Cantidad de cuotas sin interés

    // En Product.java (Backend 8080)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore // 🚀 Evita que Jackson entre en bucle infinito al serializar
    private List<Question> questions = new ArrayList<>();

    @Column(columnDefinition = "TEXT") // 👈 Importante para que MySQL use 'TEXT' y no 'VARCHAR(255)'
    private String sellerNotes;

    public Product(String name, String description, String imageUrl, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.newProduct = true;
        this.esDestacado = false;
        this.stock = 1;
        this.activo = true;
    }

    public Product() {}

    public boolean isRecentlyCreated() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
}
