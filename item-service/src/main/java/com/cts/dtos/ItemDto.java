package com.cts.dtos;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemDto {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    private LocalDateTime createdAt;
}
