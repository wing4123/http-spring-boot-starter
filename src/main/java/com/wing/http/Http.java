package com.wing.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Http {
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	public Http() {}
	
	public Http(HttpClient httpClient, ObjectMapper objectMapper) {
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	public ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}
	
	public Request get(String url) {
		Request request = new Request();
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
		Request request = new Request();
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
		Request request = new Request();
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
		Request request = new Request();
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
		UploadRequest request = new UploadRequest(url);
		
		return request;
	}
	
	public class Request {
		private String url;
		private String method;
		private byte[] body;
		private String[] headers = new String[] {"Content-Type", "text/plain"};
		
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
		
		/**
		 * 发送http请求
		 * @param url
		 * @param method
		 * @param body
		 * @param headers
		 * @return
		 * @throws IOException
		 */
		private byte[] request() throws HttpException {
			try {
				HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				        .uri(new URI(this.url))
				        .headers(this.headers)
				        .method(method.toUpperCase(), this.body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(this.body));
				
				HttpResponse<byte[]> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
				
				if (response.statusCode() != 200) {
		        	throw new HttpException("http请求异常, 状态码:" + response.statusCode() + ", 响应:" + new String(response.body()));
		        }
				
				return response.body();
			} catch (Exception e) {
				throw new HttpException(e.getMessage());
			}
		}
		
		/**
		 * 发送请求并获取byte[]类型返回值
		 * @return
		 * @throws HttpException
		 */
		public byte[] requestBytes() throws HttpException {
			return this.request();
		}
		
		/**
		 * 发送请求并获取Stirng类型返回值
		 * @return
		 * @throws HttpException
		 */
		public String requestString() throws HttpException {
			return new String(this.request());
		}
		
		/**
		 * 发送请求并获取JsonNode类型返回值
		 * @return
		 * @throws HttpException
		 */
		public JsonNode requestJson() throws HttpException {
			try {
				return objectMapper.readTree(this.request());
			} catch (IOException e) {
				throw new HttpException("json解析异常");
			}
		}
		
		/**
		 * 发送请求并获取自定义类型返回值
		 * @return
		 * @throws HttpException
		 */
		public <T> T requestObject(Class<T> valueType) throws HttpException {
			try {
				return objectMapper.readValue(this.request(), valueType);
			} catch (IOException e) {
				throw new HttpException("json解析异常");
			}
		}
	}
	
	public class UploadRequest extends Request {
		private final byte[] boundary = "\r\n------boundary--".getBytes();
		
		UploadRequest(String url) {
			super.url = url;
			
			super.method = "POST";
			super.headers = new String[] {"Content-Type", "multipart/form-data; boundary=----boundary"};
			super.body = "------boundary--".getBytes();
		}
		
		/**
		 * 添加文件参数
		 * @param name
		 * @param fileName
		 * @param file
		 * @return
		 */
		public UploadRequest addFile(String name, String fileName, byte[] file) {
			super.body[super.body.length - 2] = "\r".getBytes()[0];
			super.body[super.body.length - 1] = "\n".getBytes()[0];
			
			byte[] contentDisposition = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n", name, fileName).getBytes();
			byte[] contentType = "Content-Type: application/octet-stream\r\n\r\n".getBytes();
			
			byte[] joinedArray = new byte[super.body.length + contentDisposition.length + contentType.length + file.length + boundary.length];
	        System.arraycopy(super.body, 0, joinedArray, 0, super.body.length);
	        System.arraycopy(contentDisposition, 0, joinedArray, super.body.length, contentDisposition.length);
	        System.arraycopy(contentType, 0, joinedArray, super.body.length + contentDisposition.length, contentType.length);
	        System.arraycopy(file, 0, joinedArray, super.body.length + contentDisposition.length + contentType.length, file.length);
	        System.arraycopy(boundary, 0, joinedArray, super.body.length + contentDisposition.length + contentType.length + file.length, boundary.length);
	        super.body = joinedArray;
	        
	        return this;
		}
		
		public UploadRequest addFile(String name, String filePath) throws IOException {
			Path path = Paths.get(filePath);
			String fileName = path.getFileName().toString();
	        byte[] file = Files.readAllBytes(path);
	        
	        return this.addFile(name, fileName, file);
		}
		
		/**
		 * 添加字符串参数
		 * @param key
		 * @param value
		 * @return
		 */
		public UploadRequest addParam(String key, String value) {
			super.body[super.body.length - 2] = "\r".getBytes()[0];
			super.body[super.body.length - 1] = "\n".getBytes()[0];
			
			byte[] contentDisposition = String.format("Content-Disposition: form-data; name=\"%s\"\r\n\r\n", key).getBytes();
			
			byte[] joinedArray = new byte[super.body.length + contentDisposition.length + value.getBytes().length + boundary.length];
	        System.arraycopy(super.body, 0, joinedArray, 0, super.body.length);
	        System.arraycopy(contentDisposition, 0, joinedArray, super.body.length, contentDisposition.length);
	        System.arraycopy(value.getBytes(), 0, joinedArray, super.body.length + contentDisposition.length, value.getBytes().length);
	        System.arraycopy(boundary, 0, joinedArray, super.body.length + contentDisposition.length + value.getBytes().length, boundary.length);
	        super.body = joinedArray;
	        
	        return this;
		}
	}
	
	public class HttpException extends RuntimeException {
		private static final long serialVersionUID = -7355717529136127229L;

		HttpException(String msg) {
			super(msg);
		}
	}
	
	public static void main(String[] args) throws IOException {
		String url = "http://localhost:8088/upload";
		Http http = new Http();
		JsonNode result = http.upload(url)
				.addFile("file1", "image0.jpg", Files.readAllBytes(Paths.get("d:/image0.jpg")))
				.addFile("file2", "image1.jpg", Files.readAllBytes(Paths.get("d:/image1.jpg")))
//				.addFile("file1", "fileName1.xyz", new byte[] {1, 2, 3})
//				.addFile("file2", "fileName1.xyz", new byte[] {7, 8, 9})
				.addParam("param1", "abc")
				.addParam("param2", "123")
				.requestJson();
		
		System.out.println(result.toPrettyString());
	}
	
}
