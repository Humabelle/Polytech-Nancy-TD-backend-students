package com.example.todoapp.business.exceptions;

import com.example.todoapp.presentation.dto.ErrorDto;

/**
 * Gestion des contraintes des champs titre et description
 * Encapsule un {@link ErrorDto} décrivant le champ en erreur et le message associé.
 */
public class ValidationException extends RuntimeException {

    private ErrorDto errorDto;

    /**
     * Construit une ValidationException avec les détails de l'erreur de validation.
     * @param errorDto l'objet contenant le champ en erreur et le message de validation
     */
    public ValidationException(ErrorDto errorDto) {
        this.errorDto = errorDto;
    }

    /**
     * Retourne les détails de l'erreur de validation.
     * @return le {@link ErrorDto} associé à cette exception
     */
    public ErrorDto getErrorDto() {
        return errorDto;
    }
}
