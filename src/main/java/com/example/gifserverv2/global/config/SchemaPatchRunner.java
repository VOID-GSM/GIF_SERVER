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

/**
 * Cloudtype 등 배포 환경에서 hibernate ddl-auto 설정만으로는 엔티티에 새로
 * 추가된 컬럼이 기존 DB에 반영되지 않는 경우가 있다 (예: DB_DDL이
 * update가 아니거나, 앞선 ALTER 실패로 스키마 동기화가 누락되는 경우).
 * 그 결과 컬럼이 없는 채로 서버가 기동되어, 해당 컬럼을 조회하는 요청에서만
 * "Unknown column" 500 에러가 발생한다 (예: form.created_by_user_id 누락으로
 * 인한 양식 목록/상세 조회(GET /api/form, GET /api/form/{id}) 실패를
 * 실제 배포 서버에서 재현/확인함).
 * <p>
 * 기동 시점에 필수 컬럼 존재 여부를 직접 확인하고, 없으면 추가해 위 문제를
 * ddl-auto 설정과 무관하게 방지한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaPatchRunner implements ApplicationRunner {

    private final DataSource dataSource;

    private static final ColumnSpec[] REQUIRED_COLUMNS = {
            new ColumnSpec("form", "created_by_user_id", "BIGINT NULL"),
            new ColumnSpec("form_field", "required", "TINYINT(1) NOT NULL DEFAULT 0"),
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
