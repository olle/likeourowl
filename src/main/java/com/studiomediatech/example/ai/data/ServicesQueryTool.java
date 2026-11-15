package com.studiomediatech.example.ai.data;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Exposes the railway context data to the model as a tool.
 */
@Component
public class ServicesQueryTool {

    private static final int MAX_ROWS = 50;

    private final ServicesContextRepository repository;

    ServicesQueryTool(ServicesContextRepository repository) {
        this.repository = repository;
    }

    @Tool(name = "query_services", description = """
            Runs one read-only SQL SELECT statement against the Dutch railway
            'services' table (DuckDB, March 2025) and returns the rows as a
            markdown table. Column names contain spaces and colons, so quote
            every identifier with double quotes. Returns at most 50 rows.
            """)
    public String queryServices(@ToolParam(description = "A single SQL SELECT statement") String sql) {
        try {
            List<Map<String, Object>> rows = repository.find(sql, MAX_ROWS);
            return toMarkdown(rows);
        } catch (Exception e) {
            // Returned instead of thrown so the model can correct its query
            return "Query failed: " + e.getMessage();
        }
    }

    private static String toMarkdown(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "No rows returned.";
        }

        var columns = rows.getFirst().keySet();
        var table = new StringBuilder("| ").append(String.join(" | ", columns)).append(" |\n|");
        columns.forEach(column -> table.append("---|"));
        table.append('\n');

        for (Map<String, Object> row : rows) {
            table.append("| ");
            for (String column : columns) {
                Object value = row.get(column);
                table.append(value == null ? "" : String.valueOf(value).replace("|", "\\|")).append(" | ");
            }
            table.append('\n');
        }

        if (rows.size() >= MAX_ROWS) {
            table.append("\n(truncated at ").append(MAX_ROWS).append(" rows)");
        }
        return table.toString();
    }
}
