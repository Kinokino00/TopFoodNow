package com.example.topfoodnow.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // JSON 格式參數校驗
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String combined = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return createErrorResponse(HttpStatus.BAD_REQUEST, combined, request);
    }

    // 表單驗證錯誤
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorDto> handleBindException(BindException ex, HttpServletRequest request) {
        String combined = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return createErrorResponse(HttpStatus.BAD_REQUEST, combined, request);
    }

    // 約束驗證錯誤
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String combined = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return createErrorResponse(HttpStatus.BAD_REQUEST, combined, request);
    }

    // 自定義無效輸入例外
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorDto> handleInvalidInputException(InvalidInputException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 找不到資源
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDto> handleNoSuchElementException(NoSuchElementException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 存取被拒絕
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // 不支援的 HTTP 方法
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    // 處理所有其他未捕獲的例外
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneralException(Exception ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + ex.getMessage(), request);
    }

    private ResponseEntity<ErrorDto> createErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorDto errorDto = new ErrorDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorDto);
    }
}
