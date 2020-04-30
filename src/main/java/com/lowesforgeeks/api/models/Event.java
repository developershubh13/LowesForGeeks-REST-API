package com.lowesforgeeks.api.models;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.lowesforgeeks.api.constants.EventType;
import com.lowesforgeeks.api.constants.RecurringFrequency;

@Document
public class Event {
	
	@Id
	String id;
	
	@NotNull
	EventType eventType;
	
	@NotBlank
	@Size(max = 100)
	String eventName;
	
	String description;
	
	@NotNull
	Member createdBy;
	
	@NotBlank
	String location;
	
	@NotBlank
	String startDate;
	
	@NotBlank
	String endDate;
	
	@NotBlank
	String createdDate;
	
	Integer numberOfLikes;
	Integer numberOfWatchers;
	Integer numberOfViews;
	Integer numberOfParticipants;
	Boolean recurring;
	RecurringFrequency recurringFrequency;
	Boolean expired;
	List<Registration> registrations;
	
	public Event(EventType eventType, String eventName, String description, Member createdBy, String location, String startDate, String endDate,
				 String createdDate, Integer numberOfLikes, Integer numberOfWatchers, Integer numberOfViews, Integer numberOfParticipants,
				 Boolean recurring, RecurringFrequency recurringFrequency, Boolean expired, List<Registration> registrations) {
		this.eventType = eventType;
		this.eventName = eventName;
		this.description = description;
		this.createdBy = createdBy;
		this.location = location;
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdDate = createdDate;
		this.numberOfLikes = numberOfLikes;
		this.numberOfWatchers = numberOfWatchers;
		this.numberOfViews = numberOfViews;
		this.numberOfParticipants = numberOfParticipants;
		this.recurring = recurring;
		this.recurringFrequency = recurringFrequency;
		this.expired = expired;
		this.registrations = registrations;
	}
	
	

	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Member getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Member createdBy) {
		this.createdBy = createdBy;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Integer getNumberOfLikes() {
		return numberOfLikes;
	}

	public void setNumberOfLikes(Integer numberOfLikes) {
		this.numberOfLikes = numberOfLikes;
	}

	public Integer getNumberOfWatchers() {
		return numberOfWatchers;
	}

	public void setNumberOfWatchers(Integer numberOfWatchers) {
		this.numberOfWatchers = numberOfWatchers;
	}

	public Integer getNumberOfViews() {
		return numberOfViews;
	}

	public void setNumberOfViews(Integer numberOfViews) {
		this.numberOfViews = numberOfViews;
	}

	public Integer getNumberOfParticipants() {
		return numberOfParticipants;
	}

	public void setNumberOfParticipants(Integer numberOfParticipants) {
		this.numberOfParticipants = numberOfParticipants;
	}

	public Boolean getRecurring() {
		return recurring;
	}

	public void setRecurring(Boolean recurring) {
		this.recurring = recurring;
	}

	public RecurringFrequency getRecurringFrequency() {
		return recurringFrequency;
	}

	public void setRecurringFrequency(RecurringFrequency recurringFrequency) {
		this.recurringFrequency = recurringFrequency;
	}

	public Boolean getExpired() {
		return expired;
	}

	public void setExpired(Boolean expired) {
		this.expired = expired;
	}

	public List<Registration> getRegistrations() {
		return registrations;
	}

	public void setRegistrations(List<Registration> registrations) {
		this.registrations = registrations;
	}
	
}
