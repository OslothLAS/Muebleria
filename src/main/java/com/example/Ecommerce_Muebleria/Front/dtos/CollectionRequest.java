package com.example.Ecommerce_Muebleria.Front.dtos;

import lombok.Data;
import java.util.List;

@Data // Si usás Lombok, sino generá Getters y Setters
public class CollectionRequest {

    private String name;
    private String description;

    // Lista de IDs de los productos seleccionados en los checkboxes
    private List<Long> productIds;

    /*
       Nota: El MultipartFile suele pasarse como un @RequestParam
       aparte en el Controller para evitar líos de configuración
       de binders, pero podés incluirlo acá si lo preferís.
    */
}