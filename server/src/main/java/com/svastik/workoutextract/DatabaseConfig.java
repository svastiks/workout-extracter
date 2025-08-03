package com.svastik.workoutextract;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${DB_PORT:5432}")
    private int dbPort;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgresql://")) {
            try {
                // Parse the DATABASE_URL from Render format: postgresql://user:password@host:port/database
                URI uri = new URI(databaseUrl);
                
                // Extract components
                String[] userInfo = uri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo[1];
                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : dbPort; // Use environment variable if no port specified
                String database = uri.getPath().substring(1); // Remove leading slash
                
                // Construct JDBC URL
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                dataSource.setDriverClassName("org.postgresql.Driver");
                
                // Connection pool settings
                dataSource.setMaximumPoolSize(10);
                dataSource.setMinimumIdle(5);
                dataSource.setConnectionTimeout(30000);
                dataSource.setIdleTimeout(600000);
                dataSource.setMaxLifetime(1800000);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
            }
        } else {
            // Fallback to default configuration for local development
            dataSource.setJdbcUrl("jdbc:postgresql://localhost:5433/workout_extract_db");
            dataSource.setUsername("postgres");
            dataSource.setPassword("postgres");
            dataSource.setDriverClassName("org.postgresql.Driver");
            
            // Connection pool settings for local
            dataSource.setMaximumPoolSize(5);
            dataSource.setMinimumIdle(2);
            dataSource.setConnectionTimeout(30000);
            dataSource.setIdleTimeout(600000);
            dataSource.setMaxLifetime(1800000);
        }
        
        return dataSource;
    }
} 