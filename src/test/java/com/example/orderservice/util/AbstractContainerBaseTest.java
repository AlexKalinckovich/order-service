package com.example.orderservice.util;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.testcontainers.containers.KafkaContainer;

@Testcontainers
public abstract class AbstractContainerBaseTest {

    protected static final String MYSQL_IMAGE = "mysql:8.0";
    protected static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.6.1";
    protected static final String MYSQL_DATABASE_NAME = "orderdb";
    protected static final String MYSQL_USERNAME = "root";
    protected static final String MYSQL_PASSWORD = "orderpassword";

    @Container
    @ServiceConnection
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
            .withDatabaseName(MYSQL_DATABASE_NAME)
            .withUsername(MYSQL_USERNAME)
            .withPassword(MYSQL_PASSWORD)
            .withReuse(true);

    @Container
    @ServiceConnection
    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
            .withReuse(true);

    static {
        mysql.start();
        kafka.start();
    }
}