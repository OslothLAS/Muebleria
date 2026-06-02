package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.Front.services.ImageService;
import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.Front.services.internal.CollectionClientService;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.Ecommerce_Muebleria.entities.commons.Collection;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    // 🚀 CHAU RestClient y CHAU @Value productServiceUrl.
    // Todo ocurre en la memoria del monolito.

    @Autowired
    private ProductService productService;

    @Autowired
    private CollectionClientService collectionClientService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.findAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("product", new Product());
        return "product-list";
    }

    @GetMapping("/collections/products")
    public String mainDashboard(Model model) {
        System.out.println("DEBUG: Entrando al Dashboard de Admin");

        List<Product> products = productService.findAllProducts();
        System.out.println("DEBUG: Productos cargados -> " + (products != null ? products.size() : "NULL"));

        // Llamada nativa a la BD local
        List<Collection> collections = collectionClientService.getActiveCollections();

        model.addAttribute("products", products);
        model.addAttribute("collections", collections);
        model.addAttribute("product", new Product());

        return "redirect:/admin/products";
    }

    @GetMapping("/products/new")
    public String createProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile mainFile,
            @RequestParam("extraImages") MultipartFile[] extraFiles
    ) {
        try {
            if (mainFile != null && !mainFile.isEmpty()) {
                String mainUrl = imageService.uploadImg(mainFile);
                product.setImageUrl(mainUrl);
            }

            if (extraFiles != null) {
                for (int i = 0; i < extraFiles.length; i++) {
                    MultipartFile file = extraFiles[i];
                    if (file != null && !file.isEmpty()) {
                        String url = imageService.uploadImg(file);
                        if (i == 0) product.setExtraImage1(url);
                        if (i == 1) product.setExtraImage2(url);
                        if (i == 2) product.setExtraImage3(url);
                    }
                }
            }

            if (product.getId() != null && product.getId() > 0) {
                productService.updateProductInBackend(product);
                return "redirect:/admin/products?editSuccess";
            } else {
                productService.sendProductToBackend(product);
                return "redirect:/admin/products?success";
            }

        } catch (IOException e) {
            return "redirect:/admin/products/new?error=upload";
        } catch (Exception e) {
            return "redirect:/admin/products/new?error=backend";
        }
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 🚀 Ejecución directa de código Java, sin protocolos de red
            productService.deactivateProduct(id);

            redirectAttributes.addFlashAttribute("mensaje", "Producto desactivado correctamente.");
            redirectAttributes.addFlashAttribute("clase", "warning");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al desactivar el producto.");
            redirectAttributes.addFlashAttribute("clase", "danger");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.findProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("titulo", "Editar Producto: " + product.getName());
        return "product-form";
    }

    @PostMapping("/products/restore/{id}")
    public String restoreProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 🚀 Ejecución directa de código Java
            productService.restoreProduct(id);

            redirectAttributes.addFlashAttribute("mensaje", "Producto restaurado con éxito.");
            redirectAttributes.addFlashAttribute("clase", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "No se pudo restaurar el producto.");
            redirectAttributes.addFlashAttribute("clase", "danger");
        }
        return "redirect:/admin/products";
    }
}