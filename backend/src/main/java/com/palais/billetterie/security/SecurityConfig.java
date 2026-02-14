package com.palais.billetterie.security;

import com.palais.billetterie.security.jwt.JwtService;
import com.palais.billetterie.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public SecurityConfig(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/health",
                            "/api/auth/**",
                            "/api/payments/stripe/webhook",
                            "/api/notifications/test-email"
                    ).permitAll()
                    // Evénements: lecture publique, écriture réservée
                    .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/*").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/events/**").hasAnyRole("PROMOTER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("PROMOTER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("PROMOTER", "ADMIN")

                    // Commandes: création/mise à jour par USER ou ADMIN, suppression admin, lecture admin ou user (limité à ses commandes)
                    .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN", "USER")

                    // Paiements: intent Stripe par USER/ADMIN; CRUD admin
                    .requestMatchers(HttpMethod.POST, "/api/payments/stripe/create-intent").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/payments/**").hasRole("ADMIN")

                    // Remboursements: demande par USER/ADMIN; statut par ADMIN; lecture admin
                    .requestMatchers(HttpMethod.POST, "/api/refunds").hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/refunds/*/success", "/api/refunds/*/failed").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/refunds/**").hasRole("ADMIN")

                    // Tickets: gestion admin; lecture admin ou promoter
                    .requestMatchers(HttpMethod.GET, "/api/tickets/**").hasAnyRole("ADMIN", "PROMOTER")
                    .requestMatchers(HttpMethod.POST, "/api/tickets/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/tickets/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasRole("ADMIN")

                    // Utilisateurs: admin uniquement
                    .requestMatchers("/api/users/**").hasRole("ADMIN")

                    // Tableau de bord admin
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    .requestMatchers("/api/scan/**").hasRole("CONTROLLER")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(java.util.List.of("http://localhost:3000"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(java.util.List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
