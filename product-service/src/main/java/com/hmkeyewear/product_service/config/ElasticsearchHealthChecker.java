package com.hmkeyewear.product_service.config;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchHealthChecker {

    private final RestClient restClient;

    public boolean isReady() {
        try {
            Request request = new Request("GET", "/");
            restClient.performRequest(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
