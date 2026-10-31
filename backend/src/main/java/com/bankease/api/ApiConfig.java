package com.bankease.api;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ApiConfig {
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
    @RestControllerAdvice static class Errors {
        private static final Logger log = LoggerFactory.getLogger(Errors.class);
        @ExceptionHandler(AuthenticationException.class) public ResponseEntity<Map<String,String>> authentication(AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
        @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<Map<String,String>> status(ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()==null ? "Request failed" : e.getReason()));
        }
        @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<Map<String,String>> validation(MethodArgumentNotValidException e) {
            String message=e.getBindingResult().getFieldErrors().stream().findFirst()
                    .map(x -> x.getField()+" "+x.getDefaultMessage()).orElse("Invalid request");
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
        @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<Map<String,String>> unreadable(HttpMessageNotReadableException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is missing or contains invalid JSON"));
        }
        @ExceptionHandler(Exception.class) public ResponseEntity<Map<String,String>> error(Exception e) {
            log.error("Unhandled API error", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
}
