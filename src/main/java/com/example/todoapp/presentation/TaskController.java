package com.example.todoapp.presentation;
import com.example.todoapp.business.exceptions.ValidationException;
import com.example.todoapp.business.service.TaskService;
import com.example.todoapp.presentation.dto.CreateDto;
import com.example.todoapp.presentation.dto.ReadDto;
import com.example.todoapp.presentation.dto.UpdateDto;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController {

    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskService service = new TaskService();

    /**
     * Point d'entrée unique pour toutes les requêtes HTTP sur les routes /tasks et /tasks/{id}.
     * Dispatch vers la logique appropriée selon la méthode HTTP et le chemin de la requête.
     *
     * <ul>
     *   <li>{@code POST   /tasks}       — crée une nouvelle tâche</li>
     *   <li>{@code GET    /tasks}       — liste toutes les tâches (paramètre optionnel : {@code todo-only=true})</li>
     *   <li>{@code GET    /tasks/{id}}  — récupère une tâche par son identifiant</li>
     *   <li>{@code PUT    /tasks/{id}}  — met à jour une tâche existante</li>
     *   <li>{@code DELETE /tasks/{id}}  — supprime une tâche existante</li>
     * </ul>
     *
     * @param exchange l'objet {@link HttpExchange} représentant la requête et la réponse HTTP
     * @throws IOException si une erreur survient lors de la lecture du corps de la requête ou de l'écriture de la réponse
     */
    public static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        //manage options
        if ("OPTIONS".equals(method)) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        //region Manage POST /tasks
        if ("POST".equals(method) && "/tasks".equals(path)) {
            CreateDto input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), CreateDto.class);
            try {
                ReadDto createdTask = service.create(input);
                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
            } catch (ValidationException e) {
                sendResponse(exchange, 400, JsonUtils.serialize(e.getErrorDto()));
            } catch (SQLException e) {
                sendResponse(exchange, 500, null);
            }
            return;
        }
        //endregion

        //region Manage GET /tasks/{id}
        Matcher m = ID_PATH.matcher(path);
        if ("GET".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            try {
                Optional<ReadDto> task = service.getById(id);
                if (task.isPresent()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                } else {
                    sendResponse(exchange, 404, null);
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, null);
            }
            return;
        }
        //endregion

        //region Manage GET /tasks
        if ("GET".equals(method) && "/tasks".equals(path)) {
            String query = exchange.getRequestURI().getQuery();
            boolean todoOnly = (query != null) && query.contains("todo-only=true");
            try {
                List<ReadDto> taskList = service.getAll(todoOnly);
                if (!taskList.isEmpty()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(taskList));
                } else {
                    sendResponse(exchange, 204, null);
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, null);
            }
            return;
        }
        //endregion

        //region Manage PUT /tasks/{id}
        if ("PUT".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            UpdateDto updatedDto = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), UpdateDto.class);
            try {
                Optional<ReadDto> task = service.update(id, updatedDto);
                if (task.isPresent()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
            } catch (ValidationException e) {
                sendResponse(exchange, 400, JsonUtils.serialize(e.getErrorDto()));
            } catch (SQLException e) {
                sendResponse(exchange, 500, null);
            }
            return;
        }
        //endregion

        // region Manage DELETE /tasks/{id}
        if ("DELETE".equals(method) && "/tasks".equals(path)) {
            try {
                service.deleteAll();
                sendResponse(exchange, 204, null);
            } catch (SQLException e) {
                sendResponse(exchange, 500, null);
            }
            return;
        }
        //endregion

        //region Manage DELETE /tasks/{id}
        if ("DELETE".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            try {
                boolean deleted = service.delete(id);
                if (deleted) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, null);
            }
            return;
        }
        //endregion

        // Sinon → 404
        sendResponse(exchange, 404, null);
    }

    /**
     * Envoie une réponse HTTP avec le statut et le corps JSON fournis.
     * Si {@code json} est {@code null}, la réponse est envoyée sans corps ni en-tête Content-Type.
     *
     * @param exchange l'objet {@link HttpExchange} sur lequel écrire la réponse
     * @param status   le code de statut HTTP à retourner (ex : 200, 201, 204, 404, 500)
     * @param json     le corps de la réponse sérialisé en JSON, ou {@code null} pour une réponse sans corps
     * @throws IOException si une erreur survient lors de l'écriture de la réponse
     */
    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}
