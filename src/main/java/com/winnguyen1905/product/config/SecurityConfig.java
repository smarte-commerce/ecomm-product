package com.winnguyen1905.product.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig implements WebMvcConfigurer {

  public static final String[] whiteList = {
      "/storage/**", "/products/**", "/inventories/**", "/variations/**" };

  @Bean
  PermissionCheckFilter permissionCheckFilter() {
    return new PermissionCheckFilter();
  }

  @Bean
  ModifyResponseContentFilter modifyResponseContentFilter() {
    return new ModifyResponseContentFilter();
  }

  @Bean
  SecurityWebFilterChain springWebFilterChain(
      ServerHttpSecurity http,
      PermissionCheckFilter permissionCheckFilter,
      ModifyResponseContentFilter modifyResponseContentFilter,
      ReactiveAuthenticationManager reactiveAuthenticationManager) {
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
            .anyExchange().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .addFilterBefore(permissionCheckFilter, SecurityWebFiltersOrder.AUTHORIZATION)
        .addFilterAfter(modifyResponseContentFilter, SecurityWebFiltersOrder.LAST)
        .build();
  }

  @Bean
  RegisteredClientRepository registeredClientRepository() {
    RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("client-id")
        .clientSecret("{noop}client-secret")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .scope("read")
        .scope("write")
        .redirectUri("http://localhost:8080/login/oauth2/code/my-client")
        .build();
    return new InMemoryRegisteredClientRepository(client);
  }
}
