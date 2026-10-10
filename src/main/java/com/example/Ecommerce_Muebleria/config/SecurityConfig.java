package com.example.Ecommerce_Muebleria.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Configurando Seguridad Híbrida del Monolito...");

        http
                // 🛡️ CSRF activo: Thymeleaf agrega el token solo en los <form th:action method="post">.
                // Se excluyen únicamente los fetch() JSON de bajo impacto (la cookie SameSite=Lax los cubre).
                .csrf(csrf -> csrf.ignoringRequestMatchers("/cart/api/update-quantity", "/api/notifications/subscribe"))
                .authorizeHttpRequests(auth -> auth
                        // ⚠️ El orden importa: gana la primera regla que coincide.

                        // 🔒 APIs internas del monolito: el Front usa los services directamente, nadie las llama por HTTP.
                        //    Abiertas permitían leer órdenes ajenas y manipular carritos de cualquier usuario.
                        .requestMatchers("/api/cart/**", "/api/order/**").denyAll()

                        // 🔒 ADMINISTRACIÓN
                        .requestMatchers("/admin/**", "/api/backend/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/collections/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/collections/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/collections/**").hasRole("ADMIN")
                        .requestMatchers("/api/products/*/update-notes", "/api/products/*/update-specs",
                                "/api/products/question/*/answer", "/api/products/question/*/delete").hasRole("ADMIN")

                        // 🔒 USUARIOS LOGUEADOS
                        .requestMatchers("/api/products/question/save").authenticated()
                        .requestMatchers("/api/wishlist/**").authenticated()

                        // 🔓 RUTAS PÚBLICAS (E-commerce)
                        .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/collections/**").permitAll()
                        .requestMatchers("/firebase-messaging-sw.js", "/api/notifications/subscribe").permitAll()

                        // Páginas del front (cada controller valida el login donde hace falta)
                        .anyRequest().permitAll()
                )
                // 1. Configuración de LOGIN (Para usuarios web)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/auth0")
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(authorizationRequestResolver(this.clientRegistrationRepository))
                        )
                        .userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(userAuthoritiesMapper()))
                        .successHandler((request, response, authentication) -> {
                            log.info("✅ Login web exitoso: {}", authentication.getName());
                            response.sendRedirect("/");
                        })
                )
                // 2. Configuración de API (Para cuando mandes un Bearer Token)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("⚠️ Acceso no autenticado a: {}", request.getRequestURI());
                            response.sendRedirect("/oauth2/authorization/auth0");
                        })
                );

        return http.build();
    }

    // --- BEANS DE APOYO (Copiados de tus configs originales) ---

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("https://dev-q5auxkp2cqakq6jd.us.auth0.com/");
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcAuth) {
                    String namespace = "https://utn-muebles.com/roles";
                    Object rolesClaim = oidcAuth.getIdToken().getClaims().get(namespace);
                    if (rolesClaim instanceof List<?> roles) {
                        roles.forEach(role -> mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
                    }
                }
                mappedAuthorities.add(authority);
            });
            return mappedAuthorities;
        };
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository repo) {
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(c -> c.additionalParameters(p ->
                p.put("audience", "https://dev-q5auxkp2cqakq6jd.us.auth0.com/api/v2/")));
        return resolver;
    }
}