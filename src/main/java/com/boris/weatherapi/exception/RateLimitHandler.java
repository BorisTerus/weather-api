package com.boris.weatherapi.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RateLimitHandler {

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, String>> handleRateLimit(RequestNotPermitted ex) {
        Map<String, String> body = Map.of("message", "Rate limit exceeded. Try again after 1 minute.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(body);
    }
}
