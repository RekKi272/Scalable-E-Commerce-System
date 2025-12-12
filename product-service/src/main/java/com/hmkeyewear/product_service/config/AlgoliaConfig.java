package com.hmkeyewear.product_service.config;

import com.algolia.api.SearchClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgoliaConfig {

    @Value("${algolia.app-id}")
    private String appId;

    @Value("${algolia.api-key}")
    private String apiKey;

    @Value("${algolia.index-name}")
    private String indexName;

    @Value("${algolia.api-write-key}")
    private String apiWriteKey;

    @Bean
    public SearchClient algoliaClient() {
        return new SearchClient(appId, apiWriteKey);
    }

}
