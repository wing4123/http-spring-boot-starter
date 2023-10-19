package com.wing.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
	protected String url;
	protected String method;
	protected byte[] body;
	protected String[] headers = new String[0];
	
	protected HttpClient httpClient;
	
	protected ObjectMapper objectMapper;
	
	public Request(HttpClient httpClient, ObjectMapper objectMapper) {
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}
	
	/**
	 * 添加请求头
	 * @param headers
	 * @return
	 */
	public Request headers(Map<String, String> headers) {
		if (headers != null && !headers.isEmpty()) {
			String[] headerArray = headers.entrySet().stream().flatMap(item -> Stream.of(item.getKey(), item.getValue())).toArray(String[]::new);
			String[] joinedArray = new String[this.headers.length + headerArray.length];
			System.arraycopy(this.headers, 0, joinedArray, 0, this.headers.length);
			System.arraycopy(headerArray, 0, joinedArray, this.headers.length, headerArray.length);
			this.headers = joinedArray;
		}
		
		return this;
	}
	
	/**
	 * 添加请求头，多个参数按照key value顺序添加
	 * @param headers
	 * @return
	 */
	public Request headers(String... headers) {
		String[] joinedArray = new String[this.headers.length + headers.length];
		System.arraycopy(this.headers, 0, joinedArray, 0, this.headers.length);
		System.arraycopy(headers, 0, joinedArray, this.headers.length, headers.length);
		this.headers = joinedArray;
		return this;
	}
	
	private HttpRequest buildRequest() {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
		        .uri(URI.create(this.url))
		        .method(method.toUpperCase(), this.body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(this.body));
		if (this.headers.length > 1) {
			requestBuilder.headers(this.headers);
		}
		
		return requestBuilder.build();
	}
	
	/**
	 * 发送http请求
	 * @param url
	 * @param method
	 * @param body
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	public HttpResponse<byte[]> request() throws HttpException {
		try {
			HttpRequest request = this.buildRequest();
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			return response;
		} catch (IOException | InterruptedException e) {
			throw new HttpException("http请求异常");
		}
	}
	
	public CompletableFuture<HttpResponse<byte[]>> asyncRequest() throws HttpException {
		try {
			HttpRequest request = this.buildRequest();
			return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			throw new HttpException("http请求异常");
		}
	}
	
	/**
	 * 发送请求并获取byte[]类型返回值
	 * @return
	 * @throws HttpException
	 */
	public byte[] requestBytes() throws HttpException {
		HttpResponse<byte[]> response = this.request();
		if (response.statusCode() != 200) {
			throw new HttpException("http请求异常,状态码：" + response.statusCode() + ", 响应体：" + new String(response.body()));
		}
		
		return response.body();
	}
	
	public CompletableFuture<byte[]> asyncRequestBytes() throws HttpException {
		return this.asyncRequest().thenApply(HttpResponse::body);
	}
	
	/**
	 * 发送请求并获取Stirng类型返回值
	 * @return
	 * @throws HttpException
	 */
	public String requestString() throws HttpException {
		return new String(this.requestBytes());
	}
	
	public CompletableFuture<String> asyncRequestString() throws HttpException {
		return this.asyncRequestBytes().thenApply(String::new);
	}
	
	/**
	 * 发送请求并获取JsonNode类型返回值
	 * @return
	 * @throws HttpException
	 */
	public JsonNode requestJson() throws HttpException {
		try {
			return objectMapper.readTree(this.requestBytes());
		} catch (IOException e) {
			throw new HttpException("json解析异常");
		}
	}
	
	/**
	 * 发送请求并获取JsonNode类型返回值
	 * @return
	 * @throws HttpException
	 */
	public CompletableFuture<JsonNode> asyncRequestJson() throws HttpException {
		return this.asyncRequestBytes().thenApply(result -> {
			try {
				return objectMapper.readTree(result);
			} catch (IOException e) {
				throw new RuntimeException("json解析异常");
			}
		});
	}
	
	/**
	 * 发送请求并获取自定义类型返回值
	 * @return
	 * @throws HttpException
	 */
	public <T> T requestObject(Class<T> valueType) throws HttpException {
		try {
			return objectMapper.readValue(this.requestBytes(), valueType);
		} catch (IOException e) {
			throw new HttpException("json解析异常");
		}
	}
	
	public <T> CompletableFuture<T> asyncRequestObject(Class<T> valueType) throws HttpException {
		return this.asyncRequestBytes().thenApply(result -> {
			try {
				return objectMapper.readValue(result, valueType);
			} catch (IOException e) {
				throw new RuntimeException("json解析异常");
			}
		});
	}
}