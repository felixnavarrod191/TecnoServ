package com.tecnoserv.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Autenticación por formulario y autorización por rol (RF-08). */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/login", "/css/**", "/error").permitAll()
                        .requestMatchers("/admin/**", "/reportes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/recepcion/**").hasRole("RECEPCIONISTA")
                        .requestMatchers("/tecnico/**").hasRole("TECNICO")
                        .requestMatchers("/cliente/**").hasRole("CLIENTE")
                        .requestMatchers("/proveedor/**").hasRole("PROVEEDOR")
                        .requestMatchers("/ordenes/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "TECNICO", "CLIENTE")
                        .anyRequest().authenticated())
                .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(l -> l.logoutSuccessUrl("/login?salio").permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
