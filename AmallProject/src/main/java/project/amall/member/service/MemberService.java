package project.amall.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입 처리
	 * - 패스워드를 BCrypt로 암호화하여 저장
	 *
	 * @param member 회원 정보 DTO
	 * @throws Exception 회원가입 처리 중 발생 가능한 예외
	 */
	public void signUpMember(MemberDto member) throws Exception {
		// 패스워드 암호화 (BCrypt)
		String encodedPassword = passwordEncoder.encode(member.getMemberPwd());
		member.setMemberPwd(encodedPassword);

		// DB에 저장
		memberMapper.signUpMember(member);
	}

	public MemberDto loginMember(MemberDto member) {
		return memberMapper.loginMember(member);
	}

	public void updateConnectDate(MemberDto member) {
		memberMapper.updateConnectDate(member);
	}

	/**
	 * 회원 정보 수정
	 * - 패스워드가 변경되는 경우 암호화 처리
	 *
	 * @param member 수정할 회원 정보 DTO
	 */
	public void updateMember(MemberDto member) {
		// 패스워드가 입력된 경우에만 암호화 (빈 문자열이 아닌 경우)
		if (member.getMemberPwd() != null && !member.getMemberPwd().isEmpty()) {
			// 이미 BCrypt 형식인지 확인 ($2a$, $2b$, $2y$ 로 시작)
			if (!member.getMemberPwd().startsWith("$2")) {
				String encodedPassword = passwordEncoder.encode(member.getMemberPwd());
				member.setMemberPwd(encodedPassword);
			}
		}

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
