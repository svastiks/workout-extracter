package com.svastik.workoutextract;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    void testDatabaseUrlParsing() throws Exception {
        // Test URL without port (like Render provides)
        String databaseUrl = "postgresql://workout_user:kEskyHjnbAXN2dejbtMqox1UOoqmtMfI@dpg-d277soeuk2gs73dhjvug-a/workoutextractdb";
        
        URI uri = new URI(databaseUrl);

        String[] userInfo = uri.getUserInfo().split(":");
        String username = userInfo[0];
        String password = userInfo[1];
        String host = uri.getHost();
        int defaultPort = 5432;
        int port = uri.getPort() != -1 ? uri.getPort() : defaultPort;
        String database = uri.getPath().substring(1); // Remove leading slash
        
        // Construct JDBC URL
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        
        assertEquals("workout_user", username);
        assertEquals("kEskyHjnbAXN2dejbtMqox1UOoqmtMfI", password);
        assertEquals("dpg-d277soeuk2gs73dhjvug-a", host);
        assertEquals(5432, port); // Should use environment variable default
        assertEquals("workoutextractdb", database);
        assertEquals("jdbc:postgresql://dpg-d277soeuk2gs73dhjvug-a:5432/workoutextractdb", jdbcUrl);
    }
} 