package com.example.hadad.Model;

/**
 * tạo đối tượng post và các thuộc tính của nó
 */
public class Post {
	String pId, pDescr, pImage, pTime, uid, uEmail, uDp, uName, hostUid, pMode;

	public Post() {
	}

	public Post(String pId, String pDescr, String pImage, String pTime, String uid, String uEmail, String uDp, String uName, String hostUid, String pMode) {
		this.pId = pId;
		this.pDescr = pDescr;
		this.pImage = pImage;
		this.pTime = pTime;
		this.uid = uid;
		this.uEmail = uEmail;
		this.uDp = uDp;
		this.uName = uName;
		this.hostUid = hostUid;
		this.pMode = pMode;
	}

	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public String getpDescr() {
		return pDescr;
	}

	public void setpDescr(String pDescr) {
		this.pDescr = pDescr;
	}

	public String getpImage() {
		return pImage;
	}

	public void setpImage(String pImage) {
		this.pImage = pImage;
	}

	public String getpTime() {
		return pTime;
	}

	public void setpTime(String pTime) {
		this.pTime = pTime;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getuEmail() {
		return uEmail;
	}

	public void setuEmail(String uEmail) {
		this.uEmail = uEmail;
	}

	public String getuDp() {
		return uDp;
	}

	public void setuDp(String uDp) {
		this.uDp = uDp;
	}

	public String getuName() {
		return uName;
	}

	public void setuName(String uName) {
		this.uName = uName;
	}

	public String getHostUid() {
		return hostUid;
	}

	public void setHostUid(String hostUid) {
		this.hostUid = hostUid;
	}

	public String getpMode() {
		return pMode;
	}

	public void setpMode(String pMode) {
		this.pMode = pMode;
	}
}
