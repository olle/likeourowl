package com.studiomediatech.example.ai.data;

import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Loads the railway services data into DuckDB on first start.
 * <p>
 * Runs during context startup (not as an {@code ApplicationRunner}) so the
 * table - and its schema - is available to beans that build prompts from it.
 * The database is file-backed, so later starts reuse the existing table and
 * skip the (rather large) download.
 */
@Component
class ServicesDataLoader {

    private static final Logger log = LoggerFactory.getLogger(ServicesDataLoader.class);

    private final JdbcTemplate jdbc;
    private final Path databasePath;
    private final String source;

    ServicesDataLoader(JdbcTemplate jdbc,
            @Value("${likeourowl.data.database-path}") String databasePath,
            @Value("${likeourowl.data.services-source}") String source) {
        this.jdbc = jdbc;
        this.databasePath = Path.of(databasePath);
        this.source = source;
    }

    @PostConstruct
    void load() {
        createDatabaseDirectory();

        if (tableExists()) {
            log.info("DuckDB table 'services' already loaded ({} rows)", rowCount());
            return;
        }

        log.info("Loading 'services' table from {} ...", source);
        long started = System.nanoTime();
        jdbc.execute("CREATE TABLE services AS FROM '" + source.replace("'", "''") + "'");
        long millis = (System.nanoTime() - started) / 1_000_000;

        log.info("Loaded 'services' table: {} rows in {} ms", rowCount(), millis);
    }

    private void createDatabaseDirectory() {
        // DuckDB creates the database file itself, but not its parent directory
        Path parent = databasePath.toAbsolutePath().getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (Exception e) {
                throw new IllegalStateException("Could not create DuckDB directory " + parent, e);
            }
        }
    }

    private boolean tableExists() {
        Boolean exists = jdbc.queryForObject("""
                SELECT count(*) > 0
                FROM information_schema.tables
                WHERE table_schema = 'main' AND table_name = 'services'
                """, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private long rowCount() {
        Long count = jdbc.queryForObject("SELECT count(*) FROM services", Long.class);
        return count != null ? count : 0;
    }
}
