package com.talentx.hrms.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DatabaseConfig to verify database connection and configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSourceConfiguration() {
        assertNotNull(dataSource, "DataSource should be configured and not null");
    }

    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should be established");
            assertFalse(connection.isClosed(), "Connection should be open");
            
            // Test basic database operation
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1");
            assertTrue(resultSet.next(), "Should be able to execute basic query");
            assertEquals(1, resultSet.getInt(1), "Query should return expected result");
        }
    }

    @Test
    void testHikariConnectionPoolProperties() {
        // Verify that we're using HikariCP
        assertTrue(dataSource.getClass().getName().contains("Hikari"), 
                   "Should be using HikariCP connection pool");
    }
}

