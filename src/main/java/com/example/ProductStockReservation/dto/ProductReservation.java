package com.example.ProductStockReservation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductReservation(@NotNull UUID id, @NotNull int quantity) {
}
