package com.intro.model;

public class Users {

	@com.google.gson.annotations.SerializedName("Users")
	String Users;
	
	@com.google.gson.annotations.SerializedName("password")
	String password;
	
	@com.google.gson.annotations.SerializedName("id")
	String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Users() {
		
	}
	
	public Users(String email, String password){
		this.setUsers(email);
		this.setPassword(password);
	}
	
	
	
	@Override
	public String toString() {
		return getUsers();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsers() {
		return Users;
	}

	public void setUsers(String users) {
		Users = users;
	}


	
	
}
