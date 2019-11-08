package com.example.hadad.Model;

import java.util.List;

public class User {
	private String email;
	private String name;
	private String uid;
	private String phone;
	private String image;
	private String cover;
	private String subscribers;

	public User(String email, String name, String uid, String phone, String image, String cover, String subscribers) {
		this.email = email;
		this.name = name;
		this.uid = uid;
		this.phone = phone;
		this.image = image;
		this.cover = cover;
		this.subscribers = subscribers;
	}

	public User() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(String subscribers) {
		this.subscribers = subscribers;
	}
}
