package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.commons.Collection;
import com.example.Ecommerce_Muebleria.Front.services.CartService;
import com.example.Ecommerce_Muebleria.Front.services.internal.CollectionClientService;
import com.example.Ecommerce_Muebleria.Front.services.internal.WishlistService;
import com.example.Ecommerce_Muebleria.entities.commons.Product;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CollectionClientService collectionService;

    @Autowired
    private WishlistService wishlistService; // <--- 2. INYECCIÓN DEL SERVICIO

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "0") int page,
                        Model model,
                        HttpServletRequest request) {

        // 1. CARGA SOLO LO QUE ES ÚNICO DE ESTA PÁGINA
        List<Product> products = productService.findAllActiveProducts();
        List<Product> productsNew = productService.findIsNew();
        List<Product> destacados = productService.findFeaturedProducts();
        List<Collection> collections = collectionService.getActiveCollections();

        // 2. BORRÁ TODO LO RELACIONADO AL CARRITO Y FAVORITOS DE ACÁ
        // (Eso ya lo hace el GlobalDataController para todas las páginas)

        model.addAttribute("featuredProducts", destacados);
        model.addAttribute("productsNew", productsNew);
        model.addAttribute("products", products);
        model.addAttribute("activeCollections", collections);

        if (request.getHeader("HX-Request") != null) {
            return "index :: lista-productos";
        }

        return "index";
    }

    @GetMapping("/contact")
    public String showContacts(){
        return "contact";
    }

    @GetMapping("/about")
    public String showHowWeAre(){
        return "quienes-somos";
    }

    @GetMapping("/products")
    public String shopPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "new") String sort,
                           Model model,
                           HttpServletRequest request) {

        Map<String, Object> response;
        if (keyword != null && !keyword.isEmpty()) {
            response = productService.searchProductsPaginated(keyword, page, sort);
            model.addAttribute("keyword", keyword);
        } else {
            response = productService.findAllPaginated(page, sort);
        }

        // Lógica de enriquecimiento de favoritos... (solo si no tenés GlobalData para esto)

        model.addAttribute("allProducts", response.get("products"));
        model.addAttribute("currentPage", response.getOrDefault("currentPage", 0));
        model.addAttribute("totalPages", response.getOrDefault("totalPages", 0));
        model.addAttribute("activePage", "products");
        model.addAttribute("sort", sort);

        if (request.getHeader("HX-Request") != null) {
            return "shop :: items-scroll";
        }
        return "shop";
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id,
                                 @AuthenticationPrincipal OidcUser oidcUser,
                                 Model model) {

        Product product = productService.findProductById(id);

        if (product == null) {
            return "redirect:/";
        }

        // 1. Lógica de Favoritos (Local y Optimizada)
        boolean isFav = false;

        // Si el usuario está logueado, chequeamos en BD local
        if (oidcUser != null) {
            try {
                // 🚀 CAMBIO: Extraemos el ID del usuario de Auth0
                String auth0UserId = oidcUser.getSubject();

                // 🚀 OPTIMIZACIÓN: Usamos el método directo en vez de traer toda la lista
                isFav = wishlistService.isFavorite(auth0UserId, id);

            } catch (Exception e) {
                System.err.println("❌ Error al verificar favorito en detalles: " + e.getMessage());
            }
        }

        // 2. Pasar datos al Modelo
        model.addAttribute("isFavorite", isFav);
        model.addAttribute("product", product);

        // DEBUG (Opcional, pero útil para verificar)
        System.out.println("======= DEBUG PRODUCTO =======");
        System.out.println("Nombre: " + product.getName());
        System.out.println("Es Favorito: " + isFav);
        System.out.println("==============================");

        return "product-details";
    }


    @GetMapping("/collection-view/{id}")
    public String viewCollectionView(@PathVariable Long id,
                                     Model model,
                                     @AuthenticationPrincipal OidcUser oidcUser,
                                     HttpSession session) {

        // 1. Buscamos la colección (Backend 8080)
        Collection collection = collectionService.getCollectionById(id);
        if (collection == null) return "redirect:/";

        // 🚀 NUEVO: Filtramos los productos para quedarnos SOLO con los activos
        List<Product> activeProducts = collection.getProducts().stream()
                // Asegurate de que el método coincida con tu entidad (ej: isActivo() o getActivo())
                .filter(Product::getActivo)
                .toList();

        // 2. 🛒 LÓGICA DEL CARRITO
        String cartId = getCartId(oidcUser, session);
        Cart cart = cartService.getCart(cartId);

        // 3. Creamos el mapa de cantidades para las tarjetas
        Map<Long, Integer> productQuantities = new HashMap<>();
        if (cart != null && cart.getItems() != null) {
            for (var item : cart.getItems()) {
                productQuantities.put(item.getProductDetail().getId(), item.getQuantity());
            }
        }

        // 4. Inyectamos todo al modelo
        model.addAttribute("collection", collection);
        model.addAttribute("activeProducts", activeProducts); // 👈 Enviamos la lista filtrada
        model.addAttribute("productQuantities", productQuantities);
        model.addAttribute("cart", cart);

        // Opcional: Para el numerito rojo del carrito en el header
        int cartCount = (cart != null && cart.getItems() != null) ? cart.getItems().size() : 0;
        model.addAttribute("cartCount", cartCount);

        return "collection-view";
    }

    //Este metodo esta en varios controllers, hay que hacerlo mas eficiente
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

}