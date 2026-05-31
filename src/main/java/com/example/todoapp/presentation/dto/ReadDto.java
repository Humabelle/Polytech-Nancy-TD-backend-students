package com.example.todoapp.presentation.dto;

import com.example.todoapp.business.model.Task;

/**
 * DTO de lecture d'une tâche.
 */
public record ReadDto(int id, String title, String description, boolean done) {

    /**
     * Convertit un objet {@link Task} en ReadDto.
     * @param task la tâche à convertir
     * @return un ReadDto représentant la tâche
     */
    public static ReadDto toReadDto(Task task) {
        return new ReadDto(task.id(), task.title(), task.description(), task.done());
    }
}
