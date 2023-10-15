package com.wing.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UploadRequest extends Request {
	private final byte[] boundary = "\r\n------boundary--".getBytes();
	
	public UploadRequest(String url, HttpClient httpClient, ObjectMapper objectMapper) {
		super(httpClient, objectMapper);
		
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