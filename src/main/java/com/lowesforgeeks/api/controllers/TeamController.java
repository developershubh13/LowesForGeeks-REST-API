package com.lowesforgeeks.api.controllers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lowesforgeeks.api.models.Member;
import com.lowesforgeeks.api.models.Team;
import com.lowesforgeeks.api.services.MemberService;
import com.lowesforgeeks.api.services.TeamService;

@RestController
@RequestMapping("/team")
public class TeamController {
	
	@Autowired
	private TeamService teamService;
	
	@Autowired
	private MemberService memberService;
	
	public void updateTeamId(List<Member> existingList, List<Member> newList, String teamId) {
		
		Set<Member> set1 = new HashSet<Member>();
		Set<Member> set2 = new HashSet<Member>();
		
		set1.addAll(existingList);
		set2.addAll(newList);
		
		set1.removeAll(set2);
		
		if(!set1.isEmpty()) {
			set1.forEach(m -> {
				m.setTeamId("");
				memberService.update(m, m.getId());
			});
		}
		
		set1.clear();
		set2.clear();
		
		set1.addAll(newList);
		set2.addAll(existingList);
		
		set1.removeAll(set2);
		
		if(!set1.isEmpty()) {
			set1.forEach(m -> {
				m.setTeamId(teamId);
				memberService.update(m, m.getId());
			});
		}
		
		set1.clear();
		set2.clear();
	}
	
	@PostMapping("/create")
	public ResponseEntity<Team> create(@RequestHeader("loggedInMemberId") String loggedInMemberId, @RequestBody Team team) {
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		
		if(loggedInMember.isPresent()) {
			
			Member presentMember = loggedInMember.get();
			
			if(presentMember.getIsTeamAdmin()) {
				throw new ValidationException("Logged in member already present in a team.");
			}
			else {
				List<Member> members = team.getMembers();
				
				Iterator<Member> i = members.iterator();
				
				while(i.hasNext()) {
					Member m = i.next();
					if(m.getTeamId() != null) {
						i.remove();
					}
				}
				
				team.setMembers(members);
				
				Team createdTeam = teamService.create(team);
				String teamId = createdTeam.getTeamId();
				
				List<Member> addedMembers = team.getMembers();
				
				addedMembers.forEach(m -> {
					m.setTeamId(teamId);
					if(m.getId().equals(loggedInMemberId)) {
						m.setIsTeamAdmin(true);
					}
					memberService.update(m, m.getId());
				});
				
				return new ResponseEntity<Team>(createdTeam, HttpStatus.OK);
			}
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/view/{id}")
	public ResponseEntity<Team> view(@RequestHeader("loggedInMemberId") String loggedInMemberId, @PathVariable String id) {
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		Optional<Team> team = teamService.getById(id);
		
		if(loggedInMember.isPresent() && team.isPresent()) {
			
			return new ResponseEntity<Team>(team.get(), HttpStatus.OK);
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/viewAll")
	public ResponseEntity<List<Team>> viewAll() {
		return new ResponseEntity<List<Team>>(teamService.getAll(), HttpStatus.OK);
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<Team> update(@RequestHeader("loggedInMemberId") String loggedInMemberId, @PathVariable String id, @RequestBody Team team) {
		Optional<Member> loggedInMember = memberService.getById(loggedInMemberId);
		Optional<Team> teamFromDb = teamService.getById(id);
		
		if(loggedInMember.isPresent() && teamFromDb.isPresent()) {
			
			Member presentMember = loggedInMember.get();
			
			if(presentMember.getIsOrgAdmin()) {
				updateTeamId(teamFromDb.get().getMembers(), team.getMembers(), id);
				return new ResponseEntity<Team>(teamService.update(team, id), HttpStatus.OK);
			}
			else if(presentMember.getIsTeamAdmin()) {
				if(presentMember.getTeamId().equals(id)) {
					updateTeamId(teamFromDb.get().getMembers(), team.getMembers(), id);
					return new ResponseEntity<Team>(teamService.update(team, id), HttpStatus.OK);
				}
				else {
					throw new ValidationException("Cannot update another team.");
				}
			}
			else {
				throw new ValidationException("Normal member cannot update any team.");
			}
		}
		
		return ResponseEntity.notFound().build();
	}

}
