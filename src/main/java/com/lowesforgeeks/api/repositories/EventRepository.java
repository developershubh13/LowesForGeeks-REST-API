package com.lowesforgeeks.api.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.lowesforgeeks.api.constants.EventType;
import com.lowesforgeeks.api.models.Event;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
	
	public Event findByEventName(String eventName);
	public List<Event> findByEventType(EventType eventType);
	
	@Query("{ 'eventType' : ?0, 'expired' : ?1 }")
	public List<Event> findByEventTypeAndExpired(EventType eventType, Boolean expired);
	public List<Event> findByExpired(Boolean expired);

}
