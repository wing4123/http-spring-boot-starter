package com.wing.http;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Http {
    
    private HttpClient httpClient;
    
    private ObjectMapper objectMapper;
    
    public Http(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }
    
    public Http() {
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .executor(Executors.newWorkStealingPool())
                .followRedirects(Redirect.ALWAYS)
                .build();
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }
    
    public ObjectMapper objectMapper() {
        return this.objectMapper;
    }
    
    /**
     * 创建请求
     * @param url
     * @param method
     * @param headers
     * @param body
     * @return
     */
    public Request request(String url, String method, Map<String, String> headers, byte[] body) {
        return new Request(url, method, headers, body);
    }
    
    /**
     * 创建get请求
     * @param url
     * @return
     */
    public Request get(String url) {
        return this.request(url, "get", null, null);
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
        return this.request(url, "post", headers, jsonParams.getBytes());
    }
    
    /**
     * 创建form表单请求
     * @param url
     * @param params
     * @return
     */
    public Request form(String url, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        byte[] body = params.entrySet().stream().map(item -> item.getKey() + "=" + URLEncoder.encode(item.getValue(), Charset.defaultCharset())).collect(Collectors.joining("&")).getBytes();
        return this.request(url, "post", headers, body);
    }
    
    /**
     * 创建文件上传请求
     * @param url
     * @return
     */
    public Request upload(String url, UploadParams params) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "multipart/form-data; boundary=----boundary");
        return this.request(url, "post", headers, params.getBody());
    }
    
    public class Request {
        private String url;
        private String method;
        private Map<String, String> headers = new HashMap<>();
        private byte[] body;
        
        private Request(String url, String method, Map<String, String> headers, byte[] body) {
            this.url = url;
            this.method = method;
            this.headers.put("author", "wing4123");
            if (headers != null && !headers.isEmpty()) {
                this.headers.putAll(headers);
            }
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
        
        private HttpRequest buildRequest() {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.url))
                    .method(method.toUpperCase(), this.body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(this.body))
                    .headers(headers.entrySet().stream().flatMap(item -> Stream.of(item.getKey(), item.getValue())).toArray(String[]::new))
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
            HttpRequest request = buildRequest();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            
            return new Response(response);
        }
        
        /**
         * 异步请求
         * @return
         */
        public CompletableFuture<Response> sendAsync() {
            HttpRequest request = buildRequest();
            CompletableFuture<HttpResponse<byte[]>> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
            
            return response.thenApplyAsync(Response::new);
        }
    }
    
    public static class UploadParams {
        private byte[] body = "------boundary--".getBytes();
        private final byte[] boundary = "\r\n------boundary--".getBytes();
        
        public byte[] getBody() {
            return this.body;
        }
        
        /**
         * 添加文件参数
         * @param name 参数名称
         * @param fileName 文件名称
         * @param file 文件字节数组
         * @return
         */
        public UploadParams addFile(String name, String fileName, byte[] file) {
            this.body[this.body.length - 2] = "\r".getBytes()[0];
            this.body[this.body.length - 1] = "\n".getBytes()[0];
            
            byte[] contentDisposition = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n", name, fileName).getBytes();
            byte[] contentType = "Content-Type: application/octet-stream\r\n\r\n".getBytes();
            
            byte[] joinedArray = new byte[this.body.length + contentDisposition.length + contentType.length + file.length + boundary.length];
            System.arraycopy(this.body, 0, joinedArray, 0, this.body.length);
            System.arraycopy(contentDisposition, 0, joinedArray, this.body.length, contentDisposition.length);
            System.arraycopy(contentType, 0, joinedArray, this.body.length + contentDisposition.length, contentType.length);
            System.arraycopy(file, 0, joinedArray, this.body.length + contentDisposition.length + contentType.length, file.length);
            System.arraycopy(boundary, 0, joinedArray, this.body.length + contentDisposition.length + contentType.length + file.length, boundary.length);
            this.body = joinedArray;
            
            return this;
        }
        
        /**
         * 添加文件参数
         * @param name 参数名称
         * @param filePath 文件目录
         * @return
         * @throws IOException
         */
        public UploadParams addFile(String name, String filePath) throws IOException {
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            byte[] file = Files.readAllBytes(path);
            
            return this.addFile(name, fileName, file);
        }
        
        /**
         * 添加参数
         * @param key
         * @param value
         * @return
         */
        public UploadParams addParam(String key, String value) {
            this.body[this.body.length - 2] = "\r".getBytes()[0];
            this.body[this.body.length - 1] = "\n".getBytes()[0];
            
            byte[] contentDisposition = String.format("Content-Disposition: form-data; name=\"%s\"\r\n\r\n", key).getBytes();
            
            byte[] joinedArray = new byte[this.body.length + contentDisposition.length + value.getBytes().length + boundary.length];
            System.arraycopy(this.body, 0, joinedArray, 0, this.body.length);
            System.arraycopy(contentDisposition, 0, joinedArray, this.body.length, contentDisposition.length);
            System.arraycopy(value.getBytes(), 0, joinedArray, this.body.length + contentDisposition.length, value.getBytes().length);
            System.arraycopy(boundary, 0, joinedArray, this.body.length + contentDisposition.length + value.getBytes().length, boundary.length);
            this.body = joinedArray;
            
            return this;
        }
        
        /**
         * 添加多个参数
         * @param params
         * @return
         */
        public UploadParams addParams(Map<String, String> params) {
            for(Entry<String, String> item : params.entrySet()) {
                this.addParam(item.getKey(), item.getValue());
            }
            
            return this;
        }
    }
    
    public class Response {
        private HttpResponse<byte[]> response;
        
        private Response(HttpResponse<byte[]> response) {
            if(response.statusCode() != 200) {
                throw new RuntimeException("http请求失败，状态码：" + response.statusCode());
            }
            this.response = response;
        }
        
        /**
         * 获取响应状态码
         * @return
         */
        public int statusCode() {
            return this.response.statusCode();
        }
        
        /**
         * 获取响应头
         * @return
         */
        public HttpHeaders headers() {
            return this.response.headers();
        }
        
        /**
         * 获取响应体
         * @return
         */
        public byte[] bytes() {
            return this.response.body();
        }
        
        /**
         * 获取字符串类型的响应体
         * @return
         */
        public String string() {
            return new String(this.bytes());
        }
        
        /**
         * 获取json对象响应体
         * @return
         * @throws IOException
         */
        public JsonNode json() throws IOException {
            return objectMapper.readTree(this.bytes());
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
