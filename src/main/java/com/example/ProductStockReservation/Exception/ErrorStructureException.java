package com.example.ProductStockReservation.Exception;


import static com.example.ProductStockReservation.Exception.ErrorTypeToHttpStatusMapper.httpStatus;


public class ErrorStructureException extends RuntimeException {

    private final int httpStatus;

    private final String message;

    public ErrorStructureException(StructuredError structuredError){

        this.httpStatus = httpStatus(structuredError.type());
        this.message =structuredError.message();

    }

}
