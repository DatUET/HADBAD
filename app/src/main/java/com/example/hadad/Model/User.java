package com.example.hadad.Model;
/**
 * Đối tượng người dùng
 */
public class User {
	private String email;
	private String name;
	private String uid; //id của người dùng do google Authentication tạo
	private String phone;
	private String image; //URL của ảnh đại diện người dùng
	private String cover; //URL của ảnh bìa
	private String onlineStatus;

	public User(String email, String name, String uid, String phone, String image, String cover, String onlineStatus) {
		this.email = email;
		this.name = name;
		this.uid = uid;
		this.phone = phone;
		this.image = image;
		this.cover = cover;
		this.onlineStatus = onlineStatus;
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

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
}
