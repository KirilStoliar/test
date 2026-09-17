package com.stoliar.product.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", "Not Found",
                        "message", exception.getMessage(),
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<Map<String, String>> errors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> Map.of(
                                "field", error.getField(),
                                "message",
                                error.getDefaultMessage() == null
                                        ? "Invalid value"
                                        : error.getDefaultMessage()
                        ))
                        .toList();

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Validation failed",
                        "errors", errors,
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String parameterName = exception.getName();

        String message = "Invalid value for parameter: "
                + parameterName;

        if (exception.getRequiredType() != null) {
            message += ". Expected type: "
                    + exception.getRequiredType().getSimpleName();
        }

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Invalid parameter",
                        "message", message,
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Bad Request",
                        "message", exception.getMessage(),
                        "path", request.getRequestURI()
                ));
    }
}