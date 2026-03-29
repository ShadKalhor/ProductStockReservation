package com.example.ProductStockReservation.Exception;

import org.springframework.http.HttpStatus;

public class ErrorTypeToHttpStatusMapper {

    private ErrorTypeToHttpStatusMapper(){throw new UnsupportedOperationException("Should Not Be Instantiated");}

    public static int httpStatus(ErrorType errorType){

        if(errorType == ErrorType.VALIDATION_ERROR){
            return HttpStatus.BAD_REQUEST.value();
        }else if(errorType == ErrorType.NOT_FOUND_ERROR){
            return HttpStatus.NOT_FOUND.value();
        }else if(errorType == ErrorType.SERVER_ERROR){
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        return HttpStatus.BAD_REQUEST.value();

    }

}
