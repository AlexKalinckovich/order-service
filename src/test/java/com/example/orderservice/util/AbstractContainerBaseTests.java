package com.example.orderservice.util;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.testcontainers.containers.MySQLContainer.MYSQL_PORT;

@Testcontainers
public abstract class AbstractContainerBaseTests implements AutoCloseable{

    protected static final String DRIVER_PATH = "com.mysql.cj.jdbc.Driver";
    protected static final String MYSQL_IMAGE = "mysql:8.0";
    protected static final String MYSQL_DATABASE_NAME = "orderdb";
    protected static final String MYSQL_USERNAME = "root";
    protected static final String MYSQL_PASSWORD = "orderpassword";
    protected static final String RESOURCE_PATH = "db/changelog";
    protected static final String INITIAL_SCHEMA = RESOURCE_PATH + "/v1-initial-schema.xml";

    @Container
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
            .withDatabaseName(MYSQL_DATABASE_NAME)
            .withUsername(MYSQL_USERNAME)
            .withPassword(MYSQL_PASSWORD)
            .withExposedPorts(MYSQL_PORT)
            .withReuse(true)
            .withCopyToContainer(
                    MountableFile.forClasspathResource(RESOURCE_PATH),
                    INITIAL_SCHEMA
            );


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> DRIVER_PATH);
    }

    @Override
    public void close() {
        mysql.close();
    }
}