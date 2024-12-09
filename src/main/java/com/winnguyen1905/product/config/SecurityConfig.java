package com.winnguyen1905.product.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig implements WebMvcConfigurer {

  public static final String[] whiteList = {
      "/storage/**", "/products/**" };

  @Bean
  PermissionCheckFilter permissionCheckFilter() {
    return new PermissionCheckFilter();
  }

  @Bean
public SecurityWebFilterChain springWebFilterChain(
    ServerHttpSecurity http,
    PermissionCheckFilter permissionCheckFilter,
    ReactiveAuthenticationManager reactiveAuthenticationManager
) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .authenticationManager(reactiveAuthenticationManager)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange(authorize -> authorize
            .pathMatchers(SecurityConfig.whiteList).permitAll()
            .pathMatchers("/ws/events").permitAll()
            .pathMatchers("/auth/**", "/stripe/**", "/swagger-ui/**", "-docs/**", "/webjars/**").permitAll()
            .pathMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .addFilterBefore(permissionCheckFilter, SecurityWebFiltersOrder.AUTHORIZATION)
        .build();
}
}
