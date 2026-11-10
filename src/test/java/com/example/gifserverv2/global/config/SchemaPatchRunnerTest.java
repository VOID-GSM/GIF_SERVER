package com.example.gifserverv2.global.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 마이페이지 테이블이 ddl-auto 없이도 만들어지는지 MySQL 호환 모드 H2 로 확인한다.
 */
class SchemaPatchRunnerTest {

    private JdbcTemplate jdbc;
    private SchemaPatchRunner runner;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE users (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255) NOT NULL)");
        runner = new SchemaPatchRunner(dataSource);
    }

    @Test
    void 마이페이지_테이블과_컬럼이_없으면_만든다() {
        runner.run(null);

        jdbc.update("INSERT INTO users (id, email, major, profile_image_url) VALUES (1, 'a@gsm.hs.kr', '백엔드', NULL)");
        jdbc.update("INSERT INTO user_tech_stack (user_id, sort_order, name) VALUES (1, 0, 'Spring')");
        jdbc.update("INSERT INTO user_link (user_id, title, url) VALUES (1, '블로그', 'https://blog.example.com')");

        assertThat(jdbc.queryForObject("SELECT name FROM user_tech_stack WHERE user_id = 1", String.class)).isEqualTo("Spring");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_link", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT major FROM users WHERE id = 1", String.class)).isEqualTo("백엔드");
    }

    @Test
    void 학생_관리_테이블이_없으면_만든다() {
        runner.run(null);

        jdbc.update("INSERT INTO student_roster (student_number, name, grade, class_no, student_no) VALUES ('1101', '김학생', 1, 1, 1)");
        jdbc.update("INSERT INTO team_join_request (project_id, user_id, requested_by, status, created_at) "
                + "VALUES (1, 2, 3, 'PENDING', CURRENT_TIMESTAMP)");

        assertThat(jdbc.queryForObject("SELECT name FROM student_roster WHERE student_number = '1101'", String.class)).isEqualTo("김학생");
        assertThat(jdbc.queryForObject("SELECT status FROM team_join_request", String.class)).isEqualTo("PENDING");
    }

    @Test
    void 여러_번_실행해도_기존_데이터를_유지한다() {
        runner.run(null);
        jdbc.update("INSERT INTO users (id, email) VALUES (1, 'a@gsm.hs.kr')");
        jdbc.update("INSERT INTO user_link (user_id, url) VALUES (1, 'https://blog.example.com')");

        assertThatCode(() -> runner.run(null)).doesNotThrowAnyException();

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_link", Integer.class)).isEqualTo(1);
    }

    @Test
    void 테이블이_이미_있어도_빠진_인덱스는_만든다() {
        // ddl-auto 로 테이블만 만들어졌거나, 이전 실행에서 인덱스 생성만 실패한 상황
        jdbc.execute("CREATE TABLE team_join_request (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "project_id BIGINT NOT NULL, user_id BIGINT NOT NULL, status VARCHAR(20) NOT NULL)");
        jdbc.execute("CREATE INDEX idx_team_join_request_user_status ON team_join_request (user_id, status)");

        runner.run(null);

        assertThat(indexNames("TEAM_JOIN_REQUEST"))
                .contains("IDX_TEAM_JOIN_REQUEST_USER_STATUS", "IDX_TEAM_JOIN_REQUEST_PROJECT_STATUS");
        assertThatCode(() -> runner.run(null)).doesNotThrowAnyException();
    }

    @Test
    void 고양이_술래잡기_판단_컬럼을_VARCHAR로_바꾸고_확신도를_NULL_허용으로_바꾼다() {
        // Hibernate 6 가 MySQL 에 만든 enum NOT NULL 컬럼
        jdbc.execute("CREATE TABLE cat_chase_judgment (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "judgment ENUM('FOUND','MISSED') NOT NULL, "
                + "confidence ENUM('VERY_CONFIDENT','CONFIDENT','SOMEWHAT_UNSURE','UNSURE') NOT NULL)");

        runner.run(null);

        jdbc.update("INSERT INTO cat_chase_judgment (judgment, confidence) VALUES ('TIMEOUT', NULL)");
        assertThat(jdbc.queryForObject("SELECT judgment FROM cat_chase_judgment", String.class)).isEqualTo("TIMEOUT");
        assertThatCode(() -> runner.run(null)).doesNotThrowAnyException();
    }

    @Test
    void VARCHAR_NOT_NULL인_확신도_컬럼도_NULL_허용으로_바꾼다() {
        jdbc.execute("CREATE TABLE cat_chase_judgment (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "judgment VARCHAR(20) NOT NULL, confidence VARCHAR(20) NOT NULL)");

        runner.run(null);

        jdbc.update("INSERT INTO cat_chase_judgment (judgment, confidence) VALUES ('TIMEOUT', NULL)");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM cat_chase_judgment WHERE confidence IS NULL", Integer.class))
                .isEqualTo(1);
    }

    private java.util.List<String> indexNames(String table) {
        return jdbc.queryForList(
                "SELECT UPPER(INDEX_NAME) FROM INFORMATION_SCHEMA.INDEXES WHERE UPPER(TABLE_NAME) = ?", String.class, table);
    }
}
