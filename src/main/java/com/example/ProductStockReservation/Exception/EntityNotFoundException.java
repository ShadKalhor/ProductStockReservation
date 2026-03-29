package com.example.ProductStockReservation.Exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, Object id) {
        super(entityName + " with id " + id + " was not found.");
    }
}
