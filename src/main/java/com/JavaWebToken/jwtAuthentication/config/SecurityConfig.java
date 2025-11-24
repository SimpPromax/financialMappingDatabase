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
    //dependency injection via constructor
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

    //security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Add CORS configuration
                .csrf(csrf -> csrf.disable())//disables csrf since the apis are stateless
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/public",
                                "/public/",
                                "/public/**",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/error",
                                "/actuator/**",
                                "/api/excel/**" // ADD THIS LINE - Allow Excel endpoints without authentication
                        ).permitAll() // this are the endpoints which will be exempted from the security filter chain
                        .requestMatchers("/admin/**").hasRole("ADMIN")// demonstrate role based access
                        .anyRequest().authenticated()// all other apis pass the security chain
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(entryPoint)// for exemptions we use our customauthenticationentrypoint
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)// No HttpSession is created.This forces your app to fully authenticate every request using the JWT.
                )
                //spring security processes requests through a filter chain , if i dont specify the addbefore, the custom filter jwtRequestFilter will be added too late to be usefull
                //in this case it has to run before the UsernamePasswordAuthenticationFilter so that it dont try to trigger a login attempt fot token-authenticated request
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();// we are returning http.build() since we are modifying the httpsecurity we have to build it afresh to update the securityfilterchain
        // wethout building it the updates would not be effective and the securitycontext would not work
    }

    // CORS Configuration Source
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://172.29.80.1:5173",
                "http://192.168.100.116:5173" // <-- ADD THIS LINE for phone access
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    //password encoder bean using bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //authentication manager bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}