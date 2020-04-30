package com.lowesforgeeks.api.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lowesforgeeks.api.constants.EventType;
import com.lowesforgeeks.api.constants.RecurringFrequency;
import com.lowesforgeeks.api.models.Event;
import com.lowesforgeeks.api.models.Member;
import com.lowesforgeeks.api.repositories.EventRepository;
import com.lowesforgeeks.api.services.EventService;
import com.lowesforgeeks.api.services.MemberService;
import com.lowesforgeeks.api.utils.FieldErrorMessage;

@RestController
@RequestMapping("/event")
public class EventController {
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private EventRepository eventRepository;
	
	public LocalDateTime getNextDate(LocalDateTime date, LocalDateTime presentDate, RecurringFrequency recurringFrequency) {
		
		long daysBetween = ChronoUnit.DAYS.between(presentDate, date);
		
		if(recurringFrequency == RecurringFrequency.W) {
			long weeks = Math.round(daysBetween / 7);
			return date.plusDays(weeks * 7);
		}
		else if(recurringFrequency == RecurringFrequency.M) {
			long months = Math.round(daysBetween / 30);
			return date.plusMonths(months);
		}
		else if(recurringFrequency == RecurringFrequency.Y) {
			long years = Math.round(daysBetween / 365);
			return date.plusYears(years);
		}
		
		return date.plusDays(daysBetween);
	}
	
	public Boolean checkRecurringEventActive(LocalDateTime startDate, LocalDateTime endDate, LocalDateTime presentDate) {
		
		if( (presentDate.isEqual(startDate) || presentDate.isAfter(startDate)) 
				&& (presentDate.isBefore(endDate) || presentDate.isEqual(endDate)) ) {
				return true;
		}
		
		return false;
	}
	
