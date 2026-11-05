package com.example.gifserverv2.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaPatchRunner implements ApplicationRunner {

    private final DataSource dataSource;

    /** ddl-auto 로 만들어지지 않는 환경을 위해 없으면 생성한다. 컬럼 점검보다 먼저 실행된다. */
    private static final TableSpec[] REQUIRED_TABLES = {
            new TableSpec("user_tech_stack", List.of(
                    "CREATE TABLE IF NOT EXISTS user_tech_stack (" +
                            "user_id BIGINT NOT NULL, " +
                            "sort_order INT NOT NULL, " +
                            "name VARCHAR(30) NOT NULL, " +
                            "PRIMARY KEY (user_id, sort_order), " +
                            "CONSTRAINT fk_user_tech_stack_user FOREIGN KEY (user_id) REFERENCES users (id))")),
            new TableSpec("user_link", List.of(
                    "CREATE TABLE IF NOT EXISTS user_link (" +
                            "id BIGINT NOT NULL AUTO_INCREMENT, " +
                            "user_id BIGINT NOT NULL, " +
                            "title VARCHAR(30) NULL, " +
                            "url VARCHAR(500) NOT NULL, " +
                            "PRIMARY KEY (id))",
                    "CREATE INDEX idx_user_link_user_id ON user_link (user_id)")),
            new TableSpec("student_roster", List.of(
                    "CREATE TABLE IF NOT EXISTS student_roster (" +
                            "id BIGINT NOT NULL AUTO_INCREMENT, " +
                            "student_number VARCHAR(10) NOT NULL, " +
                            "name VARCHAR(50) NOT NULL, " +
                            "grade INT NOT NULL, " +
                            "class_no INT NOT NULL, " +
                            "student_no INT NOT NULL, " +
                            "PRIMARY KEY (id), " +
                            "CONSTRAINT uk_student_roster_student_number UNIQUE (student_number))")),
            new TableSpec("team_join_request", List.of(
                    "CREATE TABLE IF NOT EXISTS team_join_request (" +
                            "id BIGINT NOT NULL AUTO_INCREMENT, " +
                            "project_id BIGINT NOT NULL, " +
                            "user_id BIGINT NOT NULL, " +
                            "requested_by BIGINT NOT NULL, " +
                            "status VARCHAR(20) NOT NULL, " +
                            "reject_reason VARCHAR(200) NULL, " +
                            "created_at DATETIME(6) NOT NULL, " +
                            "responded_at DATETIME(6) NULL, " +
                            "PRIMARY KEY (id))",
                    "CREATE INDEX idx_team_join_request_user_status ON team_join_request (user_id, status)",
                    "CREATE INDEX idx_team_join_request_project_status ON team_join_request (project_id, status)")),
    };

    private static final ColumnSpec[] REQUIRED_COLUMNS = {
            new ColumnSpec("form", "created_by_user_id", "BIGINT NULL"),
            new ColumnSpec("form_field", "required", "TINYINT(1) NOT NULL DEFAULT 0"),
            new ColumnSpec("form_field", "allowed_extensions", "VARCHAR(200) NULL"),
            new ColumnSpec("form_submit", "deadline_complied_override", "TINYINT(1) NULL"),
            new ColumnSpec("form_field_answer", "project_id", "BIGINT NULL",
                    "UPDATE form_field_answer ffa " +
                            "JOIN form_submit fs ON ffa.form_submit_id = fs.id " +
                            "SET ffa.project_id = fs.project_id " +
                            "WHERE ffa.project_id IS NULL"),
            new ColumnSpec("calendar_event", "project_id", "BIGINT NULL",
                    "UPDATE calendar_event ce " +
                            "JOIN form_field_answer ffa ON ce.form_field_answer_id = ffa.id " +
                            "SET ce.project_id = ffa.project_id " +
                            "WHERE ce.project_id IS NULL"),
            new ColumnSpec("users", "major", "VARCHAR(50) NULL"),
            new ColumnSpec("users", "profile_image_url", "VARCHAR(500) NULL"),
    };

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            for (TableSpec spec : REQUIRED_TABLES) {
                ensureTable(connection, spec);
            }
            for (ColumnSpec spec : REQUIRED_COLUMNS) {
                ensureColumn(connection, spec);
            }
        } catch (Exception e) {
            log.error("[SchemaPatch] 스키마 점검 중 DB 연결에 실패했습니다.", e);
        }
    }

    private void ensureTable(Connection connection, TableSpec spec) {
        try {
            if (tableExists(connection, spec.table())) {
                return;
            }
            for (String sql : spec.createSqls()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(sql);
                }
            }
            log.warn("[SchemaPatch] 누락된 테이블 {}을(를) 생성했습니다.", spec.table());
        } catch (Exception e) {
            log.error("[SchemaPatch] {} 테이블 확인/생성 중 오류가 발생했습니다.", spec.table(), e);
        }
    }

    private boolean tableExists(Connection connection, String table) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        // DB 마다 식별자 대소문자 처리가 달라 소문자·대문자 모두 확인한다.
        for (String name : List.of(table, table.toUpperCase())) {
            try (ResultSet resultSet = metaData.getTables(connection.getCatalog(), null, name, new String[]{"TABLE"})) {
                if (resultSet.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ensureColumn(Connection connection, ColumnSpec spec) {
        try {
            if (columnExists(connection, spec.table(), spec.column())) {
                return;
            }
            String sql = "ALTER TABLE " + spec.table() + " ADD COLUMN " + spec.column() + " " + spec.definition();
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
            log.warn("[SchemaPatch] {} 테이블에 누락된 컬럼 {}을(를) 추가했습니다.", spec.table(), spec.column());

            if (spec.backfillSql() != null) {
                try (Statement statement = connection.createStatement()) {
                    int updated = statement.executeUpdate(spec.backfillSql());
                    log.warn("[SchemaPatch] {}.{} 컬럼 백필 완료 ({}건).", spec.table(), spec.column(), updated);
                }
            }
        } catch (Exception e) {
            log.error("[SchemaPatch] {}.{} 컬럼 확인/추가 중 오류가 발생했습니다.", spec.table(), spec.column(), e);
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        // DB 마다 식별자 대소문자 처리가 달라 소문자·대문자 모두 확인한다.
        for (String[] name : List.of(new String[]{table, column}, new String[]{table.toUpperCase(), column.toUpperCase()})) {
            try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, name[0], name[1])) {
                if (resultSet.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private record TableSpec(String table, List<String> createSqls) {
    }

    private record ColumnSpec(String table, String column, String definition, String backfillSql) {
        ColumnSpec(String table, String column, String definition) {
            this(table, column, definition, null);
        }
    }
}
