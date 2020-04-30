package com.lowesforgeeks.api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowesforgeeks.api.models.Team;
import com.lowesforgeeks.api.repositories.TeamRepository;

@Service
public class TeamService {
	
	@Autowired
	private TeamRepository teamRepository;
	
	public Team create(Team team) {
		return teamRepository.save(team);
	}
	
	public Optional<Team> getById(String id) {
		return teamRepository.findById(id);
	}
	
	public Team getByTeamName(String teamName) {
		return teamRepository.findByTeamName(teamName);
	}
	
	public List<Team> getAll() {
		return teamRepository.findAll();
	}
	
	public Team update(Team team, String id) {
		Team existingTeam = teamRepository.findById(id).get();
		existingTeam.setTeamName(team.getTeamName());
		existingTeam.setMembers(team.getMembers());
		return  teamRepository.save(existingTeam);
	}

}
