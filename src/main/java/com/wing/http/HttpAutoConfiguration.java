package com.wing.http;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import okhttp3.OkHttpClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * http自动配置
 */
@AutoConfiguration
@ConditionalOnMissingBean(Http.class)
public class HttpAutoConfiguration {

    @Bean
    Http http(OkHttpClient httpClient, ObjectMapper objectMapper) {
        return new Http(httpClient, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(OkHttpClient.class)
    OkHttpClient httpClient() {
        return new OkHttpClient();
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }
}
