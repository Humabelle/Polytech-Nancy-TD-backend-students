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

    /**
     * Crée une nouvelle tâche à partir des données fournies.
     * @param dto les données nécessaires à la création de la tâche
     * @return un ReadDto représentant la tâche créée
     * @throws SQLException si une erreur survient lors de l'accès à la base de données
     * @throws ValidationException si le titre ou la description sont invalides
     */
    public ReadDto create(CreateDto dto) throws SQLException {
        Task task = new Task(null, dto.title(), dto.description(), false);
        validate(task.title(), task.description());
        Task saved = dao.save(task).get();
        return ReadDto.toReadDto(saved);
    }

    /**
     * Récupère une tâche par son identifiant.
     * @param id l'identifiant unique de la tâche
     * @return un Optional contenant le ReadDto si la tâche existe, vide sinon
     * @throws SQLException si une erreur survient lors de l'accès à la base de données
     */
    public Optional<ReadDto> getById(int id) throws SQLException {
        Optional<Task> optTask = dao.getTaskById(id);
        return optTask.map(ReadDto::toReadDto);
    }

    /**
     * Récupère toutes les tâches, avec un filtre optionnel sur les tâches à faire.
     * @param todoOnly si {@code true}, retourne uniquement les tâches non terminées
     * @return la liste des tâches sous forme de ReadDto
     * @throws SQLException si une erreur survient lors de l'accès à la base de données
     */
    public List<ReadDto> getAll(boolean todoOnly) throws SQLException {
        List<ReadDto> result = new ArrayList<>();
        for (Task task : dao.getAllTask(todoOnly)) {
            result.add(ReadDto.toReadDto(task));
        }
        return result;
    }

    /**
     * Met à jour une tâche existante identifiée par son id.
     * @param id  l'identifiant unique de la tâche à modifier
     * @param dto les nouvelles données à appliquer à la tâche
     * @return un Optional contenant le ReadDto mis à jour si la tâche existe, vide sinon
     * @throws SQLException si une erreur survient lors de l'accès à la base de données
     * @throws ValidationException si le titre ou la description sont invalides
     */
    public Optional<ReadDto> update(int id, UpdateDto dto) throws SQLException {
        Task task = new Task(null, dto.title(), dto.description(), dto.done());
        validate(task.title(), task.description());
        Optional<Task> optTask = dao.editById(id, task);
        return optTask.map(ReadDto::toReadDto);
    }

    /**
     * Supprime une tâche par son identifiant.
     * @param id l'identifiant unique de la tâche à supprimer
     * @return {@code true} si la tâche a été supprimée, {@code false} si elle n'existait pas
     * @throws SQLException si une erreur survient lors de l'accès à la base de données
     */
    public boolean delete(int id) throws SQLException {
        Optional<Task> task = dao.deleteById(id);
        return task.isPresent();
    }
    /**
     * supprimer les tâches
     */
    public void deleteAll() throws SQLException {
        dao.deleteAll();
    }

    /**
     * Valide le titre et la description d'une tâche.
     * @param title       le titre à valider
     * @param description la description à valider
     * @throws ValidationException pour gérer les contraintes concernant le titre et la description
     */
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
