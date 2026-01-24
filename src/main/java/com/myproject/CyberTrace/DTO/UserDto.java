package com.myproject.CyberTrace.DTO;

import org.springframework.web.multipart.MultipartFile;

public class UserDto {

	private String name;
	private String contactNo;
	private String email;
	private String password;
	private String gender;
	private String address;
	private MultipartFile profilePic;
	private MultipartFile govtIdProof;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContactNo() {
		return contactNo;
	}
	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public MultipartFile getProfilePic() {
		return profilePic;
	}
	public void setProfilePic(MultipartFile profilePic) {
		this.profilePic = profilePic;
	}
	public MultipartFile getGovtIdProof() {
		return govtIdProof;
	}
	public void setGovtIdProof(MultipartFile govtIdProof) {
		this.govtIdProof = govtIdProof;
	}
	
	
}
