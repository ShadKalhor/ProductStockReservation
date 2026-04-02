package com.example.ProductStockReservation.Exception;


import lombok.Getter;

import static com.example.ProductStockReservation.Exception.ErrorTypeToHttpStatusMapper.httpStatus;


@Getter
public class ErrorStructureException extends RuntimeException {

    private final int httpStatus;

    private final String message;

    public ErrorStructureException(StructuredError structuredError){
        super(structuredError.message());
        this.httpStatus = httpStatus(structuredError.type());
        this.message =structuredError.message();

    }

}
