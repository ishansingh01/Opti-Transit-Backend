package com.ishan.api_gateway.filter;


import com.ishan.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String autherHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        String token = null;
        String email = null;
        if(autherHeader != null && autherHeader.startsWith("Bearer ")) {
            token = autherHeader.substring(7);
            email = jwtUtil.extractEmail(token);
        }
        if(email != null && jwtUtil.isTokenValid(token)) {
            // In API Gateway, we don't usually hit the DB. We trust the token if valid.
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, null, null);

            // In WebFlux, we pass the authentication down the chain via contextWrite
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authToken));
        }
        return chain.filter(exchange);
    }
}
