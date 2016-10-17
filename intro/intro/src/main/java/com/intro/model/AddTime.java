package com.intro.model;

public class AddTime {

	@com.google.gson.annotations.SerializedName("time")
	String time;
	
	@com.google.gson.annotations.SerializedName("id")
	String id;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
