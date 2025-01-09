// package com.winnguyen1905.product.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
// import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
// import org.springframework.security.config.web.server.ServerHttpSecurity;
// import org.springframework.security.web.server.SecurityWebFilterChain;
// import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

// @Configuration
// @EnableWebFluxSecurity
// public class SecurityConfig {

//   public static final String[] whiteList = {
//       "/storage/**", "/products/**", "/inventories/**", "/variations/**" };

//   // @Bean
//   // PermissionCheckFilter permissionCheckFilter() {
//   // return new PermissionCheckFilter();
//   // }

//   // @Bean
//   // ModifyResponseContentFilter modifyResponseContentFilter() {
//   // return new ModifyResponseContentFilter();
//   // }

//   @Bean
//   SecurityWebFilterChain springWebFilterChain(
//       ServerHttpSecurity http,
//       ModifyResponseContentFilter modifyResponseContentFilter) {
//     return http
//       .authenticationManager(null)
//       .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
//       .csrf(ServerHttpSecurity.CsrfSpec::disable)
//       .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
//       .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
//       .authorizeExchange(authorize -> authorize
//         .pathMatchers(SecurityConfig.whiteList).permitAll()
//         .pathMatchers("/ws/events").permitAll()
//         .pathMatchers("/auth/**", "/stripe/**", "/swagger-ui/**", "-docs/**", "/webjars/**").permitAll()
//         .pathMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
//         .anyExchange().authenticated())
//       .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
//       // .addFilterBefore(permissionCheckFilter, SecurityWebFiltersOrder.AUTHORIZATION)/
//       // .addFilterAfter(modifyResponseContentFilter, SecurityWebFiltersOrder.LAST)
//       .build();
//   }
// }
