package com.lowesforgeeks.api.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lowesforgeeks.api.models.Member;
import com.lowesforgeeks.api.models.Team;
import com.lowesforgeeks.api.services.MemberService;
import com.lowesforgeeks.api.services.TeamService;
import com.lowesforgeeks.api.utils.FieldErrorMessage;

@RestController
@RequestMapping("/member")
public class MemberController {
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private TeamService teamService;
	
	@PostMapping("/create")
	public ResponseEntity<Member> create(@RequestHeader("loggedInMemberId") String loggedInMemberId, @Valid @RequestBody Member member) {
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		
		if(loggedInMember.isPresent()) {
			
			Member presentMember = loggedInMember.get();
			
			if(presentMember.getIsOrgAdmin()) {
				
				Member createdMember = memberService.create(member);
				
				if(createdMember == null) {
					return ResponseEntity.unprocessableEntity().build();
				}
				else {
					return new ResponseEntity<Member>(createdMember, HttpStatus.OK);
				}
			}
			else {
				throw new ValidationException("Member can be created by organization admin only.");
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
	public ResponseEntity<Member> view(@RequestHeader("loggedInMemberId") String loggedInMemberId, @PathVariable String id) {
		
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		Optional<Member> memberFromDb = memberService.getById(id);
		
		if(loggedInMember.isPresent() && memberFromDb.isPresent()) {
			
			Member member = memberFromDb.get();
			Member presentMember = loggedInMember.get();
			
			if(presentMember.getIsOrgAdmin()) {
				return new ResponseEntity<Member>(member, HttpStatus.OK);
			}
			else if(presentMember.getIsTeamAdmin()) {
				if(presentMember.getTeamId().equals(member.getTeamId())) {
					return new ResponseEntity<Member>(member, HttpStatus.OK);
				}
				else {
					throw new ValidationException("Team admin can view members of own team only.");
				}
			}
			else {
				if(member.getIsOrgAdmin() || member.getIsTeamAdmin()) {
					return new ResponseEntity<Member>(member, HttpStatus.OK);
				}
				else {
					throw new ValidationException("Normal members can only view organization and team admins.");
				}
			}
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/viewAll")
	public ResponseEntity<List<Member>> viewAll(@RequestHeader("loggedInMemberId") String loggedInMemberId) {
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		
		if(loggedInMember.isPresent()) {
			
			Member presentMember = loggedInMember.get();
			
			if(presentMember.getIsOrgAdmin()) {
				return new ResponseEntity<List<Member>>(memberService.getAll(), HttpStatus.OK);
			}
			else if(presentMember.getIsTeamAdmin()) {
				
				Optional<Team> team = teamService.getById(presentMember.getTeamId());
				
				if(team.isPresent()) {
					Team t = team.get();
					return new ResponseEntity<List<Member>>(t.getMembers(), HttpStatus.OK);
				}
				else {
					return ResponseEntity.notFound().build();
				}
			}
			else {
				Set<Member> admins = new HashSet<Member>();
				
				admins.addAll(memberService.getOrganizationAdmins(true));
				admins.addAll(memberService.getTeamAdmins(true));
				
				List<Member> members = new ArrayList<Member>();
				
				members.addAll(admins);
				
				return new ResponseEntity<List<Member>>(members, HttpStatus.OK);
			}
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<String> update(@RequestHeader("loggedInMemberId") String loggedInMemberId, @PathVariable String id, 
										 @RequestBody Member member) {
		
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		Optional<Member> memberFromDb = memberService.getById(id);
		
		if(loggedInMember.isPresent() && memberFromDb.isPresent()) {
			
			Member oldMember = memberFromDb.get();
			Member presentMember = loggedInMember.get();
			
			if(presentMember.getIsOrgAdmin()) {
				if(loggedInMemberId.equals(id) && member.getIsOrgAdmin() == false) {
					throw new ValidationException("Organization must have at least 1 organization admin.");
				}
				else {
					memberService.update(member, id);
					return new ResponseEntity<String>("Member updated successfully!", HttpStatus.OK);
				}
			}
			else if(presentMember.getIsTeamAdmin()) {
				if( (oldMember.getId().equals(member.getId()) && member.getIsTeamAdmin() == false) || 
					!(oldMember.getTeamId().equals(presentMember.getTeamId())) || oldMember.getIsOrgAdmin() ) {
					throw new ValidationException("Invalid operation by team admin.");
				}
				else {
					memberService.update(member, id);
					return new ResponseEntity<String>("Member updated successfully!", HttpStatus.OK);
				}
			}
			else {
				if(oldMember.getId().equals(id) && !member.getIsOrgAdmin() && !member.getIsTeamAdmin()) {
					memberService.update(member, id);
					return new ResponseEntity<String>("Member updated successfully!", HttpStatus.OK);
				}
				else {
					throw new ValidationException("Operation not allowed.");
				}
			}
		}
		
		return ResponseEntity.notFound().build();
	}

}
