package project.amall.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;

@Service
public class MemberService {
	
	@Autowired
	private MemberMapper memberMapper;
	
	public void signUpMember(MemberDto member) throws Exception {
		memberMapper.signUpMember(member);
	}

	public MemberDto loginMember(MemberDto member) {
		return memberMapper.loginMember(member);
	}

	public void updateConnectDate(MemberDto member) {
		memberMapper.updateConnectDate(member);
	}

	public void updateMember(MemberDto member) {
		memberMapper.updateMember(member);
	}

	public void deleteMember(MemberDto member) {
		memberMapper.deleteMember(member);
	}

	public MemberDto reloadMemberData(String memberId) {
		return memberMapper.reloadMemberData(memberId);
	}

	public MemberDto getDtoUseHash(String memberMatchHash) {
		return memberMapper.getDtoUseHash(memberMatchHash);
	}

	public void finishMatching(String myHash, String myId, String yourHash, String yourId) {
		memberMapper.updateMatchId(myId, yourId);
		memberMapper.updateMatchId(yourId, myId);
	}

}
