// package com.winnguyen1905.product.config;

// import java.util.Arrays;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.UUID;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.context.ReactiveSecurityContextHolder;
// import org.springframework.security.core.context.SecurityContext;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
// import org.springframework.security.oauth2.jwt.Jwt;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import reactor.core.publisher.Flux;
// import reactor.core.publisher.Mono;

// public final class SecurityUtils {

//   @Value("${jwt.access_token-validity-in-seconds}")
//   private String jwtExpiration;

//   public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

//   private static Flux<Permission> jsonToPermission(List<String> permissionStrings) {
//     ObjectMapper objectMapper = new ObjectMapper();
//     return Flux.fromIterable(permissionStrings)
//         .map(permissionString -> permissionString.trim().replaceAll("^\"|\"$", "").replaceAll("^\\{|\\}$", ""))
//         .map(trimmedString -> {
//           Map<String, String> permissionMap = Arrays.stream(trimmedString.split(",(?![^\\{\\[]*[\\]\\}])"))
//               .map(pair -> pair.split("=", 2))
//               .filter(entry -> entry.length == 2)
//               .collect(Collectors.toMap(
//                   entry -> entry[0].trim(),
//                   entry -> entry[1].trim()));
//           return objectMapper.convertValue(permissionMap, Permission.class);
//         });
//   }

//   public static Flux<Permission> getCurrentUsersPermissions() {
//     return ReactiveSecurityContextHolder.getContext()
//         .map(SecurityContext::getAuthentication)
//         .flatMapMany(authentication -> extractPermissionsFromAuthentication(authentication))
//         .switchIfEmpty(Flux.error(new IllegalStateException("No permissions found for the current user")));
//   }

//   private static Flux<Permission> extractPermissionsFromAuthentication(Authentication authentication) {
//     if (authentication.getPrincipal() instanceof Jwt jwt) {
//       List<String> permissions = jwt.getClaimAsStringList("permissions");
//       return jsonToPermission(permissions);
//     } else {
//       return Flux.empty();
//     }
//   }

//   public static Mono<UUID> getCurrentUserId() {
//     return Mono.deferContextual(contextView -> {
//       SecurityContext securityContext = contextView.getOrDefault(SecurityContext.class, null);
//       if (securityContext == null || securityContext.getAuthentication() == null) {
//         return Mono.empty();
//       }

//       Object principal = securityContext.getAuthentication().getPrincipal();
//       if (principal instanceof Jwt jwt) {
//         String subject = jwt.getSubject();
//         int separatorIndex = subject.indexOf("/") + 1;

//         if (separatorIndex > 0 && separatorIndex < subject.length()) {
//           try {
//             return Mono.just(UUID.fromString(subject.substring(separatorIndex)));
//           } catch (IllegalArgumentException e) {
//             return Mono.empty();
//           }
//         }
//       }
//       return Mono.empty();
//     });
//   }

//   public static Optional<String> getCurrentUserLogin() {
//     SecurityContext securityContext = SecurityContextHolder.getContext();
//     return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
//   }

//   private static String extractPrincipal(Authentication authentication) {
//     if (authentication == null) {
//       return null;
//     } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
//       return springSecurityUser.getUsername();
//     } else if (authentication.getPrincipal() instanceof Jwt jwt) {
//       return jwt.getSubject().substring(0, jwt.getSubject().indexOf("/"));
//     } else if (authentication.getPrincipal() instanceof String str) {
//       return str;
//     }
//     return null;
//   }

//   public static Optional<String> getCurrentUserJWT() {
//     SecurityContext securityContext = SecurityContextHolder.getContext();
//     return Optional.ofNullable(securityContext.getAuthentication())
//         .filter(authentication -> authentication.getCredentials() instanceof String)
//         .map(authentication -> (String) authentication.getCredentials());
//   }

//   public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
//     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//     return (authentication != null
//         && getAuthorities(authentication).anyMatch(authority -> Arrays.asList(authorities).contains(authority)));
//   }

//   public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
//     return !hasCurrentUserAnyOfAuthorities(authorities);
//   }

//   public static boolean hasCurrentUserThisAuthority(String authority) {
//     return hasCurrentUserAnyOfAuthorities(authority);
//   }

//   private static Stream<String> getAuthorities(Authentication authentication) {
//     return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
//   }
// }
