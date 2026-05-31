package com.example.todoapp.presentation.dto;

import com.example.todoapp.business.model.Task;

public record ReadDto(int id, String title, String description, boolean done) {
    public static ReadDto toReadDto(Task task) {
        return new ReadDto(task.id(), task.title(), task.description(), task.done());
    }
}
