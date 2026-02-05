package com.hmkeyewear.cart_service.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.cloud.Timestamp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module timestampModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Timestamp.class, new TimestampDeserializer());
        return module;
    }
}
