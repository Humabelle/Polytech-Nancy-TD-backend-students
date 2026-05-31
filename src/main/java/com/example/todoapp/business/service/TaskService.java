package com.example.todoapp.business.service;

import com.example.todoapp.business.exceptions.ValidationException;
import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.presentation.dto.CreateDto;
import com.example.todoapp.presentation.dto.ErrorDto;
import com.example.todoapp.presentation.dto.ReadDto;
import com.example.todoapp.presentation.dto.UpdateDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskService {
    private final TaskDao dao = new TaskDao();

    public ReadDto create(CreateDto dto) throws SQLException {
        Task task = new Task(null, dto.title(), dto.description(), false);
        validate(task.title(), task.description());
        Task saved = dao.save(task).get();
        return ReadDto.toReadDto(saved);
    }

    public Optional<ReadDto> getById(int id) throws SQLException {

        Optional <Task> optTask = dao.getTaskById(id);
        return optTask.map(ReadDto::toReadDto);
    }

    public List<ReadDto> getAll(boolean todoOnly) throws SQLException {
        List<ReadDto> result = new ArrayList<>();
        for (Task task : dao.getAllTask(todoOnly)) {
            result.add(ReadDto.toReadDto(task));
        }
        return result;
    }

    public Optional<ReadDto> update(int id, UpdateDto dto) throws SQLException {
        Task task = new Task(null, dto.title(), dto.description(), dto.done());
        validate(task.title(), task.description());
        Optional <Task> optTask = dao.editById(id, task);
        return optTask.map(ReadDto::toReadDto);
    }

    public boolean delete(int id) throws SQLException {
        Optional<Task> task = dao.deleteById(id);
        return task.isPresent();
    }
    private static void validate(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new ValidationException(new ErrorDto("title", "Le titre est obligatoire"));
        }
        if (title.length() > 50) {
            throw new ValidationException(new ErrorDto("title", "Le titre ne peut pas dépasser 50 caractères"));
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException(new ErrorDto("description", "La description est obligatoire"));
        }
        if (description.length() > 255) {
            throw new ValidationException(new ErrorDto("description", "La description ne peut pas dépasser 255 caractères"));
        }
    }

}
