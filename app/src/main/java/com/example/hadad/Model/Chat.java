package com.example.hadad.Model;

/**
 * Tạo đối tượng chat và các thuộc tính cho chat
 * sender, reciver là ID của người gửi và người nhận
 * message: Nội dung tin nhắn
 * image, video: URL của ảnh hoặc video đã gửi
 * isseen: check người dùng đã xem tin nhắn hay chưa
 */
public class Chat {
	String sender, reciver, message, timestamp, image, video;
	boolean isseen;

	public Chat() {
	}

	public Chat(String sender, String reciver, String message, String timestamp, String image, String video, boolean isseen) {
		this.sender = sender;
		this.reciver = reciver;
		this.message = message;
		this.timestamp = timestamp;
		this.image = image;
		this.video = video;
		this.isseen = isseen;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReciver() {
		return reciver;
	}

	public void setReciver(String reciver) {
		this.reciver = reciver;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isIsseen() {
		return isseen;
	}

	public void setIsseen(boolean isseen) {
		this.isseen = isseen;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}
}
