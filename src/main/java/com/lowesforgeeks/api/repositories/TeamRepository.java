package com.lowesforgeeks.api.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lowesforgeeks.api.models.Team;

@Repository
public interface TeamRepository extends MongoRepository<Team, String> {

	public Team findByTeamName(String teamName);
}
