package com.example.ProductStockReservation.Exception;

import io.vavr.control.Option;
import org.hibernate.HibernateException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        final var errorResponse = new ErrorResponse("ENTITY_NOT_FOUND");
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        final var errorResponse = new ErrorResponse("INVALID_REQUEST");
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(HibernateException.class)
    ResponseEntity<ErrorResponse> handleHibernateException(HibernateException ex) {
        final var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        final var errorResponse = new ErrorResponse("Something went wrong");
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.error("Handle MethodArgumentTypeMismatchException", ex);
        final var errorResponse = new ErrorResponse("INVALID_REQUEST");
        return ResponseEntity.badRequest().body(errorResponse);
    }
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        logger.error("Handling MaxUploadSizeExceededException", ex);
        final var errorResponse = new ErrorResponse("INVALID_REQUEST");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("handling Exception", ex);
        final var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        final var errorResponse = new ErrorResponse("Something went wrong");
        return new ResponseEntity<>(errorResponse, httpStatus);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        logger.error("Handle HttpMessageNotReadableException", ex);
        final var errorResponse = new ErrorResponse("INVALID_REQUEST");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        logger.error("Handle MethodArgumentNotValidException", ex);
        final var errorResponse = new ErrorResponse("INVALID_REQUEST");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private ResponseEntity<Object> handleBindingResultErrors(BindingResult bindingResult) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        Option.ofOptional(
                                        bindingResult.getFieldErrors().stream()
                                                .map(FieldError::getField)
                                                .findFirst()
                                )
                                .getOrElse("Something went wrong")
                )
        );
    }
}