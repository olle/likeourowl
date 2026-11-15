package com.studiomediatech.example.ai.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Read-only access to the railway services context data in DuckDB.
 * <p>
 * Intended as the context source for the AI models: {@link #schema()} gives a
 * compact description of the table for prompting, and {@link #find(String)}
 * runs ad-hoc SELECT queries with a hard row cap.
 */
@Repository
public class ServicesContextRepository {

    private static final int DEFAULT_MAX_ROWS = 100;
    private static final int HARD_MAX_ROWS = 1_000;

    private final JdbcTemplate jdbc;

    public ServicesContextRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long rowCount() {
        Long count = jdbc.queryForObject("SELECT count(*) FROM services", Long.class);
        return count != null ? count : 0;
    }

    /**
     * The table definition as a single CREATE TABLE statement, suitable for
     * inclusion in a prompt.
     */
    public String schema() {
        List<Map<String, Object>> columns = jdbc.queryForList("DESCRIBE services");

        StringBuilder ddl = new StringBuilder("CREATE TABLE services (\n");
        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> column = columns.get(i);
            ddl.append("  \"").append(column.get("column_name")).append("\" ").append(column.get("column_type"));
            ddl.append(i < columns.size() - 1 ? ",\n" : "\n");
        }
        return ddl.append(");").toString();
    }

    public List<Map<String, Object>> find(String sql) {
        return find(sql, DEFAULT_MAX_ROWS);
    }

    /**
     * Runs a single read-only query and returns at most {@code maxRows} rows.
     *
     * @throws IllegalArgumentException if the statement is not a SELECT
     */
    public List<Map<String, Object>> find(String sql, int maxRows) {
        String statement = sql == null ? "" : sql.strip();
        String lower = statement.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("select") && !lower.startsWith("with")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed");
        }
        if (statement.contains(";")) {
            throw new IllegalArgumentException("Only a single statement is allowed");
        }

        int limit = Math.clamp(maxRows, 1, HARD_MAX_ROWS);
        String capped = "SELECT * FROM (" + statement + ") AS result LIMIT " + limit;

        return jdbc.query(capped, (rs, rowNum) -> {
            var row = new LinkedHashMap<String, Object>();
            var meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            return row;
        });
    }
}
