package com.wing.http;

import java.net.http.HttpClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfiguration
public class HttpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Http http(HttpClient httpClient, ObjectMapper objectMapper){
        return new Http(httpClient, objectMapper);
    }
    
    @Bean
    @ConditionalOnMissingBean
    HttpClient httpClient() {
    	return HttpClient.newBuilder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingClass("org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration")
    ObjectMapper objectMapper() {
    	return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }
}
