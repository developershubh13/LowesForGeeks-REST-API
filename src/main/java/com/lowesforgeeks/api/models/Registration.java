package com.lowesforgeeks.api.models;

import org.springframework.data.mongodb.core.mapping.Document;

import com.lowesforgeeks.api.constants.ResponseType;

@Document
public class Registration {
	
	private Member member;
	private ResponseType responseType;
	
	public Registration(Member member, ResponseType responseType) {
		this.member = member;
		this.responseType = responseType;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

}
