package com.lowesforgeeks.api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowesforgeeks.api.models.Member;
import com.lowesforgeeks.api.repositories.MemberRepository;

@Service
public class MemberService {
	
	@Autowired
	private MemberRepository memberRepository;
	
	public Member create(Member member) {
		return memberRepository.save(member);
	}
	
	public Optional<Member> getById(String id) {
		return memberRepository.findById(id);
	}
	
	public List<Member> getAll() {
		return memberRepository.findAll();
	}
	
	public Member getByFirstName(String firstName) {
		return memberRepository.findByFirstName(firstName);
	}
	
	public List<Member> getOrganizationAdmins(Boolean isOrgAdmin) {
		return memberRepository.findByIsOrgAdmin(isOrgAdmin);
	}
	
	public List<Member> getTeamAdmins(Boolean isTeamAdmin) {
		return memberRepository.findByIsTeamAdmin(isTeamAdmin);
	}
	
	public Member update(Member member, String id) {
		Member existingMember = memberRepository.findById(id).get();
		
		existingMember.setFirstName(member.getFirstName());
		existingMember.setLastName(member.getLastName());
		existingMember.setEmail(member.getEmail());
		existingMember.setIsOrgAdmin(member.getIsOrgAdmin());
		existingMember.setIsTeamAdmin(member.getIsTeamAdmin());
		existingMember.setTeamId(member.getTeamId());
		
		return memberRepository.save(existingMember);
	}

}
