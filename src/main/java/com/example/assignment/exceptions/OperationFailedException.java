package com.example.assignment.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class OperationFailedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2L;

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Object... formatParams) {
        this(String.format(message, formatParams));
    }
}