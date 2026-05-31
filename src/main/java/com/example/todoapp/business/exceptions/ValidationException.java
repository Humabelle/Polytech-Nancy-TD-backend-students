package com.example.todoapp.business.exceptions;

import com.example.todoapp.presentation.dto.ErrorDto;

public class ValidationException extends RuntimeException{
    private ErrorDto errorDto;
    public ValidationException(ErrorDto errorDto) {
        this.errorDto = errorDto;
    }

    public ErrorDto getErrorDto() {
        return errorDto;
    }
}
