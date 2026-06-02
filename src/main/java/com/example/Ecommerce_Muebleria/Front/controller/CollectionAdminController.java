package com.example.Ecommerce_Muebleria.Front.controller;


import com.example.Ecommerce_Muebleria.BackProducts.repositories.CollectionRepository;
import com.example.Ecommerce_Muebleria.Front.dtos.CollectionRequest;
import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.Front.services.internal.CollectionClientService;
import com.example.Ecommerce_Muebleria.entities.commons.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/collections")
public class CollectionAdminController {

    @Autowired
    private CollectionClientService collectionClientService;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/new")
    public String showForm(Model model) {
        // Pedimos los productos al backend para que el admin pueda elegir
        model.addAttribute("allProducts", productService.findAllActiveProducts());
        model.addAttribute("collection", new CollectionRequest()); // Un DTO simple para el form
        return "admin/collection-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam("name") String name,
                       @RequestParam("description") String description,
                       @RequestParam(value = "productIds", required = false) List<Long> productIds,
                       @RequestParam("imageFile") MultipartFile imageFile) {

        collectionClientService.saveCollection(name, description, productIds, imageFile);

        return "redirect:/admin/collections/products";
    }

    @PostMapping("/delete/{id}") // 🚀 Debe coincidir con el th:action del HTML
    public String deleteCollection(@PathVariable Long id) {
        System.out.println("LOG 8081: Recibida orden de borrar ID: " + id);

        // Llamada al servicio que conecta con el 8080
        collectionClientService.deleteCollection(id);

        System.out.println("LOG 8081: Borrado solicitado al backend con éxito.");
        return "redirect:/admin/products";
    }

    // --- MOSTRAR FORMULARIO DE EDICIÓN ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Buscamos la colección actual para precargar el form
        var collection = collectionClientService.getCollectionById(id);

        model.addAttribute("collection", collection);
        model.addAttribute("allProducts", productService.findAllProducts());
        model.addAttribute("isEdit", true); // Para reutilizar el form de "new"

        return "admin/collection-form";
    }

    // --- PROCESAR LA ACTUALIZACIÓN ---
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam("name") String name,
                         @RequestParam("description") String description,
                         @RequestParam(value = "productIds", required = false) List<Long> productIds,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        collectionClientService.updateCollection(id, name, description, productIds, imageFile);
        return "redirect:/admin/collections/products?updateSuccess";
    }

    public Collection getCollectionById(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada con ID: " + id));
    }

}