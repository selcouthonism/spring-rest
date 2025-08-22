package org.springapi.shopping.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springapi.shopping.enums.Status;

import java.time.Instant;

/**
 * immutable DTO using record <br/>
 * This is preferred in newer Spring Boot apps for simplicity and immutability.
 * Valid on Java 16+ and Spring Boot 3.x
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDto(
        Long id,

        @NotNull(message = "Product field is required")
        @NotBlank(message = "Product name must not be blank")
        String product,

        @NotNull(message = "Quantity field is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        Status status,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        Instant updatedAt
) {}