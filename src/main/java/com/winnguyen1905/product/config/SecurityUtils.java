package com.winnguyen1905.product.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

public final class SecurityUtils {

  public static Flux<Permission> jsonToPermission(List<String> permissionStrings) {
    ObjectMapper objectMapper = new ObjectMapper();
    return Flux.fromIterable(permissionStrings)
        .map(permissionString -> permissionString.replaceAll("^\"|\"$", "").replaceAll("^\\{|\\}$", ""))
        .map(trimmedstringPermissions -> {
          String[] keyValuePairs = trimmedstringPermissions.split(",(?![^\\{\\[]*[\\]\\}])");
          Map<String, String> PermissionMap = new HashMap<>();
          Arrays.stream(keyValuePairs).forEach(pair -> {
            String[] entry = pair.split("=", 2);
            if (entry.length == 2) PermissionMap.put(entry[0].trim(), entry[1].trim());
          });
          return objectMapper.convertValue(PermissionMap, Permission.class);
        });
  }

  public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

  @Value("${jwt.access_token-validity-in-seconds}")
  private String jwtExpiration;

  public static Flux<Permission> getCurrentUsersPermissions() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> context.getAuthentication())
        .flatMapMany(authentication -> {
          if (authentication.getPrincipal() instanceof Jwt jwt)
            return jsonToPermission(jwt.getClaimAsStringList("permissions"));
          else
            return null;
        });
  }

  public static Optional<UUID> getCurrentUserId() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    if (securityContext.getAuthentication().getPrincipal() instanceof Jwt jwt) {
      String tmp = jwt.getSubject().substring(jwt.getSubject().indexOf("/") + 1);
      return Optional.ofNullable(UUID.fromString(tmp));
    }
    return null;
  }

  public static Optional<String> getCurrentUserLogin() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
  }

  private static String extractPrincipal(Authentication authentication) {
    if (authentication == null) {
      return null;
    } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
      return springSecurityUser.getUsername();
    } else if (authentication.getPrincipal() instanceof Jwt jwt) {
      return jwt.getSubject().substring(0, jwt.getSubject().indexOf("/"));
    } else if (authentication.getPrincipal() instanceof String str) {
      return str;
    }
    return null;
  }

  public static Optional<String> getCurrentUserJWT() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(securityContext.getAuthentication())
        .filter(authentication -> authentication.getCredentials() instanceof String)
        .map(authentication -> (String) authentication.getCredentials());
  }

  public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (authentication != null
        && getAuthorities(authentication).anyMatch(authority -> Arrays.asList(authorities).contains(authority)));
  }

  public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
    return !hasCurrentUserAnyOfAuthorities(authorities);
  }

  public static boolean hasCurrentUserThisAuthority(String authority) {
    return hasCurrentUserAnyOfAuthorities(authority);
  }

  private static Stream<String> getAuthorities(Authentication authentication) {
    return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
  }
}
