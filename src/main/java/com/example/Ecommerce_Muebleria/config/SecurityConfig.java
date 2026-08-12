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
                .csrf(csrf -> csrf.disable()) // Mantenemos disable por compatibilidad con tus servicios previos
                .authorizeHttpRequests(auth -> auth
                        // 🔓 RUTAS PÚBLICAS (E-commerce)
                        .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/collections/**").permitAll()
                        .requestMatchers("/api/cart/**", "/api/cart/checkout").permitAll()
                        .requestMatchers("/firebase-messaging-sw.js", "/api/notifications/subscribe").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/collections/**").permitAll() // Según tu código anterior era libre
                        .requestMatchers("/api/products/question/save").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().permitAll() // Permitimos el resto para no romper el Front durante la migración
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