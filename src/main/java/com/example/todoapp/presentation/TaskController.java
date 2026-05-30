package com.example.todoapp.presentation;

import com.example.todoapp.Application;
import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final TaskDao dao = new TaskDao();
    
    public static void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        //region Manage POST /tasks
        if ("POST".equals(method) && "/tasks".equals(path)) {
            Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
            try {
                Optional<Task> createdTask = dao.save(input);
                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.get().id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask.get()));
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
                Optional<Task> task = dao.getTaskById(id);

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
                List<Task> taskList = dao.getAllTask(todoOnly);
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
            Task updatedTask = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
            try {
                Optional<Task> task = dao.editById(id, updatedTask);

                if (task.isPresent()) {
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


        //region Manage DELETE /tasks/{id}
        if ("DELETE".equals(method) && m.matches()) {
            int id = Integer.parseInt(m.group(1));
            try {
                Optional<Task> task = dao.deleteById(id);

                if (task.isPresent()) {
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

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if(nonNull(json)) {
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
