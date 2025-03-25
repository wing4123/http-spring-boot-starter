package com.wing.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Http {
    
    private OkHttpClient httpClient;
    
    private ObjectMapper objectMapper;
    
    public Http(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }
    
    public Http() {
        this.httpClient = new OkHttpClient.Builder().build();
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }
    
    public ObjectMapper objectMapper() {
        return this.objectMapper;
    }
    
    /**
     * 创建get请求
     * @param url
     * @return
     */
    public Request get(String url) {
        Map<String, String> headers = new HashMap<>();
        return new Request(url, "GET", headers, null);
    }
    
    public Request delete(String url) {
        Map<String, String> headers = new HashMap<>();
        return new Request(url, "DELETE", headers, null);
    }
    
    /**
     * 创建post请求发送json数据
     * @param url
     * @param jsonParams
     * @return
     */
    public Request post(String url, String jsonParams) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new Request(url, "POST", headers, RequestBody.create(jsonParams, MediaType.parse("application/json")));
    }
    
    public Request put(String url, String jsonParams) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new Request(url, "PUT", headers, RequestBody.create(jsonParams, MediaType.parse("application/json")));
    }
    
    /**
     * 创建form表单请求
     * @param url
     * @param params
     * @return
     */
    public FormRequest form(String url, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        
        return new FormRequest(url, "POST", headers, params);
    }
    
    /**
     * 创建文件上传请求
     * @param url
     * @return
     */
    public UploadRequest upload(String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "multipart/form-data");
        return new UploadRequest(url, "POST", headers);
    }
    
    public Request request(String url, String method, Map<String, String> headers, RequestBody body) {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        return new Request(url, method, headers, body);
    }
    
    public class Request {
        private HttpUrl url;
        private String method;
        private Map<String, String> headers;
        private RequestBody body;
        
        private Request() {
            
        }
        
        private Request(String url, String method, Map<String, String> headers, RequestBody body) {
            this.url = HttpUrl.parse(url);
            this.method = method;
            this.headers = headers;
            this.body = body;
        }
        
        /**
         * 添加请求头
         * @param key
         * @param value
         * @return
         */
        public Request addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }
        
        /**
         * 添加多个请求头
         * @param headers
         * @return
         */
        public Request addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }
        
        private okhttp3.Request buildRequest() {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(this.url)
                    .method(this.method.toUpperCase(), this.body)
                    .headers(Headers.of(headers))
                    .build();
            return request;
        }
        
        /**
         * 发送请求
         * @return
         * @throws IOException
         * @throws InterruptedException
         */
        public Response send() throws IOException, InterruptedException {
            okhttp3.Request request = buildRequest();
            okhttp3.Response response = httpClient.newCall(request).execute();
            
            return new Response(response);
        }
        
        /**
         * 异步请求
         * @return
         */
        public CompletableFuture<okhttp3.Response> sendAsync() {
            okhttp3.Request request = buildRequest();
            CompletableFuture<okhttp3.Response> future = new CompletableFuture<>();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call arg0, IOException e) {
                    future.completeExceptionally(e); // 异常情况
                }
                @Override
                public void onResponse(Call arg0, okhttp3.Response response) throws IOException {
                    future.complete(response); // 正常返回
                }
            });
            
            return future;
        }
        
        public void sendCallBack(Callback callback) {
            okhttp3.Request request = buildRequest();
            httpClient.newCall(request).enqueue(callback);
        }
    }
    
    public class FormRequest extends Request {
        private FormBody.Builder bodyBuilder = new FormBody.Builder();
        
        public FormRequest(String url, String method, Map<String, String> headers, Map<String, String> params) {
            super(url, method, headers, null);
            params.entrySet().forEach(item -> {
                this.bodyBuilder.addEncoded(item.getKey(), item.getValue());
            });
        }
        
        @Override
        public Response send() throws IOException, InterruptedException {
            super.body = this.bodyBuilder.build();
            return super.send();
        }
        
        public FormRequest addParam(String key, String value) {
            bodyBuilder.addEncoded(key, value);
            return this;
        }
        
        @Override
        public FormRequest addHeader(String key, String value) {
            super.addHeader(key, value);
            return this;
        }
        
        @Override
        public FormRequest addHeaders(Map<String, String> headers) {
            super.addHeaders(headers);
            return this;
        }
    }
    
    public class UploadRequest extends Request {
        private MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        
        public UploadRequest(String url, String method, Map<String, String> headers) {
            super(url, method, headers, null);
        }
        
        @Override
        public Response send() throws IOException, InterruptedException {
            super.body = this.bodyBuilder.build();
            return super.send();
        }
        
        public UploadRequest addParam(String key, String value) {
            bodyBuilder.addFormDataPart(key, value);
            return this;
        }
        
        public UploadRequest addFile(String key, String filename, byte[] file) {
            bodyBuilder.addFormDataPart(key, filename, RequestBody.create(file, MediaType.parse("application/octet-stream")));
            return this;
        }
        
        public UploadRequest addFile(String key, File file) {
            bodyBuilder.addFormDataPart(key, file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream")));
            return this;
        }
        
        @Override
        public UploadRequest addHeader(String key, String value) {
            super.addHeader(key, value);
            return this;
        }
        
        @Override
        public UploadRequest addHeaders(Map<String, String> headers) {
            super.addHeaders(headers);
            return this;
        }
    }
    
    public class Response {
        public okhttp3.Response response;
        
        private Response(okhttp3.Response response) {
            this.response = response;
        }
        
        /**
         * 获取响应状态码
         * @return
         */
        public int statusCode() {
            return this.response.code();
        }
        
        /**
         * 获取响应头
         * @return
         */
        public Headers headers() {
            return this.response.headers();
        }
        
        /**
         * 获取响应体
         * @return
         * @throws IOException 
         */
        public byte[] bytes() throws IOException {
            return this.response.body().bytes();
        }
        
        /**
         * 获取字符串类型的响应体
         * @return
         * @throws IOException 
         */
        public String string() throws IOException {
            return this.response.body().string();
        }
        
        /**
         * 获取json对象响应体
         * @return
         * @throws IOException
         */
        public JsonNode json() throws IOException {
            return objectMapper.readTree(this.string());
        }
        
        /**
         * 获取自定义对象类型的响应体
         * @param <T>
         * @param clazz
         * @return
         * @throws IOException
         */
        public <T> T object(Class<T> clazz) throws IOException {
            return objectMapper.readValue(this.bytes(), clazz);
        }
    }
}
