package com.winnguyen1905.product.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.winnguyen1905.product.util.OptionalExtractor;

import reactor.core.publisher.Mono;

@Component
public class PermissionCheckFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    // String path = exchange.getRequest().getPath().toString();
    // String requestURI = exchange.getRequest().getURI().toString();
    // String httpMethod = exchange.getRequest().getMethod().toString();
    return SecurityUtils.getCurrentUsersPermissions(OptionalExtractor.currentUserId())
        .filter(permission -> permission.apiPath().equals(exchange.getRequest().getPath().toString())
            && permission.method().equals(exchange.getRequest().getMethod().toString())
            || permission.apiPath().equals("/api/**"))
        .hasElements()
        .flatMap(hasPermission -> {
          if (hasPermission) {
            return chain.filter(exchange);
          }
          exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
          return exchange.getResponse().setComplete();
        });
  }
}
