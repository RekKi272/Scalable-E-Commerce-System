package com.hmkeyewear.product_service.config;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ElasticsearchHealthChecker {

    private final Optional<RestClient> restClient;

    public boolean isReady() {
        if (restClient.isEmpty()) {
            return false;
        }
        try {
            Request request = new Request("GET", "/");
            restClient.get().performRequest(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
