package com.lowesforgeeks.api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowesforgeeks.api.constants.EventType;
import com.lowesforgeeks.api.models.Event;
import com.lowesforgeeks.api.repositories.EventRepository;

@Service
public class EventService {
	
	@Autowired
	private EventRepository eventRepository;
	
	public Event create(Event event) {
		return eventRepository.save(event);
	}
	
	public Optional<Event> getById(String id) {
		return eventRepository.findById(id);
	}
	
	public Event getByEventName(String eventName) {
		return eventRepository.findByEventName(eventName);
	}
	
	public List<Event> getByEventTypeAndExpired(EventType eventType, Boolean expired) {
		return eventRepository.findByEventTypeAndExpired(eventType, expired);
	}
	
	public List<Event> getByExpired(Boolean expired) {
		return eventRepository.findByExpired(expired);
	}
	
	public List<Event> getAll() {
		return eventRepository.findAll();
	}
	
	public Event update(Event event, String id) {
		
		Optional<Event> existingEvent = eventRepository.findById(id);
		Event e = existingEvent.get();
		e.setEventType(event.getEventType());
		e.setEventName(event.getEventName());
		e.setDescription(event.getDescription());
		e.setCreatedBy(event.getCreatedBy());
		e.setLocation(event.getLocation());
		e.setStartDate(event.getStartDate());
		e.setNumberOfLikes(event.getNumberOfLikes());
		e.setNumberOfParticipants(event.getNumberOfParticipants());
		e.setNumberOfViews(event.getNumberOfViews());
		e.setNumberOfWatchers(event.getNumberOfWatchers());
		e.setRecurring(event.getRecurring());
		e.setRecurringFrequency(event.getRecurringFrequency());
		e.setExpired(event.getExpired());
		e.setRegistrations(event.getRegistrations());
		return eventRepository.save(event);
		
	}
	
	public void delete(String id) {
		eventRepository.deleteById(id);
	}

}
