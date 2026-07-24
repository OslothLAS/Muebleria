package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.example.Ecommerce_Muebleria.entities.products.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Controller
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductForBackend(@PathVariable Long id) {
        Product product = productService.findProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        // Spring Boot convertirá automáticamente este objeto Java a JSON
        return ResponseEntity.ok(product);
    }

    // Agrega esto en tu HomeController o ProductController

    @GetMapping("/toggle-favorite/{productId}")
    public String toggleFavorite(@PathVariable Long productId,
                                 Model model,
                                 @CookieValue(name = "USER_ID_INFO", required = false) String userId) {

        if (userId == null) {
            // Opción: Si no está logueado, podrías devolver un error o redirigir
            return null;
        }

        boolean isNowFavorite = true;
        // --------------------------------------------------

        // 2. Pasamos el dato al modelo
        model.addAttribute("isFavorite", isNowFavorite);
        model.addAttribute("productId", productId); // Necesario para reconstruir el ID del botón

        // 3. Devolvemos SOLO el fragmento del botón de corazón
        return "index :: btnFavorito";
    }

    @PostMapping("/question/save")
    public String saveQuestion(@RequestParam Long productId,
                               @RequestParam String text,
                               @AuthenticationPrincipal OidcUser principal,
                               RedirectAttributes ra) {

        if (principal == null) {
            return "redirect:/oauth2/authorization/auth0";
        }

        try {
            // 1. Instanciamos la pregunta
            Question question = new Question();

            // 2. ASIGNAMOS TODOS LOS DATOS (¡Esto era lo que faltaba!)
            question.setText(text);

            // Extraemos los datos del usuario de Auth0 (ajusta los getters si usas otros en tu OidcUser)
            question.setUserId(principal.getSubject());
            question.setUserName(principal.getFullName() != null ? principal.getFullName() : principal.getPreferredUsername());

            // 3. Asignamos el producto
            Product product = new Product();
            product.setId(productId);
            question.setProduct(product);

            // 4. Ahora sí, el objeto está lleno y listo para viajar al backend
            productService.saveQuestion(question);

            ra.addFlashAttribute("mensaje", "¡Pregunta enviada!");

        } catch (Exception e) {
            log.error("Error al guardar: ", e);
            ra.addFlashAttribute("error", "Error al enviar la pregunta.");
        }

        return "redirect:/product/" + productId + "#questions-section";
    }


    // En ProductApiController.java del Front
    @PostMapping("/question/{questionId}/answer")
    public String answerQuestion(@PathVariable Long questionId,
                                 @RequestParam Long productId,
                                 @RequestParam String answerText,
                                 RedirectAttributes ra) {
        try {
            // Llamamos al servicio del front que le pega al back 8080
            productService.saveAnswer(questionId, answerText);
            ra.addFlashAttribute("mensaje", "Respuesta enviada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al responder");
        }
        return "redirect:/product/" + productId + "#questions-section"; //Esto va a la seccion de preguntas
    }

    @PostMapping("/question/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, @RequestParam Long productId) {
        productService.deleteQuestion(id);
        return "redirect:/product/" + productId + "#questions-section";
    }

    @PostMapping("/{id}/update-notes")
    public String updateNotes(@PathVariable Long id, @RequestParam String sellerNotes, RedirectAttributes ra) {
        try {
            productService.updateProductNotes(id, sellerNotes);
            ra.addFlashAttribute("mensaje", "¡Notas actualizadas!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudieron guardar las notas.");
        }
        // Redirigimos al detalle del producto
        return "redirect:/product/" + id;
    }
    @PostMapping("/{id}/update-specs")
    public String updateProductSpecs(@PathVariable Long id, @ModelAttribute Product productFromForm, RedirectAttributes ra) {
        try {
            // 1. Buscamos el producto real (que tiene nombre, precio, imagen, etc.)
            Product productExistente = productService.findProductById(id);

            if (productExistente != null) {
                // 2. Le actualizamos SOLO los campos de la ficha técnica
                productExistente.setModel(productFromForm.getModel());
                productExistente.setMaterialStructure(productFromForm.getMaterialStructure());
                productExistente.setMaterialUpholstery(productFromForm.getMaterialUpholstery());
                productExistente.setHeight(productFromForm.getHeight());
                productExistente.setWidth(productFromForm.getWidth());
                productExistente.setDepth(productFromForm.getDepth());
                productExistente.setMaxWeight(productFromForm.getMaxWeight());
                productExistente.setWarranty(productFromForm.getWarranty());

                // 3. Ahora mandamos el objeto COMPLETO y cargado
                productService.updateProductSpecs(productExistente);
                ra.addFlashAttribute("mensaje", "Ficha técnica actualizada.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/product/" + id;
    }

}