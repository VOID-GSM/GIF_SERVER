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

    /**
     * ddl-auto 로 만들어지지 않는 환경을 위해 없으면 생성한다. 컬럼 점검보다 먼저 실행된다.
     * 인덱스는 테이블과 따로 확인한다. 테이블이 이미 있어도(ddl-auto 로 만들어졌거나, 이전 실행에서 인덱스 생성만 실패한 경우)
     * 빠진 인덱스는 만든다.
     */
    private static final TableSpec[] REQUIRED_TABLES = {
            new TableSpec("user_tech_stack",
                    "CREATE TABLE IF NOT EXISTS user_tech_stack (" +
                            "user_id BIGINT NOT NULL, " +
                            "sort_order INT NOT NULL, " +
                            "name VARCHAR(30) NOT NULL, " +
                            "PRIMARY KEY (user_id, sort_order), " +
                            "CONSTRAINT fk_user_tech_stack_user FOREIGN KEY (user_id) REFERENCES users (id))"),
            new TableSpec("user_link",
                    "CREATE TABLE IF NOT EXISTS user_link (" +
                            "id BIGINT NOT NULL AUTO_INCREMENT, " +
                            "user_id BIGINT NOT NULL, " +
                            "title VARCHAR(30) NULL, " +
                            "url VARCHAR(500) NOT NULL, " +
                            "PRIMARY KEY (id))",
                    new IndexSpec("idx_user_link_user_id", "user_id")),
            new TableSpec("student_roster",
                    "CREATE TABLE IF NOT EXISTS student_roster (" +
                            "id BIGINT NOT NULL AUTO_INCREMENT, " +
                            "student_number VARCHAR(10) NOT NULL, " +
                            "name VARCHAR(50) NOT NULL, " +
                            "grade INT NOT NULL, " +
                            "class_no INT NOT NULL, " +
                            "student_no INT NOT NULL, " +
                            "PRIMARY KEY (id), " +
                            "CONSTRAINT uk_student_roster_student_number UNIQUE (student_number))"),
            new TableSpec("team_join_request",
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
                    new IndexSpec("idx_team_join_request_user_status", "user_id, status"),
                    new IndexSpec("idx_team_join_request_project_status", "project_id, status")),
    };

    /**
     * 없으면 추가한다. backfillSql 은 컬럼이 이미 있어도 매번 실행해서, 이전 실행에서 백필만 실패했어도 다시 시도한다.
     * 그래서 backfillSql 은 여러 번 실행해도 결과가 같아야 한다. (예: WHERE 새컬럼 IS NULL)
     */
    private static final ColumnSpec[] REQUIRED_COLUMNS = {
            new ColumnSpec("form", "created_by_user_id", "BIGINT NULL"),
            new ColumnSpec("form_field", "required", "TINYINT(1) NOT NULL DEFAULT 0"),
            new ColumnSpec("form_field", "allowed_extensions", "VARCHAR(200) NULL"),
            new ColumnSpec("user_item", "created_at", "DATETIME(6) NULL"),
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
            new ColumnSpec("project_comment", "hidden", "TINYINT(1) NOT NULL DEFAULT 0"),
            new ColumnSpec("project_comment", "deleted_at", "DATETIME(6) NULL"),
            new ColumnSpec("users", "major", "VARCHAR(50) NULL"),
            new ColumnSpec("users", "profile_image_url", "VARCHAR(500) NULL"),
    };

    /**
     * Hibernate 6 는 MySQL 에서 EnumType.STRING 컬럼을 enum('A','B') 타입으로 만든다.
     * 이후 Java enum 에 값이 추가되면(예: Role.EXTERNAL) ddl-auto=update 가 컬럼을 바꾸지 않아 저장 시
     * "Data truncated for column" 오류가 난다. enum 타입으로 남아 있는 컬럼을 VARCHAR 로 바꾼다.
     */
    private static final ColumnTypeSpec[] ENUM_COLUMNS_TO_VARCHAR = {
            new ColumnTypeSpec("users", "role", "VARCHAR(20) NOT NULL"),
            new ColumnTypeSpec("users", "admin_role", "VARCHAR(20) NULL"),
            new ColumnTypeSpec("users", "client_role", "VARCHAR(20) NULL"),
            new ColumnTypeSpec("cat_chase_judgment", "judgment", "VARCHAR(20) NOT NULL"),
            new ColumnTypeSpec("cat_chase_judgment", "confidence", "VARCHAR(20) NULL"),
    };

    /**
     * ddl-auto=update 는 NOT NULL 컬럼을 NULL 허용으로 바꾸지 않는다. NOT NULL 로 남아 있는 컬럼을 NULL 허용으로 바꾼다.
     * enum 변환보다 뒤에 실행된다.
     */
    private static final ColumnTypeSpec[] COLUMNS_TO_NULLABLE = {
            // 고양이 술래잡기 TIMEOUT 판단은 확신도가 없다
            new ColumnTypeSpec("cat_chase_judgment", "confidence", "VARCHAR(20) NULL"),
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
            for (ColumnTypeSpec spec : ENUM_COLUMNS_TO_VARCHAR) {
                convertEnumColumn(connection, spec);
            }
            for (ColumnTypeSpec spec : COLUMNS_TO_NULLABLE) {
                makeColumnNullable(connection, spec);
            }
        } catch (Exception e) {
            log.error("[SchemaPatch] 스키마 점검 중 DB 연결에 실패했습니다.", e);
        }
    }

    private void ensureTable(Connection connection, TableSpec spec) {
        try {
            if (!tableExists(connection, spec.table())) {
                execute(connection, spec.createSql());
                log.warn("[SchemaPatch] 누락된 테이블 {}을(를) 생성했습니다.", spec.table());
            }
        } catch (Exception e) {
            log.error("[SchemaPatch] {} 테이블 확인/생성 중 오류가 발생했습니다.", spec.table(), e);
            return;
        }
        for (IndexSpec index : spec.indexes()) {
            ensureIndex(connection, spec.table(), index);
        }
    }

    private void ensureIndex(Connection connection, String table, IndexSpec index) {
        try {
            if (indexExists(connection, table, index.name())) {
                return;
            }
            execute(connection, "CREATE INDEX " + index.name() + " ON " + table + " (" + index.columns() + ")");
            log.warn("[SchemaPatch] {} 테이블에 누락된 인덱스 {}을(를) 생성했습니다.", table, index.name());
        } catch (Exception e) {
            log.error("[SchemaPatch] {}.{} 인덱스 확인/생성 중 오류가 발생했습니다.", table, index.name(), e);
        }
    }

    private boolean indexExists(Connection connection, String table, String indexName) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        // DB 마다 식별자 대소문자 처리가 달라 소문자·대문자 모두 확인한다.
        for (String name : List.of(table, table.toUpperCase())) {
            try (ResultSet resultSet = metaData.getIndexInfo(connection.getCatalog(), null, name, false, false)) {
                while (resultSet.next()) {
                    if (indexName.equalsIgnoreCase(resultSet.getString("INDEX_NAME"))) {
                        return true;
                    }
                }
            }
        }
        return false;
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
            if (!columnExists(connection, spec.table(), spec.column())) {
                execute(connection, "ALTER TABLE " + spec.table() + " ADD COLUMN " + spec.column() + " " + spec.definition());
                log.warn("[SchemaPatch] {} 테이블에 누락된 컬럼 {}을(를) 추가했습니다.", spec.table(), spec.column());
            }
        } catch (Exception e) {
            log.error("[SchemaPatch] {}.{} 컬럼 확인/추가 중 오류가 발생했습니다.", spec.table(), spec.column(), e);
            return;
        }
        if (spec.backfillSql() != null) {
            backfill(connection, spec);
        }
    }

    private void convertEnumColumn(Connection connection, ColumnTypeSpec spec) {
        try {
            String typeName = columnTypeName(connection, spec.table(), spec.column());
            // MySQL 은 ENUM, H2 는 ENUM(...) 형태로 알려준다
            if (typeName == null || !typeName.toUpperCase().startsWith("ENUM")) {
                return;
            }
            execute(connection, "ALTER TABLE " + spec.table() + " MODIFY COLUMN " + spec.column() + " " + spec.definition());
            log.warn("[SchemaPatch] {}.{} 컬럼을 enum 에서 {} 로 변경했습니다.", spec.table(), spec.column(), spec.definition());
        } catch (Exception e) {
            log.error("[SchemaPatch] {}.{} 컬럼 타입 확인/변경 중 오류가 발생했습니다.", spec.table(), spec.column(), e);
        }
    }

    private void makeColumnNullable(Connection connection, ColumnTypeSpec spec) {
        try {
            if (!"NO".equalsIgnoreCase(columnNullable(connection, spec.table(), spec.column()))) {
                return;
            }
            execute(connection, "ALTER TABLE " + spec.table() + " MODIFY COLUMN " + spec.column() + " " + spec.definition());
            log.warn("[SchemaPatch] {}.{} 컬럼을 NULL 허용({})으로 변경했습니다.", spec.table(), spec.column(), spec.definition());
        } catch (Exception e) {
            log.error("[SchemaPatch] {}.{} 컬럼 NULL 허용 확인/변경 중 오류가 발생했습니다.", spec.table(), spec.column(), e);
        }
    }

    /** IS_NULLABLE 값(YES/NO). 컬럼이 없으면 null */
    private String columnNullable(Connection connection, String table, String column) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        // DB 마다 식별자 대소문자 처리가 달라 소문자·대문자 모두 확인한다.
        for (String[] name : List.of(new String[]{table, column}, new String[]{table.toUpperCase(), column.toUpperCase()})) {
            try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, name[0], name[1])) {
                if (resultSet.next()) {
                    return resultSet.getString("IS_NULLABLE");
                }
            }
        }
        return null;
    }

    /** 컬럼이 없으면 null */
    private String columnTypeName(Connection connection, String table, String column) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        // DB 마다 식별자 대소문자 처리가 달라 소문자·대문자 모두 확인한다.
        for (String[] name : List.of(new String[]{table, column}, new String[]{table.toUpperCase(), column.toUpperCase()})) {
            try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, name[0], name[1])) {
                if (resultSet.next()) {
                    return resultSet.getString("TYPE_NAME");
                }
            }
        }
        return null;
    }

    private void backfill(Connection connection, ColumnSpec spec) {
        try (Statement statement = connection.createStatement()) {
            int updated = statement.executeUpdate(spec.backfillSql());
            if (updated > 0) {
                log.warn("[SchemaPatch] {}.{} 컬럼 백필 완료 ({}건).", spec.table(), spec.column(), updated);
            }
        } catch (Exception e) {
            log.error("[SchemaPatch] {}.{} 컬럼 백필 중 오류가 발생했습니다. 다음 실행에서 다시 시도합니다.",
                    spec.table(), spec.column(), e);
        }
    }

    private static void execute(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
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

    private record TableSpec(String table, String createSql, List<IndexSpec> indexes) {
        TableSpec(String table, String createSql, IndexSpec... indexes) {
            this(table, createSql, List.of(indexes));
        }
    }

    private record IndexSpec(String name, String columns) {
    }

    private record ColumnTypeSpec(String table, String column, String definition) {
    }

    private record ColumnSpec(String table, String column, String definition, String backfillSql) {
        ColumnSpec(String table, String column, String definition) {
            this(table, column, definition, null);
        }
    }
}