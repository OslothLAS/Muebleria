package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.BackProducts.services.StoreConfigService;
import com.example.Ecommerce_Muebleria.Front.services.ImageService;
import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.Front.services.internal.CollectionClientService;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.example.Ecommerce_Muebleria.entities.front.StoreConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.Ecommerce_Muebleria.entities.commons.Collection;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CollectionClientService collectionClientService;

    @Autowired
    private ImageService imageService;

    // 🚀 INYECTAMOS EL NUEVO SERVICIO DE CONFIGURACIÓN
    @Autowired
    private StoreConfigService storeConfigService;

    @GetMapping("/products")
    public String listProducts(Model model) {
        // 1. Cargamos Productos
        List<Product> products = productService.findAllProducts();

        // 2. Cargamos Colecciones (Vital para que la tabla de colecciones en tu HTML funcione)
        List<Collection> collections = collectionClientService.getActiveCollections();

        // 3. Cargamos Configuraciones de Visibilidad
        StoreConfig config = storeConfigService.getConfig();
        model.addAttribute("bannerActive", config.isBannerActive());
        model.addAttribute("carouselActive", config.isCarouselActive());
        model.addAttribute("collectionsActive", config.isCollectionsActive());

        // 4. Mandamos todo a la vista
        model.addAttribute("products", products);
        model.addAttribute("collections", collections);
        model.addAttribute("product", new Product());

        return "product-list"; // Asegurate de que tu HTML del dashboard se llame product-list.html
    }

    @GetMapping("/collections/products")
    public String mainDashboard(Model model) {
        System.out.println("DEBUG: Entrando al Dashboard de Admin");
        // Como centralizamos todo en /products, dejamos que este simplemente redirija
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
            @RequestParam(value = "imageFile", required = false) MultipartFile mainFile,
            @RequestParam(value = "extraImages", required = false) MultipartFile[] extraFiles
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
            System.err.println("Error al subir la imagen:");
            e.printStackTrace();
            return "redirect:/admin/products/new?error=upload";
        } catch (Exception e) {
            System.err.println("Error general al guardar el producto:");
            e.printStackTrace();
            return "redirect:/admin/products/new?error=backend";
        }
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
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
            productService.restoreProduct(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto restaurado con éxito.");
            redirectAttributes.addFlashAttribute("clase", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "No se pudo restaurar el producto.");
            redirectAttributes.addFlashAttribute("clase", "danger");
        }
        return "redirect:/admin/products";
    }

    // ========================================================
    // 🚀 NUEVOS ENDPOINTS: CONTROL DE VISIBILIDAD (BOTONES)
    // ========================================================

    @PostMapping("/visibility/carousel")
    public String toggleCarousel(RedirectAttributes redirectAttributes) {
        storeConfigService.toggleCarousel();
        redirectAttributes.addFlashAttribute("mensaje", "Visibilidad del carrousel actualizada.");
        redirectAttributes.addFlashAttribute("clase", "success");
        return "redirect:/admin/products";
    }

    @PostMapping("/visibility/collections")
    public String toggleCollections(RedirectAttributes redirectAttributes) {
        storeConfigService.toggleCollections();
        redirectAttributes.addFlashAttribute("mensaje", "Visibilidad de las colecciones actualizada.");
        redirectAttributes.addFlashAttribute("clase", "success");
        return "redirect:/admin/products";
    }

    @PostMapping("/banner/toggle")
    public String toggleBanner(RedirectAttributes redirectAttributes) {
        storeConfigService.toggleBanner();
        redirectAttributes.addFlashAttribute("mensaje", "Visibilidad del banner actualizada.");
        redirectAttributes.addFlashAttribute("clase", "success");
        return "redirect:/admin/products";
    }
}