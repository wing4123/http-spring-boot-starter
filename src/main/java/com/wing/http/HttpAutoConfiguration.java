package com.wing.http;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import okhttp3.OkHttpClient;

@AutoConfiguration
public class HttpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Http http(OkHttpClient httpClient, ObjectMapper objectMapper){
        return new Http(httpClient, objectMapper);
    }
    
    @Bean
    @ConditionalOnMissingBean
    OkHttpClient httpClient() {
    	return new OkHttpClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass("org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration")
    ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }
}
