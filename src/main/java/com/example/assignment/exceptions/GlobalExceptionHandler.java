package com.example.assignment.exceptions;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Class responsible for catching and rethrowing exceptions.
 * Executes the exception handler methods when the matching exceptions are thrown.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<ValidationErrorResponse> handleRuntimeExceptions(RuntimeException ex, WebRequest request) {
        HttpStatus status = resolveStatusBasedOnException(ex);

        ValidationErrorResponse error = ValidationErrorResponse.Builder.newInstance().withStatus(status.value())
                .withError(ex.getClass().getSimpleName()).withMessage(ex.getMessage())
                .atPath(request.getDescription(false).replace("uri=", "")).atTimestamp(Instant.now().toString())
                .build();
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        return buildExceptionResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    protected ResponseEntity<?> handleBadCredentialsException(AccessDeniedException ade, WebRequest request) {
        ValidationErrorResponse error = ValidationErrorResponse.Builder.newInstance()
                .atPath(request.getDescription(false).replace("uri=", "")).withStatus(HttpStatus.FORBIDDEN.value())
                .withMessage(ade.getMessage()).withError(HttpStatus.FORBIDDEN.getReasonPhrase()).build();

        error.setTimestamp(Instant.now().toString());

        logger.warn(String.format("403 Forbidden: '%s'", error.getPath()));
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {BadCredentialsException.class, CredentialsExpiredException.class})
    protected ResponseEntity<?> handleBadCredentialsException(AuthenticationException bce, WebRequest request) {
        ValidationErrorResponse error = ValidationErrorResponse.Builder.newInstance()
                .atPath(request.getDescription(false).replace("uri=", "")).withStatus(HttpStatus.UNAUTHORIZED.value())
                .withMessage(bce.getMessage()).withError(HttpStatus.UNAUTHORIZED.getReasonPhrase()).build();

        error.setTimestamp(Instant.now().toString());

        logger.warn(String.format("401 Unauthorized: '%s'", error.getPath()));
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Object> buildExceptionResponse(Exception exception, WebRequest request, HttpStatus status) {
        ValidationErrorResponse error = ValidationErrorResponse.Builder.newInstance().withStatus(status.value())
                .withError(status.getReasonPhrase()).withMessage(exception.toString())
                .atPath(request.getDescription(false).replace("uri=", "")).atTimestamp(Instant.now().toString())
                .build();

        logger.error(getFullStackTrace(exception));

        return handleExceptionInternal(exception, error, new HttpHeaders(), status, request);
    }

    private String getFullStackTrace(Exception e) {
        CharArrayWriter cw = new CharArrayWriter();
        PrintWriter w = new PrintWriter(cw);
        e.printStackTrace(w);
        w.close();
        return cw.toString();
    }

    private HttpStatus resolveStatusBasedOnException(Exception ex) {
        if (ex instanceof InvalidInputException || ex instanceof UserNotFoundException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof OperationFailedException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
