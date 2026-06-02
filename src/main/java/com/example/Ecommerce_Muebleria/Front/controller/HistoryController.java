package com.example.Ecommerce_Muebleria.Front.controller;


import com.example.Ecommerce_Muebleria.Front.services.OrderService;
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

        model.addAttribute("order", order);
        return "order-details";
    }
}