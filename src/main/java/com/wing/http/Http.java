package com.wing.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * http请求工具类
 */
public class Http {

    private OkHttpClient httpClient;

    private ObjectMapper objectMapper;

    /**
     * 构造函数
     * 
     * @param httpClient   httpClient
     * @param objectMapper objectMapper
     */
    public Http(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 默认构造函数
     */
    public Http() {
        this.httpClient = new OkHttpClient.Builder().build();
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    /**
     * 获取objectMapper
     * 
     * @return ObjectMapper
     */
    public ObjectMapper objectMapper() {
        return this.objectMapper;
    }

    /**
     * 创建get请求
     * 
     * @param url url
     * @return Request
     */
    public Request get(String url) {
        Map<String, String> headers = new HashMap<>();
        return new Request(url, "GET", headers, null);
    }

    /**
     * 创建delete请求
     * 
     * @param url url
     * @return Request
     */
    public Request delete(String url) {
        Map<String, String> headers = new HashMap<>();
        return new Request(url, "DELETE", headers, null);
    }

    /**
     * 创建post请求发送json数据
     * 
     * @param url        url
     * @param jsonParams jsonParams
     * @return Request
     */
    public Request post(String url, String jsonParams) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new Request(url, "POST", headers, RequestBody.create(jsonParams, MediaType.parse("application/json")));
    }

    /**
     * 创建put请求发送json数据
     * 
     * @param url        url
     * @param jsonParams jsonParams
     * @return Request
     */
    public Request put(String url, String jsonParams) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new Request(url, "PUT", headers, RequestBody.create(jsonParams, MediaType.parse("application/json")));
    }

    /**
     * 创建form表单请求
     * 
     * @param url    url
     * @param params params
     * @return FormRequest
     */
    public FormRequest form(String url, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        return new FormRequest(url, "POST", headers, params);
    }

    /**
     * 创建文件上传请求
     * 
     * @param url url
     * @return UploadRequest
     */
    public UploadRequest upload(String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "multipart/form-data");
        return new UploadRequest(url, "POST", headers);
    }

    /**
     * 创建请求
     * 
     * @param url     url
     * @param method  method
     * @param headers headers
     * @param body    body
     * @return Request
     */
    public Request request(String url, String method, Map<String, String> headers, RequestBody body) {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        return new Request(url, method, headers, body);
    }

    /**
     * 请求
     */
    public class Request {
        private HttpUrl url;
        private String method;
        private Map<String, String> headers;
        private RequestBody body;

        /**
         * 默认构造函数
         */
        private Request() {

        }

        /**
         * 构造函数
         * 
         * @param url     url
         * @param method  method
         * @param headers headers
         * @param body    body
         */
        private Request(String url, String method, Map<String, String> headers, RequestBody body) {
            this.url = HttpUrl.parse(url);
            this.method = method;
            this.headers = headers;
            this.body = body;
        }

        /**
         * 添加请求头
         * 
         * @param key   key
         * @param value value
         * @return Request
         */
        public Request addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * 添加多个请求头
         * 
         * @param headers headers
         * @return Request
         */
        public Request addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * 构建okhttp请求
         * 
         * @return okhttp3.Request
         */
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
         * 
         * @return Response Response
         * @throws IOException          IOException
         * @throws InterruptedException InterruptedException
         */
        public Response send() throws IOException, InterruptedException {
            okhttp3.Request request = buildRequest();
            okhttp3.Response response = httpClient.newCall(request).execute();

            return new Response(response);
        }

        /**
         * 异步请求
         * 
         * @return CompletableFuture CompletableFuture
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

        /**
         * 异步请求
         * 
         * @param callback callback Callback
         */
        public void sendCallBack(Callback callback) {
            okhttp3.Request request = buildRequest();
            httpClient.newCall(request).enqueue(callback);
        }
    }

    /**
     * 表单请求
     */
    public class FormRequest extends Request {
        private FormBody.Builder bodyBuilder = new FormBody.Builder();

        /**
         * 构造函数
         * 
         * @param url     url
         * @param method  method
         * @param headers headers
         * @param params  params
         */
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

        @Override
        public CompletableFuture<okhttp3.Response> sendAsync() {
            super.body = this.bodyBuilder.build();
            return super.sendAsync();
        }

        /**
         * 添加表单参数
         * 
         * @param key   key
         * @param value value
         * @return FormRequest
         */
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

    /**
     * 文件上传请求
     */
    public class UploadRequest extends Request {
        private MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        /**
         * 构造函数
         * 
         * @param url     url
         * @param method  method
         * @param headers headers
         */
        public UploadRequest(String url, String method, Map<String, String> headers) {
            super(url, method, headers, null);
        }

        @Override
        public Response send() throws IOException, InterruptedException {
            super.body = this.bodyBuilder.build();
            return super.send();
        }

        @Override
        public CompletableFuture<okhttp3.Response> sendAsync() {
            super.body = this.bodyBuilder.build();
            return super.sendAsync();
        }

        /**
         * 添加表单参数
         * 
         * @param key   key
         * @param value value
         * @return UploadRequest
         */
        public UploadRequest addParam(String key, String value) {
            bodyBuilder.addFormDataPart(key, value);
            return this;
        }

        /**
         * 添加文件
         * 
         * @param key      key
         * @param filename filename
         * @param file     file
         * @return UploadRequest
         */
        public UploadRequest addFile(String key, String filename, byte[] file) {
            bodyBuilder.addFormDataPart(key, filename,
                    RequestBody.create(file, MediaType.parse("application/octet-stream")));
            return this;
        }

        /**
         * 添加文件
         * 
         * @param key  key
         * @param file file
         * @return UploadRequest
         */
        public UploadRequest addFile(String key, File file) {
            bodyBuilder.addFormDataPart(key, file.getName(),
                    RequestBody.create(file, MediaType.parse("application/octet-stream")));
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

    /**
     * 响应
     */
    public class Response {
        /**
         * 原始响应
         */
        public okhttp3.Response response;

        private Response(okhttp3.Response response) {
            this.response = response;
        }

        /**
         * 获取响应状态码
         * 
         * @return int int
         */
        public int statusCode() {
            return this.response.code();
        }

        /**
         * 获取响应头
         * 
         * @return Headers Headers
         */
        public Headers headers() {
            return this.response.headers();
        }

        /**
         * 获取响应体
         * 
         * @return byte[] byte[]
         * @throws IOException IOException
         */
        public byte[] bytes() throws IOException {
            return this.response.body().bytes();
        }

        /**
         * 获取字符串类型的响应体
         * 
         * @return String String
         * @throws IOException IOException
         */
        public String string() throws IOException {
            return this.response.body().string();
        }

        /**
         * 获取json对象响应体
         * 
         * @return JsonNode JsonNode
         * @throws IOException IOException
         */
        public JsonNode json() throws IOException {
            return objectMapper.readTree(this.string());
        }

        /**
         * 获取自定义对象类型的响应体
         * 
         * @param <T>   T
         * @param clazz clazz
         * @return T T
         * @throws IOException IOException
         */
        public <T> T object(Class<T> clazz) throws IOException {
            return objectMapper.readValue(this.bytes(), clazz);
        }
    }
}
