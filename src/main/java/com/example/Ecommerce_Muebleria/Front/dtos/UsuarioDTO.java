package com.example.Ecommerce_Muebleria.Front.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class UsuarioDTO {
    private String nombre;
    private String apellido;

    // @DateTimeFormat sirve para recibir el dato del HTML (Formulario)
    @DateTimeFormat(pattern = "yyyy-MM-dd")

    // @JsonFormat sirve para ENVIAR el dato al Backend (JSON)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // <--- AGREGA ESTO
    private LocalDate fechaNacimiento;

    private String username;
    private String contrasenia;
    private String rol;
}