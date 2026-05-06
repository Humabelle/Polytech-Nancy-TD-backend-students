package com.example.todoapp;

import java.util.*;
import java.util.List;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private final Map<Integer, Task> storage = new HashMap<>();

    {
        save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        storage.put(task.id(), task);
        return task;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Retrieve all {@link Task} models.
     * @return list of all {@link Task} models.
     */
    public List <Task> findAll(boolean todoOnly) {
        List<Task> allTasks = new ArrayList<>(storage.values());
        if (todoOnly) {
            return allTasks.stream().filter(t -> !t.done()).toList();
        }
        return allTasks;
    }

    /**
     * Delete {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model deleted.
     */
    public Optional<Task> deleteById(int id) {
         return Optional.ofNullable(storage.remove(id));
    }

    /**
     * Edit {@link Task} model by id.
     * @param id  identifier of the {@link Task}.
     * @param task nouvelle task.
     * @return {@link Task} model deleted.
     */
    public Optional<Task> editById(int id, Task task) {
        return Optional.ofNullable(storage.replace(id, task));
    }
}