	@PostMapping("/create")
	public ResponseEntity<Event> create(@RequestHeader("loggedInMemberId") String loggedInMemberId, @Valid @RequestBody Event event) {
		
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		
		if(loggedInMember.isPresent()) {
			
			Member presentMember = loggedInMember.get();
			DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			String date = LocalDateTime.now().toString();
			
			try {
				
				LocalDateTime presentDate = LocalDateTime.parse(date, dtf);
				LocalDateTime startDate = LocalDateTime.parse(event.getStartDate(), dtf);
				long daysBetween = ChronoUnit.DAYS.between(presentDate, startDate);
		    	
		    	if(event.getEventType() == EventType.ORGANIZATION) {
					if(presentMember.getIsOrgAdmin()) {
						if(daysBetween >= 60) {
							return new ResponseEntity<Event>(eventService.create(event), HttpStatus.OK);
						}
						else {
							throw new ValidationException("Organization event must be created at least 2 months before event start date.");
						}
					}
					else {
						throw new ValidationException("Organization event can only be created by organization admin.");
					}
				}
				else if(event.getEventType() == EventType.TEAM) {
					if( presentMember.getIsOrgAdmin() || presentMember.getIsTeamAdmin() ) {
						if(daysBetween >= 7) {
							return new ResponseEntity<Event>(eventService.create(event), HttpStatus.OK);
						}
						else {
							throw new ValidationException("Team event must be created at least 1 week before event start date.");
						}
					}
					else {
						throw new ValidationException("Team event can only be created by either organization admin or team admin.");
					}
				}
				else if(event.getEventType() == EventType.PRIVATE) {
					if(daysBetween >= 2) {
						return new ResponseEntity<Event>(eventService.create(event), HttpStatus.OK);
					}
					else {
						throw new ValidationException("Private event must be created at least 2 days before event start date.");
					}
				}
			} catch(Exception e) {
					System.out.println("Exception :" + e.getMessage());
					throw new ValidationException(e.getMessage());
				}
		}
	    
	    return ResponseEntity.notFound().build();
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	List<FieldErrorMessage> exceptionHandler(MethodArgumentNotValidException e) {
		List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
		List<FieldErrorMessage> fieldErrorMessages = fieldErrors.stream().map(fieldError -> new FieldErrorMessage(fieldError.getField(), fieldError.getDefaultMessage())).collect(Collectors.toList());
		
		return fieldErrorMessages;
	}
	
	
	@GetMapping("/view/{id}")
	public ResponseEntity<Event> view(@RequestHeader("loggedInMemberId") String loggedInMemberId, @PathVariable String id) {
		
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		Optional<Event> event = eventService.getById(id);
		
		if(loggedInMember.isPresent() && event.isPresent()) {
			Event e = event.get();
			Member presentMember = loggedInMember.get();
			
			if(presentMember.getIsOrgAdmin()) {
				if(e.getEventType() == EventType.PRIVATE && !e.getCreatedBy().equals(presentMember)) {
					throw new ValidationException("Organization admin can only view private events created by him.");
				}
				else {
					e.setNumberOfViews(e.getNumberOfViews() + 1);
					Event updatedEvent = eventRepository.save(e);
					return new ResponseEntity<Event>(updatedEvent, HttpStatus.OK);
				}
			}
			else if(presentMember.getIsTeamAdmin()) {
				if(e.getEventType() == EventType.PRIVATE && !e.getCreatedBy().getTeamId().equals(presentMember.getTeamId())) {
					throw new ValidationException("Team admin can only view private events created by him and members of his team.");
				}
				else {
					e.setNumberOfViews(e.getNumberOfViews() + 1);
					Event updatedEvent = eventRepository.save(e);
					return new ResponseEntity<Event>(updatedEvent, HttpStatus.OK);
				}
			}
			else {
				if(e.getEventType() == EventType.PRIVATE && !e.getCreatedBy().getTeamId().equals(presentMember.getTeamId())) {
					throw new ValidationException("Normal members can only view private events created by him and members of his team.");
				}
				else {
					e.setNumberOfViews(e.getNumberOfViews() + 1);
					Event updatedEvent = eventRepository.save(e);
					return new ResponseEntity<Event>(updatedEvent, HttpStatus.OK);
				}
			}
		}
		
		return ResponseEntity.notFound().build();
	}
	
	
	@GetMapping("/viewAll")
	public ResponseEntity<List<Event>> viewAll(@RequestHeader("loggedInMemberId") String loggedInMemberId) {
		
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		
		if(loggedInMember.isPresent()) {
			
			Member presentMember = loggedInMember.get();
			//System.out.println("Member :" + presentMember);
			List<Event> events = eventService.getByExpired(false);
			//System.out.println("Events :" + events);

			//			filtering already hosted events
			
			DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			String date = LocalDateTime.now().toString();
			
			Iterator<Event> itr = events.iterator();
			
			while(itr.hasNext()) {
				
				Event e = itr.next();
				LocalDateTime startDate = LocalDateTime.parse(e.getStartDate(), dtf);
				LocalDateTime endDate = LocalDateTime.parse(e.getEndDate(), dtf);
				LocalDateTime presentDate = LocalDateTime.parse(date, dtf);
				
				if(!e.getRecurring() && endDate.isBefore(presentDate)) {
					e.setExpired(true);
					eventRepository.save(e);
					itr.remove();
				}
				else if(startDate.isAfter(presentDate)) {
					itr.remove();
				}
				else if(e.getRecurring()) {
					
					LocalDateTime nextStartDate = getNextDate(startDate, presentDate, e.getRecurringFrequency());
					LocalDateTime nextEndDate = getNextDate(endDate, presentDate, e.getRecurringFrequency());
					
					Boolean isEventActive = checkRecurringEventActive(nextStartDate, nextEndDate, presentDate);
					if(!isEventActive) {
						itr.remove();
					}
				}
			}
			
			//System.out.println("Events :" + events);
			
			if(presentMember.getIsOrgAdmin() && !presentMember.getIsTeamAdmin()) {
				
				Iterator<Event> itr1 = events.iterator();
				
				while(itr1.hasNext()) {
					Event e = itr1.next();
					if(e.getEventType() == EventType.PRIVATE && !e.getCreatedBy().equals(presentMember)) {
						itr1.remove();
					}
				}
				
				return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
			}
			else  {
				//System.out.println("inside else");
				Iterator<Event> itr2 = events.iterator();
				List<Event> filteredEvents = new ArrayList<Event>();
				
				while(itr2.hasNext()) {
					Event e = itr2.next();
					if(e.getEventType() == EventType.PRIVATE) {
						//System.out.println("true");
						if(e.getCreatedBy().equals(presentMember) || e.getCreatedBy().getTeamId().equals(presentMember.getTeamId())) {
							filteredEvents.add(e);
						}
					}
					else {
						filteredEvents.add(e);
					}
				}
				//System.out.println("Res :" + filteredEvents);
				return new ResponseEntity<List<Event>>(filteredEvents, HttpStatus.OK);
			}
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/viewTrending")
	public ResponseEntity<List<Event>> viewTrending() {
		
		List<Event> events = new ArrayList<Event>();
		events.addAll(eventService.getByEventTypeAndExpired(EventType.ORGANIZATION, false));
		events.addAll(eventService.getByEventTypeAndExpired(EventType.TEAM, false));
		
		Iterator<Event> itr = events.iterator();
		DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		String date = LocalDateTime.now().toString();
		
		while(itr.hasNext()) {
			Event e = itr.next();
			LocalDateTime createdDate = LocalDateTime.parse(e.getCreatedDate(), dtf);
			LocalDateTime presentDate = LocalDateTime.parse(date, dtf);
			LocalDateTime startDate = LocalDateTime.parse(e.getStartDate(), dtf);
			long diff1 = ChronoUnit.DAYS.between(createdDate, presentDate);
			long diff2 = ChronoUnit.DAYS.between(presentDate, startDate);
			
			if(diff1 < 60 || diff2 < 7 ) {
				itr.remove();
			}
			if(e.getRecurring()) {
				itr.remove();
			}
		}
		
		events.sort((e1, e2) -> {
			long sum1 = e1.getNumberOfLikes()+e1.getNumberOfWatchers()+e1.getNumberOfParticipants();
			long sum2 = e2.getNumberOfLikes()+e2.getNumberOfWatchers()+e2.getNumberOfParticipants(); 
			
			return (int)(sum2 - sum1);
		});
		
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}
	
	@GetMapping("/viewPopular")
	public ResponseEntity<List<Event>> viewPopular() {
		
		List<Event> events = new ArrayList<Event>();
		
		events.addAll(eventService.getByEventTypeAndExpired(EventType.ORGANIZATION, false));
		events.addAll(eventService.getByEventTypeAndExpired(EventType.TEAM, false));
		
		Iterator<Event> itr = events.iterator();
		
		DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		String date = LocalDateTime.now().toString();
		
		while(itr.hasNext()) {
			
			Event e = itr.next();
			LocalDateTime createdDate = LocalDateTime.parse(e.getCreatedDate(), dtf);
			LocalDateTime presentDate = LocalDateTime.parse(date, dtf);
			LocalDateTime startDate;
			
			if(e.getRecurring()) {
				startDate = getNextDate(LocalDateTime.parse(e.getStartDate(), dtf), presentDate, e.getRecurringFrequency());
			}
			else {
				startDate = LocalDateTime.parse(e.getStartDate(), dtf);
			}
			
			long diff1 = ChronoUnit.DAYS.between(createdDate, presentDate);
			long diff2 = ChronoUnit.DAYS.between(presentDate, startDate);
			
			if(diff1 < 30 || diff2 < 2 ) {
				itr.remove();
			}
		}
			
			events.sort((e1, e2) -> {
				long sum1 = e1.getNumberOfLikes()+e1.getNumberOfViews();
				long sum2 = e2.getNumberOfLikes()+e2.getNumberOfViews(); 
				
				return (int)(sum2 - sum1);
			});
			
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}
	
	@GetMapping("/viewUpcoming")
	public ResponseEntity<List<Event>> viewUpcoming() {
		
		List<Event> events = new ArrayList<Event>();
		
		events.addAll(eventService.getByEventTypeAndExpired(EventType.ORGANIZATION, false));
		events.addAll(eventService.getByEventTypeAndExpired(EventType.TEAM, false));
		events.addAll(eventService.getByEventTypeAndExpired(EventType.PRIVATE, false));
		
		Iterator<Event> itr = events.iterator();
		
		DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		String date = LocalDateTime.now().toString();
		
		while(itr.hasNext()) {
			
			Event e = itr.next();
			LocalDateTime createdDate = LocalDateTime.parse(e.getCreatedDate(), dtf);
			LocalDateTime presentDate = LocalDateTime.parse(date, dtf);
			LocalDateTime startDate;
			
			if(e.getRecurring()) {
				startDate = getNextDate(LocalDateTime.parse(e.getStartDate(), dtf), presentDate, e.getRecurringFrequency());
			}
			else {
				startDate = LocalDateTime.parse(e.getStartDate(), dtf);
			}
			
			long diff1 = ChronoUnit.DAYS.between(createdDate, presentDate);
			long diff2 = ChronoUnit.DAYS.between(presentDate, startDate);
			
			if(diff1 > 7 || diff2 < 1 ) {
				itr.remove();
			}
		}
			
		events.sort((e1, e2) -> e2.getNumberOfViews().compareTo(e1.getNumberOfViews()));
			
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<String> update(@RequestHeader("loggedInMemberId") String loggedInMemberId, @PathVariable String id,
										 @RequestBody Event event) {
		
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		Optional<Event> eventFromDb = eventService.getById(id);
		
		if(loggedInMember.isPresent() && eventFromDb.isPresent()) {
			System.out.println("all good!");
			Member presentMember = loggedInMember.get();
			Event e = eventFromDb.get();
			
			DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			String date = LocalDateTime.now().toString();
			LocalDateTime presentDate = LocalDateTime.parse(date, dtf);
			LocalDateTime startDate;
			
			if(e.getRecurring()) {
				startDate = getNextDate(LocalDateTime.parse(e.getStartDate(), dtf), presentDate, e.getRecurringFrequency());
			}
			else {
				startDate = LocalDateTime.parse(e.getStartDate(), dtf);
			}
			
			long daysBetween = ChronoUnit.DAYS.between(presentDate, startDate);
			LocalDateTime newStartDate = LocalDateTime.parse(event.getStartDate(), dtf);
			
			if(presentDate.isEqual(startDate) || presentDate.isAfter(startDate) && newStartDate.isAfter(presentDate)) {
				return new ResponseEntity<String>("Cannot reschedule event which has started.", HttpStatus.BAD_REQUEST);
			}
			else {
				
				if(presentMember.getIsOrgAdmin()) {
					
					if(e.getEventType() == EventType.ORGANIZATION) {
						if(daysBetween >= 30) {
							eventService.update(event, id);
							return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
						}
						else {
							if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								throw new ValidationException("Organizatoin event must be rescheduled at least 1 month before original start date.");
							}
						}
					}
					else if(e.getEventType() == EventType.TEAM){
						if(daysBetween >= 7) {
							eventService.update(event, id);
							return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
						}
						else {
							if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								throw new ValidationException("Team event must be rescheduled at least 1 week before original start date.");
							}
						}
					}
					else {
						if(e.getCreatedBy().getId().equals(loggedInMemberId)) {
							if(daysBetween >= 2) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
									eventService.update(event, id);
									return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
								}
								else {
									throw new ValidationException("Private event must be rescheduled at least 2 days before original start date.");
								}
							}
						}
						else {
							if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								throw new ValidationException("Cannot update other's private event.");
							}
						}
					}
				}
				else if(presentMember.getIsTeamAdmin()) {
					
					if(e.getEventType() == EventType.ORGANIZATION) {
						
						if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
							eventService.update(event, id);
							return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
						}
						else {
							throw new ValidationException("Cannot reschedule Organization event.");
						}
					}
					else if(e.getEventType() == EventType.TEAM) {
						
						if(e.getCreatedBy().getTeamId().equals(presentMember.getTeamId())) {
							if(daysBetween >= 7) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
									eventService.update(event, id);
									return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
								}
								else {
									throw new ValidationException("Team event must be rescheduled at least 1 week before original start date.");
								}
							}
						}
						else {
							if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								throw new ValidationException("Cannot reschedule other team's event.");
							}
						}
					}
					else {
						if(e.getCreatedBy().getId().equals(loggedInMemberId)) {
							if(daysBetween >= 2) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
									eventService.update(event, id);
									return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
								}
								else {
									throw new ValidationException("Private event must be rescheduled at least 2 days before original start date.");
								}
							}
						}
						else {
							if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								throw new ValidationException("Cannot update other's private event.");
							}
						}
					}
				}
				else if( !presentMember.getIsOrgAdmin() && !presentMember.getIsTeamAdmin()) {
					
					if(e.getEventType() == EventType.ORGANIZATION) {
						
						if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
							eventService.update(event, id);
							return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
						}
						else {
							throw new ValidationException("Cannot reschedule Organization event.");
						}
					}
					else if(e.getEventType() == EventType.TEAM) {
						
						if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
							eventService.update(event, id);
							return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
						}
						else {
							throw new ValidationException("Cannot reschedule Team event.");
						}
					}
					else {
						if(e.getCreatedBy().getId().equals(loggedInMemberId)) {
							
							if(daysBetween >= 2) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
									eventService.update(event, id);
									return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
								}
								else {
									throw new ValidationException("Private event must be rescheduled at least 2 days before original start date.");
								}
							}
						}
						else {
							if(e.getStartDate().equals(event.getStartDate()) && e.getEndDate().equals(event.getEndDate())) {
								eventService.update(event, id);
								return new ResponseEntity<String>("Updated Successfully", HttpStatus.OK);
							}
							else {
								throw new ValidationException("Cannot update other's private event.");
							}
						}
					}
				}
			}
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/delete")
	public ResponseEntity<String> delete(@RequestHeader("loggedInMemberId") String loggedInMemberId, @RequestParam String id) {
		
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		Optional<Event> eventFromDb = eventService.getById(id);
		
		if(loggedInMember.isPresent() && eventFromDb.isPresent()) {
			
			Member presentMember = loggedInMember.get();
			Event e = eventFromDb.get();
			
			DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			String date = LocalDateTime.now().toString();
			LocalDateTime presentDate = LocalDateTime.parse(date, dtf);
			LocalDateTime startDate;
			
			if(e.getRecurring()) {
				startDate = getNextDate(LocalDateTime.parse(e.getStartDate(), dtf), presentDate, e.getRecurringFrequency());
			}
			else {
				startDate = LocalDateTime.parse(e.getStartDate(), dtf);
			}
			
			long daysBetween = ChronoUnit.DAYS.between(presentDate, startDate);
			
			if(presentDate.isEqual(startDate) || presentDate.isAfter(startDate)) {
				return new ResponseEntity<String>("Cannot delete event that has started.", HttpStatus.BAD_REQUEST);
			}
			else {
				
				if(presentMember.getIsOrgAdmin() && e.getEventType() == EventType.ORGANIZATION) {
					if(daysBetween >= 14) {
						eventService.delete(id);
						return ResponseEntity.ok().build();
					}
					else {
						throw new ValidationException("Organizatoin event must be cancelled at least 2 weeks before original start date.");
					}
				}
				else if(presentMember.getIsTeamAdmin() && e.getEventType() == EventType.TEAM) {
					if(daysBetween >= 7) {
						eventService.delete(id);
						return ResponseEntity.ok().build();
					}
					else {
						throw new ValidationException("Team event must be cancelled at least 1 week before original start date.");
					}
				}
				else {
					if(daysBetween >= 2) {
						eventService.delete(id);
						return ResponseEntity.ok().build();
					}
					else {
						throw new ValidationException("Private event must be cancelled at least 2 days before original start date.");
					}
				}
			}
		}
		
		return ResponseEntity.notFound().build();
	}

}
