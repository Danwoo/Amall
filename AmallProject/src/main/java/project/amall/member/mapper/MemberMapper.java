package project.amall.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import project.amall.member.dto.MemberDto;

@Mapper
public interface MemberMapper {
	
	void signUpMember(MemberDto member);

	MemberDto loginMember(MemberDto member);

	void updateConnectDate(MemberDto member);

	void updateMember(MemberDto member);

	void deleteMember(MemberDto member);

	MemberDto reloadMemberData(String memberId);

	MemberDto getDtoUseHash(String memberMatchHash);
	
	void updateMatchId(@Param("myId") String myId, @Param("yourId") String yourId);

}
