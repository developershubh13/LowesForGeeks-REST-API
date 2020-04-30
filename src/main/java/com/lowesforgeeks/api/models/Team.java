package com.lowesforgeeks.api.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Team {
	
	@Id
	String id;
	String teamName;
	List<Member> members;
	
	public Team(String teamName, List<Member> members) {
		this.teamName = teamName;
		this.members = members;
	}
	
	public String getTeamId() {
		return id;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
	}

}
