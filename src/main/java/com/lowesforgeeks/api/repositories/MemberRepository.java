package com.lowesforgeeks.api.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.lowesforgeeks.api.models.Member;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {

	public Member findByFirstName(String firstName);

	public List<Member> findByIsOrgAdmin(Boolean isOrgAdmin);

	public List<Member> findByIsTeamAdmin(Boolean isTeamAdmin);
	
}
