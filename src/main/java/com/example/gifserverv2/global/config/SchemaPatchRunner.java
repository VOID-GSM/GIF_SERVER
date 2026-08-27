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

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaPatchRunner implements ApplicationRunner {

    private final DataSource dataSource;

    private static final ColumnSpec[] REQUIRED_COLUMNS = {
            new ColumnSpec("form", "created_by_user_id", "BIGINT NULL"),
            new ColumnSpec("form_field", "required", "TINYINT(1) NOT NULL DEFAULT 0"),
            new ColumnSpec("form_field", "allowed_extensions", "VARCHAR(200) NULL"),
    };

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            for (ColumnSpec spec : REQUIRED_COLUMNS) {
                ensureColumn(connection, spec);
            }
        } catch (Exception e) {
            log.error("[SchemaPatch] 스키마 점검 중 DB 연결에 실패했습니다.", e);
        }
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
        } catch (Exception e) {
            log.error("[SchemaPatch] {}.{} 컬럼 확인/추가 중 오류가 발생했습니다.", spec.table(), spec.column(), e);
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, table, column)) {
            return resultSet.next();
        }
    }

    private record ColumnSpec(String table, String column, String definition) {
    }
}
