package com.wing.http;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Http {
	
	private HttpClient httpClient;
	
	private ObjectMapper objectMapper;
	
	public Http(HttpClient httpClient, ObjectMapper objectMapper) {
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	public ObjectMapper objectMapper() {
		return this.objectMapper;
	}
	
	public Request get(String url) {
		Request request = new Request(httpClient, objectMapper);
		request.url = url;
		request.method = "GET";
		
		return request;
	}
	
	/**
	 * post请求，添加默认请求头Content-Type=application/json
	 * @param url
	 * @param jsonParams json字符串
	 * @return
	 */
	public Request post(String url, String jsonParams) {
		Request request = new Request(httpClient, objectMapper);
		request.url = url;
		request.method = "POST";
		request.headers = new String[] {"Content-Type", "application/json"};
		request.body = jsonParams.getBytes();
		
		return request;
	}
	
	/**
	 * 发送表单，添加默认请求头Content-Type=application/x-www-form-urlencoded
	 * @param url
	 * @param params
	 * @return
	 */
	public Request form(String url, Map<String, String> params) {
		Request request = new Request(httpClient, objectMapper);
		request.url = url;
		request.method = "POST";
		request.headers = new String[] {"Content-Type", "application/x-www-form-urlencoded"};
		request.body = params.entrySet().stream().map(item -> item.getKey() + "=" + item.getValue()).collect(Collectors.joining("&")).getBytes();
		
		return request;
	}
	
	/**
	 * 上传文件，添加默认请求头Content-Type=multipart/form-data
	 * @param url
	 * @param files
	 * @param params
	 * @return
	 */
	public Request upload(String url, Map<String, byte[]> files, Map<String, String> params) {
		Request request = new Request(httpClient, objectMapper);
		request.url = url;
		request.method = "POST";
		request.headers = new String[] {"Content-Type", "multipart/form-data; boundary=----boundary"};
		
		byte[] boundary = "\r\n------boundary\r\n".getBytes();
		byte[] content = boundary;
		if (files != null && !files.isEmpty()) {
			for (Entry<String, byte[]> item : files.entrySet()) {
				String key = item.getKey();
				byte[] value = item.getValue();
				byte[] contentDisposition = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n", key, key).getBytes();
				byte[] contentType = "Content-Type: application/octet-stream\r\n\r\n".getBytes();
				
				byte[] joinedArray = new byte[content.length + contentDisposition.length + contentType.length + value.length + boundary.length];
		        System.arraycopy(content, 0, joinedArray, 0, content.length);
		        System.arraycopy(contentDisposition, 0, joinedArray, content.length, contentDisposition.length);
		        System.arraycopy(contentType, 0, joinedArray, content.length + contentDisposition.length, contentType.length);
		        System.arraycopy(value, 0, joinedArray, content.length + contentDisposition.length + contentType.length, value.length);
		        System.arraycopy(boundary, 0, joinedArray, content.length + contentDisposition.length + contentType.length + value.length, boundary.length);
		        content = joinedArray;
				
			};
		}
		if (params != null && !params.isEmpty()) {
			for (Entry<String, String> item : params.entrySet()) {
				String key = item.getKey();
				String value = item.getValue();
				byte[] contentDisposition = String.format("Content-Disposition: form-data; name=\"%s\"\r\n\r\n", key).getBytes();
				
				byte[] joinedArray = new byte[content.length + contentDisposition.length + value.getBytes().length + boundary.length];
		        System.arraycopy(content, 0, joinedArray, 0, content.length);
		        System.arraycopy(contentDisposition, 0, joinedArray, content.length, contentDisposition.length);
		        System.arraycopy(value.getBytes(), 0, joinedArray, content.length + contentDisposition.length, value.getBytes().length);
		        System.arraycopy(boundary, 0, joinedArray, content.length + contentDisposition.length + value.getBytes().length, boundary.length);
		        content = joinedArray;
			};
		}
		byte[] joinedArray = new byte[content.length - 2];
		System.arraycopy(content, 2, joinedArray, 0, joinedArray.length - 2);
		System.arraycopy("--".getBytes(), 0, joinedArray, joinedArray.length - 2, 2);
		request.body = joinedArray;
		
		return request;
	}
	
	/**
	 * 上传文件
	 * @param url
	 * @return
	 */
	public UploadRequest upload(String url) {
		UploadRequest request = new UploadRequest(url, httpClient, objectMapper);
		
		return request;
	}
	
}
