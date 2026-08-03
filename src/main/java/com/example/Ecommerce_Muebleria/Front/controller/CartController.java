package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.Front.services.CartService;
import com.example.Ecommerce_Muebleria.Front.services.OrderService;
import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.Front.services.internal.WishlistService;
import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.ClientAttributes.clientRegistrationId;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final WishlistService wishlistService;
    private final ProductService productService;

    @Autowired
    private WebClient cartWebClient;

    private String getUserId(OidcUser oidcUser, HttpSession session) {
        if (oidcUser != null) {
            return oidcUser.getSubject(); // Usuario real de Auth0
        }
        // Si no hay login, usamos el ID de la sesión del navegador
        return "GUEST_" + session.getId();
    }

    // --- 1. VER CARRITO (Página Completa) ---
    @GetMapping
    public String viewCart(Model model,
                           @AuthenticationPrincipal OidcUser oidcUser,
                           HttpSession session) {

        // Obtenemos el ID (ya sea el de Auth0 o el de Invitado)
        String cartId = getCartId(oidcUser, session);

        // 🚀 IMPORTANTE: Ahora le pasamos el cartId al service
        // (Luego modificaremos el service para que lo reciba)
        Cart cart = cartService.getCart(cartId);

        // Usamos el método de cálculo que ya tenés en el service para mayor limpieza
        BigDecimal total = cartService.calculateTotal(cart);

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);

        return "cart";
    }

    // --- 2. AGREGAR Y REDIRIGIR ---
    @GetMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") Integer qty,
                            @AuthenticationPrincipal OidcUser oidcUser,
                            HttpSession session) {

        String cartId = getCartId(oidcUser, session);

        cartService.addToCart(productId, qty, cartId);
        return "redirect:/cart";
    }

    // --- 3. ACTUALIZAR CANTIDAD (AJAX) ---
    @PostMapping("/api/update-quantity")
    public String updateQuantityAjax(@RequestParam Long productId,
                                     @RequestParam int delta,
                                     @AuthenticationPrincipal OidcUser oidcUser,
                                     HttpSession session,
                                     Model model) {
        try {
            // 🚀 Eliminamos el "if (oidcUser == null)" para que el invitado pueda editar
            String cartId = getCartId(oidcUser, session);

            // 1. Actualizamos en el micro 8082 usando el ID híbrido
            // (Asegurate de que tu service.updateQuantity ahora reciba el cartId)
            cartService.updateQuantity(productId, delta, cartId);

            // 2. Obtenemos el carrito actualizado para refrescar la vista
            Cart cart = cartService.getCart(cartId);
            Map<Long, Integer> productQuantities = new HashMap<>();
            if (cart != null && cart.getItems() != null) {
                cart.getItems().forEach(item -> productQuantities.put(item.getProductId(), item.getQuantity()));
            }

            Product product = productService.findProductById(productId);

            model.addAttribute("product", product);
            model.addAttribute("productQuantities", productQuantities);
            model.addAttribute("safeId", productId);

            return "shop :: cartActionsFragment";

        } catch (Exception e) {
            System.err.println("❌ Error actualizando cantidad: " + e.getMessage());
            return null;
        }
    }

    // --- 4. REMOVER AJAX (HTMX / Card Producto) ---
    @GetMapping("/remove-ajax/{productId}")
    public String removeCartAjax(@PathVariable Long productId,
                                 Model model,
                                 @AuthenticationPrincipal OidcUser oidcUser,
                                 HttpSession session) {

        // 🚀 Eliminamos el bloqueo. El invitado también puede arrepentirse de una compra.
        String cartId = getCartId(oidcUser, session);

        // 1. Borramos el producto del 8082
        cartService.removeProduct(productId, cartId);

        // 2. Lógica de favoritos (Esto sí depende de si está logueado)
        List<Long> favoritesIds = (oidcUser != null) ? wishlistService.getFavoriteIds() : new ArrayList<>();

        // 3. Preparar el mapa del producto para el fragmento
        Product productEntity = productService.findProductById(productId);
        Map<String, Object> productMap = new HashMap<>();

        if (productEntity != null) {
            productMap.put("id", productEntity.getId());
            productMap.put("name", productEntity.getName());
            productMap.put("price", productEntity.getPrice());
            productMap.put("imageUrl", productEntity.getImageUrl());
            productMap.put("favorite", favoritesIds.contains(productId));
        }

        model.addAttribute("product", productMap);
        model.addAttribute("favoritesIds", favoritesIds);
        model.addAttribute("productQuantities", new HashMap<>());

        return "shop :: cardProducto";
    }

    @GetMapping("/panel-fragment")
    public String getCartPanelFragment(Model model, @AuthenticationPrincipal OidcUser oidcUser, HttpSession session) {
        String cartId = getCartId(oidcUser, session); // 🚀 Usamos el ID híbrido
        Cart myCart = cartService.getCart(cartId);    // 🚀 Buscamos SIEMPRE

        model.addAttribute("myCart", myCart != null ? myCart : new Cart());
        return "fragments :: sideCartPanel";
    }


    @GetMapping("/add-ajax/{productId}")
    public String addCartAjax(@PathVariable Long productId,
                              Model model,
                              @AuthenticationPrincipal OidcUser oidcUser,
                              HttpSession session) {

        // 🚀 ELIMINAMOS EL REDIRECT AL LOGIN.
        String cartId = getCartId(oidcUser, session);

        try {
            // 1. Agregamos el mueble al 8082 usando el ID que corresponda
            cartService.addToCart(productId, 1, cartId);

            // 2. Recuperamos el carrito actualizado de ese mismo ID
            Cart cart = cartService.getCart(cartId);

            // 3. Preparamos los datos para que el botón cambie a +/-
            Map<Long, Integer> productQuantities = new HashMap<>();
            if (cart != null && cart.getItems() != null) {
                cart.getItems().forEach(item ->
                        productQuantities.put(item.getProductId(), item.getQuantity())
                );
            }

            model.addAttribute("product", productService.findProductById(productId));
            model.addAttribute("productQuantities", productQuantities);
            model.addAttribute("cartCount", (cart != null) ? cart.getItems().size() : 0);

        } catch (Exception e) {
            System.err.println("❌ Error en add-ajax: " + e.getMessage());
        }

        // Devolvemos solo el pedacito de HTML de los botones
        return "shop :: cartActionsFragment";
    }
    // Esto hace que el cartCount esté disponible en TODAS las páginas
    @ModelAttribute
    public void addCartAttributes(Model model,
                                  @AuthenticationPrincipal OidcUser oidcUser,
                                  HttpSession session) {

        String cartId = getCartId(oidcUser, session);

        try {
            // 1. Buscamos el carrito (ya hidratado con el 8082 y 8080)
            Cart cart = cartService.getCart(cartId);

            // 2. Creamos el mapa de cantidades: Map<ID_Producto, Cantidad>
            Map<Long, Integer> productQuantities = new HashMap<>();
            if (cart != null && cart.getItems() != null) {
                cart.getItems().forEach(item ->
                        productQuantities.put(item.getProductId(), item.getQuantity())
                );
            }

            // 🚀 LA CLAVE: Inyectamos esto para que las tarjetas lo vean al cargar
            model.addAttribute("productQuantities", productQuantities);
            model.addAttribute("cartCount", productQuantities.size());
            model.addAttribute("myCart", cart);

        } catch (Exception e) {
            model.addAttribute("productQuantities", new HashMap<Long, Integer>());
            model.addAttribute("cartCount", 0);
            model.addAttribute("myCart", new Cart());
        }
    }


    @PostMapping("/checkout")
    public String startCheckout(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) return "redirect:/oauth2/authorization/auth0";

        // Pedimos el link al Micro 8082
        String mpUrl = cartWebClient.post()
                .uri("/api/cart/checkout")
                .attributes(clientRegistrationId("auth0"))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Redirección externa a Mercado Pago
        return "redirect:" + mpUrl;
    }


    @GetMapping("/success")
    public String paymentSuccess(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RequestParam(value = "payment_id", required = false) String paymentId,
            HttpSession session, // 🚀 Para identificar al invitado
            Model model) {

        // Ya no redirigimos al login. El invitado tiene derecho a su éxito.
        String cartId = getCartId(oidcUser, session);

        try {
            // Le pasamos el ID al service para que sepa qué carrito vaciar en la DB
            orderService.confirmPurchase(paymentId, cartId);
            System.out.println("✅ Orden procesada para: " + cartId);
        } catch (Exception e) {
            return "redirect:/cart?error=true";
        }

        model.addAttribute("paymentId", paymentId);
        return "purchase-success";
    }


    @GetMapping("/failure")
    public String paymentFailure() {
        System.out.println("❌ El usuario canceló el pago o la tarjeta fue rechazada.");
        // NOTA: Aquí NO llamamos a cartService.clearCart()
        return "purchase-failure"; // Nombre del HTML
    }

    //8081
    @GetMapping("/checkout")
    public String showCheckout(Model model, @AuthenticationPrincipal OidcUser oidcUser, HttpSession session) {
        String cartId = getCartId(oidcUser, session);
        Cart cart = cartService.getCart(cartId);

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Usamos el método de tu servicio para el total
        BigDecimal total = cartService.calculateTotal(cart);

        model.addAttribute("myCart", cart);
        model.addAttribute("totalAmount", total);

        return "checkout"; // El HTML que te pasé antes
    }

    @PostMapping("/process-checkout")
    public String processCheckout(@RequestParam String address,
                                  @RequestParam String zipCode,
                                  @RequestParam String city,
                                  @AuthenticationPrincipal OidcUser oidcUser, // 🚀 Agregamos esto
                                  HttpSession session) {                      // 🚀 Y esto

        // Obtenemos el ID híbrido
        String cartId = getCartId(oidcUser, session);

        // 🚀 IMPORTANTE: Pasamos el cartId al service
        String mpUrl = orderService.getPaymentLink(address, zipCode, city, cartId);

        return "redirect:" + mpUrl;
    }


    private String getCartId(OidcUser oidcUser, HttpSession session) {
        if (oidcUser != null) {
            return oidcUser.getSubject();
        }

        // 🚀 TRUCO: Verificamos si es una sesión nueva y forzamos un atributo
        // Esto obliga al servidor a enviar el encabezado "Set-Cookie"
        if (session.isNew() || session.getAttribute("GUEST_INIT") == null) {
            session.setAttribute("GUEST_INIT", true);
        }

        return "GUEST_" + session.getId();
    }

    // --- COMPRA RÁPIDA (Desde el detalle del producto) ---
    @PostMapping("/quick-buy/{productId}")
    public String quickBuy(@PathVariable Long productId,
                           @RequestParam(name = "qty", defaultValue = "1") Integer qty,
                           @AuthenticationPrincipal OidcUser oidcUser,
                           HttpSession session) {

        // 1. Obtenemos el ID del usuario o invitado
        String cartId = getCartId(oidcUser, session);

        // 2. Agregamos la cantidad seleccionada al carrito
        cartService.addToCart(productId, qty, cartId);

        // 3. Redirigimos directo al proceso de pago
        return "redirect:/cart/checkout";
    }
}