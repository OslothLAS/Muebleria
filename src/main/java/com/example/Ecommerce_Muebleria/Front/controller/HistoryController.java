package com.example.Ecommerce_Muebleria.Front.controller;


import com.example.Ecommerce_Muebleria.Front.services.OrderService;
import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.entities.cart.OrderItem;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.Ecommerce_Muebleria.entities.cart.Order;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HistoryController {

    private final OrderService orderService;
    private final ProductService productService;

    @GetMapping("/my-orders")
    public String showMyOrders(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) return "redirect:/login";

        // ✅ Ya no necesitamos el token acá, el Service lo maneja solo
        List<Order> userOrders = orderService.getOrders(oidcUser.getSubject());

        model.addAttribute("orders", userOrders);
        return "my-orders";
    }
    @GetMapping("/order-details/{id}")
    public String orderDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) return "redirect:/login";

        // ✅ Limpio y Senior
        Order order = orderService.getOrder(id);

        if (order == null) return "redirect:/my-orders";

        // 🚀 EL PASO CLAVE: Hidratamos los nombres de los productos consultando al microservicio
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                try {
                    // Buscamos el producto en el puerto 8080 usando tu servicio
                    Product product = productService.findProductById(item.getProductId());

                    // Si el producto existe, le seteamos el nombre. Si no, queda en null.
                    if (product != null) {
                        item.setProductName(product.getName());
                    }
                } catch (Exception e) {
                    // Si el micro 8080 está caído o el producto fue borrado, atrapamos el error
                    // para que la página de la orden no se rompa entera.
                    System.err.println("⚠️ Error obteniendo producto " + item.getProductId() + ": " + e.getMessage());
                }
            }
        }

        model.addAttribute("order", order);
        return "order-details";
    }
}