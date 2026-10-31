// package com.bankease.security;

// import org.springframework.context.annotation.*;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.HttpStatusEntryPoint;
// import org.springframework.http.HttpStatus;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

// @Configuration
// @EnableMethodSecurity
// public class SecurityConfig {
//     @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
//     @Bean DaoAuthenticationProvider authenticationProvider(
//             BankUserDetailsService users, PasswordEncoder encoder) {
//         DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//         provider.setUserDetailsService(users);
//         provider.setPasswordEncoder(encoder);
//         return provider;
//     }
//     @Bean SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
//         return http.csrf(c -> c.disable())
//             .cors(Customizer.withDefaults())
//             .authenticationProvider(provider)
//             .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
//             .authorizeHttpRequests(a -> a
//                 .requestMatchers("/api/auth/**", "/api/health", "/error").permitAll()
//                 .anyRequest().authenticated())
//             .sessionManagement(s -> s.sessionFixation().migrateSession()
//                 .maximumSessions(3)).build();
//     }
// }

package com.bankease.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(
            BankUserDetailsService users, PasswordEncoder encoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);

        return provider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:5174"
        ));

        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider provider) throws Exception {

        return http
            .csrf(c -> c.disable())
            .cors(Customizer.withDefaults())
            .authenticationProvider(provider)
            .exceptionHandling(e -> e.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**", "/api/health", "/error").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(s -> s.sessionFixation().migrateSession()
                .maximumSessions(3))
            .build();
    }
}
