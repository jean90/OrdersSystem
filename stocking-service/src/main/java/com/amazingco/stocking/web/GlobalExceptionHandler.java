package com.amazingco.stocking.web;

import com.amazingco.core.exception.DomainException;
import com.amazingco.core.product.ProductNotFoundException;
import com.amazingco.core.stock.InsufficientStockException;
import com.amazingco.core.stock.InvalidReservationStateException;
import com.amazingco.core.stock.StockNotFoundException;
import com.amazingco.core.stock.StockReservationNotFoundException;
import com.amazingco.stocking.product.ProductAlreadyExistsException;
import com.amazingco.stocking.stock.StockAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Translates domain/binding failures into RFC 7807 {@link ProblemDetail} bodies with the
 * right HTTP status, so controllers stay free of try/catch. Lives in this shared {@code web}
 * package rather than under {@code product}/{@code stock} since it's cross-cutting adapter
 * infra, not a feature — the same justification as {@code usecase} being a shared package
 * in core.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class, StockNotFoundException.class,
            StockReservationNotFoundException.class})
    public ProblemDetail handleNotFound(DomainException ex) {
        return problem(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({ProductAlreadyExistsException.class, StockAlreadyExistsException.class,
            InsufficientStockException.class, InvalidReservationStateException.class})
    public ProblemDetail handleConflict(DomainException ex) {
        return problem(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ProblemDetail handleBadRequest(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Bad Request");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail(ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; ")));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Unexpected error");
        problemDetail.setDetail("An unexpected error occurred");
        return problemDetail;
    }

    private ProblemDetail problem(HttpStatus status, DomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(ex.getClass().getSimpleName());
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}
