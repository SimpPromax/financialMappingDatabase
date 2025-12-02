package com.JavaWebToken.jwtAuthentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.JavaWebToken.jwtAuthentication.service.CustomUserDetailsService;

import java.util.Arrays;

@Configuration
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint entryPoint;
    private final CustomUserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(CustomAuthenticationEntryPoint entryPoint,
                          CustomUserDetailsService userDetailsService,
                          JwtRequestFilter jwtRequestFilter) {
        this.entryPoint = entryPoint;
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1️⃣ Enable CORS first
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2️⃣ Disable CSRF since it's a stateless API
                .csrf(csrf -> csrf.disable())
                // 3️⃣ Exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(entryPoint)
                )
                // 4️⃣ Session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 5️⃣ Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers(
                                "/public",
                                "/public/**",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/error",
                                "/actuator/**",
                                "/api/excel-sheets",
                                "/api/excel-elements",
                                "/api/mappings",
                                "/api/mappings/**",
                                "/api/reports/**",
                                "/api/excel/**", // <-- allow Excel endpoints
                                "/api/coa/**"    // <-- allow COA endpoints if needed
                        ).permitAll()
                        // admin-only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // all others require authentication
                        .anyRequest().authenticated()
                )
                // 6️⃣ Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ CORS configuration source
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://192.168.100.118:5173", // your LAN frontend
                "http://172.29.80.1:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
