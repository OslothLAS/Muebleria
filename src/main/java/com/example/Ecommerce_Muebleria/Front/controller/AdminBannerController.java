package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.BackProducts.services.GlobalConfigService;
import com.example.Ecommerce_Muebleria.Front.services.ImageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/banner")
public class AdminBannerController {

    @Autowired
    private GlobalConfigService configService;

    // 🚀 Inyectamos tu servicio de Cloudinary
    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public String uploadBanner(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                // 1. Subimos el archivo a Cloudinary usando tu método
                String imageUrl = imageService.uploadImg(file);

                // 2. Si la subida fue exitosa, guardamos la URL devuelta en la BD
                if (imageUrl != null) {
                    configService.updateBannerUrl(imageUrl);
                    redirectAttributes.addFlashAttribute("mensaje", "Banner actualizado exitosamente.");
                    redirectAttributes.addFlashAttribute("clase", "success");
                } else {
                    redirectAttributes.addFlashAttribute("mensaje", "El archivo estaba vacío o no se pudo procesar.");
                    redirectAttributes.addFlashAttribute("clase", "danger");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al subir el banner: " + e.getMessage());
            redirectAttributes.addFlashAttribute("clase", "danger");
        }

        // Redirige de vuelta al dashboard de admin
        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteBanner(RedirectAttributes redirectAttributes) {
        configService.removeBannerUrl();
        redirectAttributes.addFlashAttribute("mensaje", "Banner eliminado correctamente.");
        redirectAttributes.addFlashAttribute("clase", "warning");
        return "redirect:/admin";
    }
}