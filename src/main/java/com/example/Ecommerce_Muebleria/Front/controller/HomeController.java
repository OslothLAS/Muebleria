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

import java.math.BigDecimal;
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
    private WishlistService wishlistService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private GlobalConfigService configService;

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "0") int page,
                        Model model,
                        HttpServletRequest request) {

        List<Product> products = productService.findAllActiveProducts();
        List<Product> productsNew = productService.findIsNew();
        List<Product> destacados = productService.findFeaturedProducts();
        List<Collection> collections = collectionService.getActiveCollections();

        StoreConfig storeConfig = storeConfigService.getConfig();

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

    // 🚀 MÉTODO DE LA TIENDA ACTUALIZADO CON TODOS LOS FILTROS
    @GetMapping("/products")
    public String shopPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "new") String sort,
                           @RequestParam(required = false) List<String> categorias,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           @RequestParam(required = false) Boolean inStock,
                           Model model,
                           HttpServletRequest request) {

        // Llamamos al nuevo método súper-filtro del servicio
        Map<String, Object> response = productService.filterProductsPaginated(
                keyword, categorias, minPrice, maxPrice, inStock, page, sort);

        // Guardamos las selecciones en el modelo para que el HTML "recuerde" lo que tildó el usuario
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("categoriasMarcadas", categorias != null ? categorias : new ArrayList<>());
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("inStock", inStock != null ? inStock : false);

        model.addAttribute("allProducts", response.get("products"));
        model.addAttribute("currentPage", response.getOrDefault("currentPage", 0));
        model.addAttribute("totalPages", response.getOrDefault("totalPages", 0));
        model.addAttribute("activePage", "products");

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

        boolean isFav = false;

        if (oidcUser != null) {
            try {
                String auth0UserId = oidcUser.getSubject();
                isFav = wishlistService.isFavorite(auth0UserId, id);
            } catch (Exception e) {
                System.err.println("❌ Error al verificar favorito en detalles: " + e.getMessage());
            }
        }

        List<Product> recommendedProducts = new ArrayList<>();
        try {
            List<Long> recommendedIds = orderService.getFrequentlyBoughtTogetherIds(id);

            if (recommendedIds != null && !recommendedIds.isEmpty()) {
                recommendedProducts = productService.findProductsByIds(recommendedIds);
            } else {
                recommendedProducts = productService.findRecommendedProducts(product.getCategories(), product.getId(), 4);
                recommendedProducts.removeIf(p -> p.getId().equals(id));
            }
            recommendedProducts.removeIf(p -> p.getActivo() == null || !p.getActivo());

        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar recomendaciones: " + e.getMessage());
        }

        model.addAttribute("isFavorite", isFav);
        model.addAttribute("product", product);
        model.addAttribute("recommendedProducts", recommendedProducts);

        return "product-details";
    }

    @GetMapping("/collection-view/{id}")
    public String viewCollectionView(@PathVariable Long id,
                                     Model model,
                                     @AuthenticationPrincipal OidcUser oidcUser,
                                     HttpSession session) {

        Collection collection = collectionService.getCollectionById(id);
        if (collection == null) return "redirect:/";

        List<Product> activeProducts = collection.getProducts().stream()
                .filter(Product::getActivo)
                .toList();

        String cartId = getCartId(oidcUser, session);
        Cart cart = cartService.getCart(cartId);

        Map<Long, Integer> productQuantities = new HashMap<>();
        if (cart != null && cart.getItems() != null) {
            for (var item : cart.getItems()) {
                productQuantities.put(item.getProductDetail().getId(), item.getQuantity());
            }
        }

        model.addAttribute("collection", collection);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("productQuantities", productQuantities);
        model.addAttribute("cart", cart);

        int cartCount = (cart != null && cart.getItems() != null) ? cart.getItems().size() : 0;
        model.addAttribute("cartCount", cartCount);

        return "collection-view";
    }

    private String getCartId(OidcUser oidcUser, HttpSession session) {
        if (oidcUser != null) {
            return oidcUser.getSubject();
        }

        if (session.isNew() || session.getAttribute("GUEST_INIT") == null) {
            session.setAttribute("GUEST_INIT", true);
        }

        return "GUEST_" + session.getId();
    }

    @GetMapping("/checkout/transfer-success")
    public String transferSuccess() {
        return "transfer-success";
    }
}