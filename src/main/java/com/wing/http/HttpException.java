package com.wing.http;
public class HttpException extends Exception {
	private static final long serialVersionUID = -7355717529136127229L;

	HttpException(String msg) {
		super(msg);
	}
}