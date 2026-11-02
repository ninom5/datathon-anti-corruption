package com.datathon.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/** Starts the app against a throwaway PostgreSQL 17: proves V1__init.sql applies and every entity matches it. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
