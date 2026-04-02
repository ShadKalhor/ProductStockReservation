package com.example.ProductStockReservation.Exception;


import lombok.Getter;

import static com.example.ProductStockReservation.Exception.ErrorTypeToHttpStatusMapper.httpStatus;


public class ErrorStructureException extends RuntimeException {

    @Getter
    private final int httpStatus;

    @Getter
    private final String message;

    public ErrorStructureException(StructuredError structuredError){
        super(structuredError.message());
        this.httpStatus = httpStatus(structuredError.type());
        this.message =structuredError.message();

    }

}
