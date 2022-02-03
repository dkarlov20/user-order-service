package com.griddynamics.gridu.user.order.service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.griddynamics.gridu.user.order.service.util.ContextualLogging.REQUEST_ID_KEY;
import static com.griddynamics.gridu.user.order.service.util.ContextualLogging.logOnEach;
import static org.springframework.util.CollectionUtils.firstElement;

@Slf4j
@Component
public class RequestFilter implements WebFilter {
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = getRequestId(request.getHeaders());

        return chain
                .filter(exchange)
                .doOnEach(logOnEach(r -> log.info("{} {}", request.getMethod(), request.getURI())))
                .contextWrite(Context.of(REQUEST_ID_KEY, requestId));
    }


    private String getRequestId(HttpHeaders headers) {
        List<String> requestIdHeaders = headers.get(REQUEST_ID_HEADER);
        return Optional.ofNullable(firstElement(requestIdHeaders))
                .orElse(UUID.randomUUID().toString());
    }
}