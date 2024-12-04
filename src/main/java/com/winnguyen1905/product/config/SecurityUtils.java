package com.winnguyen1905.product.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {
  public static Permission jsonToPermission(String stringPermission) {
    String trimmedstringPermission = stringPermission.replaceAll("^\"|\"$", "").replaceAll("^\\{|\\}$", "");
    String[] keyValuePairs = trimmedstringPermission.split(",(?![^\\{\\[]*[\\]\\}])");
    Map<String, String> map = new HashMap<>();
    for (String pair : keyValuePairs) {
      String[] entry = pair.split("=", 2);
      if (entry.length == 2) {
        map.put(entry[0].trim(), entry[1].trim());
      }
    }

    Permission dto = new Permission();
    dto.setName(map.get("name"));
    dto.setCode(map.get("code"));
    dto.setApiPath(map.get("apiPath"));
    dto.setMethod(map.get("method"));
    dto.setModule(map.get("module"));
    dto.setId(UUID.fromString(map.get("id")));
    return dto;
  }

  public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

  @Value("${jwt.access_token-validity-in-seconds}")
  private String jwtExpiration;

  public static Optional<List<String>> getCurrentUsersPermissions() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(
        securityContext.getAuthentication().getPrincipal() instanceof Jwt jwt ? jwt.getClaimAsStringList("permissions")
            : null);
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
