package com.winnguyen1905.auth.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
@SuppressWarnings("null")
public class ModifyResponseContentFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Intercepting the response body to modify it
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Example: Modify the response body (e.g., wrapping it in a custom structure)
            exchange.getResponse().beforeCommit(() -> {
                // Modify response content before it is written
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                return Mono.empty();
            });
        }));
    }
}
