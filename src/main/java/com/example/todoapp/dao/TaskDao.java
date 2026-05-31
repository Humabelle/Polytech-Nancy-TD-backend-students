package com.example.todoapp.dao;

import com.example.todoapp.business.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {
    private static final String url = "jdbc:sqlite:tasks.db";

    public void createTable() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt  = conn.createStatement()) {
            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS mytasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title VARCHAR(55) NOT NULL,
                description VARCHAR(255) NOT NULL,
                done INTEGER DEFAULT 0 );
            """);
        }
    }

    public void initializeTable() throws SQLException {
        save(new Task(0, "Cuisiner", "Cuisiner atassi à 22h", false));
        save(new Task(0, "Discuter", "Répondre aux messages", false));
        save(new Task(0, "Réviser", "Réviser réseaux et analyse numérique", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task object.
     */
    public Optional<Task> save(Task task) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = """
            INSERT INTO mytasks (title, description, done)
            VALUES (?,?,0)
            """;
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.executeUpdate();

            int id = pstmt.getGeneratedKeys().getInt(1);
            return getTaskById(id);
        }
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> getTaskById(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT * FROM mytasks WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            } else {
                return Optional.of(new Task(rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getBoolean("done")));
            }
        }
    }

    /**
     * Retrieve all {@link Task} models.
     * @return list of all {@link Task} models.
     */
    public List<Task> getAllTask(boolean todoOnly) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            List<Task> tasks = new ArrayList<>();
            String sql = "SELECT * FROM mytasks";
            if (todoOnly) {
                sql = "SELECT * FROM mytasks WHERE done=1";
            }
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getBoolean("done")));
            }
            return tasks;
        }
    }

    /**
     * Delete {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model deleted.
     */
    public Optional<Task> deleteById(int id) throws SQLException {
        Optional<Task> deletedTask = getTaskById(id);
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "DELETE FROM mytasks WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return deletedTask;
        }
    }

    /**
     * Edit {@link Task} model by id.
     * @param id  identifier of the {@link Task}.
     * @param task nouvelle task.
     * @return {@link Task} model deleted.
     */
    public Optional<Task> editById(int id, Task task) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "UPDATE mytasks SET title=?, description=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, id);
            pstmt.executeUpdate();

            return getTaskById(id);
        }
    }
}