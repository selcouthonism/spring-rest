package org.springapi.shopping.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springapi.shopping.enums.Status;

import java.time.Instant;

/**
 * immutable DTO using record <br/>
 * This is preferred in newer Spring Boot apps for simplicity and immutability.
 * Valid on Java 16+ and Spring Boot 3.x
 */
public record OrderDto(
        Long id,
        @NotBlank(message = "Product name must not be blank") String product,
        @Min(value = 1, message = "Quantity must be at least 1") int quantity,
        Status status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC") Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC") Instant updatedAt
) {}