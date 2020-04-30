package com.lowesforgeeks.api.models;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Member {
	
	@Id
	String id;
	
	@NotBlank
	String firstName;
	
	@NotBlank
	String lastName;
	
	@NotBlank
	@Email
	String email;
	
	Boolean isOrgAdmin;
	Boolean isTeamAdmin;
	String teamId;
	
	public Member(String firstName, String lastName, String email, Boolean isOrgAdmin, Boolean isTeamAdmin, String teamId) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.isOrgAdmin = isOrgAdmin;
		this.isTeamAdmin = isTeamAdmin;
		this.teamId = teamId;
	}
	
	

	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getIsOrgAdmin() {
		return isOrgAdmin;
	}

	public void setIsOrgAdmin(Boolean isOrgAdmin) {
		this.isOrgAdmin = isOrgAdmin;
	}

	public Boolean getIsTeamAdmin() {
		return isTeamAdmin;
	}

	public void setIsTeamAdmin(Boolean isTeamAdmin) {
		this.isTeamAdmin = isTeamAdmin;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

}
