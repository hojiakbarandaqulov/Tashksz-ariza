package org.example.toshkszariza.config;

import org.example.toshkszariza.domain.ApplicationRegion;
import org.example.toshkszariza.domain.ConversationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Hibernate mavjud PostgreSQL enum CHECK cheklovlarini yangilamaydi.
 * Shu migratsiya yangi Java enum qiymatlarini bot ishga tushishidan oldin bazaga moslaydi.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PostgresEnumConstraintMigration implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(PostgresEnumConstraintMigration.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public PostgresEnumConstraintMigration(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        String databaseName;
        try (Connection connection = dataSource.getConnection()) {
            databaseName = connection.getMetaData().getDatabaseProductName();
        }
        if (!databaseName.toLowerCase(java.util.Locale.ROOT).contains("postgresql")) {
            return;
        }

        String regions = enumValues(ApplicationRegion.values());
        replaceCheck("service_applications", "region", "service_applications_region_check", regions, true);
        replaceCheck("user_conversations", "region", "user_conversations_region_check", regions, true);

        String steps = enumValues(ConversationStep.values());
        replaceCheck("user_conversations", "step", "user_conversations_step_check", steps, false);
        log.info("PostgreSQL hudud va suhbat bosqichi cheklovlari yangilandi");
    }

    private void replaceCheck(
            String table,
            String column,
            String constraint,
            String allowedValues,
            boolean nullable
    ) {
        jdbcTemplate.execute("alter table " + table + " drop constraint if exists " + constraint);
        String nullablePart = nullable ? column + " is null or " : "";
        jdbcTemplate.execute("alter table " + table + " add constraint " + constraint
                + " check (" + nullablePart + column + " in (" + allowedValues + "))");
    }

    private String enumValues(Enum<?>[] values) {
        return Arrays.stream(values)
                .map(value -> "'" + value.name().replace("'", "''") + "'")
                .collect(Collectors.joining(", "));
    }
}
