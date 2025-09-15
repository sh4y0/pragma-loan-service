package com.creditya.loanservice.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
public class MalformedUrlFilter implements WebFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MALFORMED_PATH_PATTERN = "[&:\\';]";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();

        if (path.matches(".*" + MALFORMED_PATH_PATTERN + ".*")) {
            Map<String, Object> emptyContent = Map.of("content", Collections.emptyList());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(emptyContent))
                    .flatMap(bytes -> exchange.getResponse()
                            .writeWith(Mono.just(exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(bytes)))
                    );
        }

        return chain.filter(exchange);
    }
}
