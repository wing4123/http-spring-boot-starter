package com.wing.http;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

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
    	return HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .executor(Executors.newWorkStealingPool())
                .followRedirects(Redirect.ALWAYS)
                .build();
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
