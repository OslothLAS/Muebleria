package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.BackProducts.repositories.ProductRepository;
import com.example.Ecommerce_Muebleria.BackProducts.services.GlobalConfigService;
import com.example.Ecommerce_Muebleria.BackProducts.services.StoreConfigService;
import com.example.Ecommerce_Muebleria.Front.services.OrderService;
import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.config.GlobalConfig;
import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.commons.Collection;
import com.example.Ecommerce_Muebleria.Front.services.CartService;
import com.example.Ecommerce_Muebleria.Front.services.internal.CollectionClientService;
import com.example.Ecommerce_Muebleria.Front.services.internal.WishlistService;
import com.example.Ecommerce_Muebleria.entities.commons.Product;

import com.example.Ecommerce_Muebleria.entities.front.StoreConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private StoreConfigService storeConfigService;

    @Autowired
    private CollectionClientService collectionService;

    @Autowired
    private WishlistService wishlistService; // <--- 2. INYECCIÓN DEL SERVICIO

    @Autowired
    private OrderService orderService;

    @Autowired
    private GlobalConfigService configService;

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "0") int page,
                        Model model,
                        HttpServletRequest request) {

        // 1. CARGA SOLO LO QUE ES ÚNICO DE ESTA PÁGINA
        List<Product> products = productService.findAllActiveProducts();
        List<Product> productsNew = productService.findIsNew();
        List<Product> destacados = productService.findFeaturedProducts();
        List<Collection> collections = collectionService.getActiveCollections();

        StoreConfig storeConfig = storeConfigService.getConfig();


        // 2. BORRÁ TODO LO RELACIONADO AL CARRITO Y FAVORITOS DE ACÁ
        // (Eso ya lo hace el GlobalDataController para todas las páginas)

        model.addAttribute("featuredProducts", destacados);
        model.addAttribute("productsNew", productsNew);
        model.addAttribute("products", products);
        model.addAttribute("activeCollections", collections);
        model.addAttribute("bannerActive", storeConfig.isBannerActive());
        model.addAttribute("carouselActive", storeConfig.isCarouselActive());
        model.addAttribute("collectionsActive", storeConfig.isCollectionsActive());

        model.addAttribute("productsHabitacion", productRepository.findByCategoriesContainingAndActivoTrue("Habitacion"));
        model.addAttribute("productsBano", productRepository.findByCategoriesContainingAndActivoTrue("Baño"));
        model.addAttribute("productsBalcon", productRepository.findByCategoriesContainingAndActivoTrue("Balcon"));
        model.addAttribute("productsComedor", productRepository.findByCategoriesContainingAndActivoTrue("Comedor"));
        model.addAttribute("productsLiving", productRepository.findByCategoriesContainingAndActivoTrue("Living"));
        model.addAttribute("productsExterior", productRepository.findByCategoriesContainingAndActivoTrue("Exterior"));

        GlobalConfig config = configService.getConfig();
        model.addAttribute("superBannerUrl", config.getSuperBannerUrl());

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

        if (oidcUser != null) {
            try {
                String auth0UserId = oidcUser.getSubject();
                isFav = wishlistService.isFavorite(auth0UserId, id);
            } catch (Exception e) {
                System.err.println("❌ Error al verificar favorito en detalles: " + e.getMessage());
            }
        }

        // 2. Lógica de Recomendaciones (Algoritmo 3 + Plan B)
        List<Product> recommendedProducts = new ArrayList<>();
        try {
            // Pedimos los IDs al microservicio de órdenes (asegurate de tener este método)
            List<Long> recommendedIds = orderService.getFrequentlyBoughtTogetherIds(id);

            if (recommendedIds != null && !recommendedIds.isEmpty()) {
                // Si hay historial, hidratamos esos IDs pidiéndolos al micro 8080
                recommendedProducts = productService.findProductsByIds(recommendedIds);
            } else {
                // PLAN B: Fallback a la misma categoría si nadie los compró juntos todavía
                // Asumiendo que el producto tiene un atributo Category y armás el método en ProductService
                recommendedProducts = productService.findRecommendedProducts(product.getCategories(), product.getId(), 4);

                // Removemos el producto actual para que no se recomiende a sí mismo
                recommendedProducts.removeIf(p -> p.getId().equals(id));
            }

            // Filtramos para asegurar que no recomendamos productos dados de baja
            recommendedProducts.removeIf(p -> p.getActivo() == null || !p.getActivo());

        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar recomendaciones: " + e.getMessage());
            // El bloque try-catch asegura que si falla el micro de órdenes, la página del producto sigue cargando bien
        }

        // 3. Pasar datos al Modelo
        model.addAttribute("isFavorite", isFav);
        model.addAttribute("product", product);
        model.addAttribute("recommendedProducts", recommendedProducts);

        // DEBUG (Opcional, pero útil para verificar)
        System.out.println("======= DEBUG PRODUCTO =======");
        System.out.println("Nombre: " + product.getName());
        System.out.println("Es Favorito: " + isFav);
        System.out.println("Recomendaciones encontradas: " + recommendedProducts.size());
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

    @GetMapping("/checkout/transfer-success")
    public String transferSuccess() {
        return "transfer-success"; // Apunta al archivo transfer-success.html
    }
}