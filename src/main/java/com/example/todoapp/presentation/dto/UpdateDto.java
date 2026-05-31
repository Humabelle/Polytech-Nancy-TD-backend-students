package com.example.todoapp.presentation.dto;

/**
 * DTO de mise à jour d'une tâche.
 */
public record UpdateDto(String title, String description, Boolean done) {
}
