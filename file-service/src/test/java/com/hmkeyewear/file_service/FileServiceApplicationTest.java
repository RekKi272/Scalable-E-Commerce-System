package com.hmkeyewear.file_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hmkeyewear.file_service.service.FileService;

import static org.mockito.Mockito.mock;

@SpringBootTest(classes = {
        FileServiceApplication.class,
        FileServiceApplicationTest.MockConfig.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FileServiceApplicationTest {

    @Test
    void contextLoads() {
    }

    @Configuration
    static class MockConfig {

        @Bean
        FileService fileService() {
            return mock(FileService.class);
        }
    }
}
