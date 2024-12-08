package com.winnguyen1905.product.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.servlet.HandlerMapping;

import com.winnguyen1905.product.exception.BaseException;

import reactor.core.publisher.Mono;

@Component
public class PermissionCheckFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().toString();
    String requestURI = exchange.getRequest().getURI().toString();
    String httpMethod = exchange.getRequest().getMethod().toString();
    return SecurityUtils.getCurrentUsersPermissions()
        .flatMap(permissionStrings -> SecurityUtils.jsonToPermission(permissionStrings).fromIterable(null))
        .flatMap(permissions -> {
          Boolean isAllow = permissions.stream().anyMatch(item -> (item.apiPath().equals(path) && item.method().equals(httpMethod)) || item.apiPath().equals("/api/**"));
          if (isAllow == false)
            throw new BaseException("Cannot use endpoint " + path, 403, "Forbidden");
        })
        .switchIfEmpty(Mono.defer(() -> {
          exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
          return exchange.getResponse().setComplete();
        }));
    // return chain.filter(exchange);
  }
}
