package com.nnvmso.web.json.facebook;

public class FacebookError {
	private String message;
	private String type;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
