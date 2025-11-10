package project.amall.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.member.dto.MemberDto;
import project.amall.member.mapper.MemberMapper;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입 처리
	 * - 패스워드를 BCrypt로 암호화하여 저장
	 *
	 * @param member 회원 정보 DTO
	 * @throws BusinessException 입력 검증 실패 또는 중복 ID인 경우
	 */
	@Transactional
	public void signUpMember(MemberDto member) {
		log.debug("회원가입 요청: memberId={}", member.getMemberId());

		// 입력 검증
		validateMemberInput(member, true);

		// 중복 ID 체크
		if (!isIdAvailable(member.getMemberId())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
				"이미 사용 중인 회원 ID입니다: " + member.getMemberId());
		}

		// 패스워드 암호화 (BCrypt)
		String encodedPassword = passwordEncoder.encode(member.getMemberPwd());
		member.setMemberPwd(encodedPassword);

		// DB에 저장
		memberMapper.signUpMember(member);
		log.info("회원가입 완료: memberId={}", member.getMemberId());
	}

	/**
	 * @deprecated Spring Security의 인증 메커니즘 사용 권장
	 */
	@Deprecated
	public MemberDto loginMember(MemberDto member) {
		log.warn("Deprecated method called: loginMember() - Use Spring Security authentication");
		return memberMapper.loginMember(member);
	}

	/**
	 * 회원 최종 접속일시 업데이트
	 *
	 * @param member 회원 정보 DTO
	 * @throws BusinessException 회원 정보가 null인 경우
	 */
	@Transactional
	public void updateConnectDate(MemberDto member) {
		if (member == null || member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 정보가 올바르지 않습니다.");
		}
		log.debug("접속일시 업데이트: memberId={}", member.getMemberId());
		memberMapper.updateConnectDate(member);
	}

	/**
	 * 회원 정보 수정
	 * - 패스워드가 변경되는 경우 암호화 처리
	 *
	 * @param member 수정할 회원 정보 DTO
	 * @throws BusinessException 입력 검증 실패 시
	 */
	@Transactional
	public void updateMember(MemberDto member) {
		log.debug("회원 정보 수정: memberId={}", member.getMemberId());

		// 입력 검증 (패스워드는 선택사항)
		validateMemberInput(member, false);

		// 패스워드가 입력된 경우에만 암호화 (빈 문자열이 아닌 경우)
		if (member.getMemberPwd() != null && !member.getMemberPwd().isEmpty()) {
			// 이미 BCrypt 형식인지 확인 ($2a$, $2b$, $2y$ 로 시작)
			if (!member.getMemberPwd().startsWith("$2")) {
				String encodedPassword = passwordEncoder.encode(member.getMemberPwd());
				member.setMemberPwd(encodedPassword);
			}
		}

		memberMapper.updateMember(member);
		log.info("회원 정보 수정 완료: memberId={}", member.getMemberId());
	}

	/**
	 * 회원 탈퇴
	 *
	 * @param member 회원 정보 DTO
	 * @throws BusinessException 회원 정보가 null이거나 ID가 없는 경우
	 */
	@Transactional
	public void deleteMember(MemberDto member) {
		if (member == null || member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 정보가 올바르지 않습니다.");
		}
		log.debug("회원 탈퇴: memberId={}", member.getMemberId());
		memberMapper.deleteMember(member);
		log.info("회원 탈퇴 완료: memberId={}", member.getMemberId());
	}

	/**
	 * 회원 ID로 회원 정보 조회
	 *
	 * @param memberId 회원 ID
	 * @return 회원 정보 (없으면 null)
	 * @throws BusinessException memberId가 null이거나 빈 문자열인 경우
	 */
	public MemberDto reloadMemberData(String memberId) {
		if (memberId == null || memberId.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 ID가 필요합니다.");
		}
		log.debug("회원 정보 조회: memberId={}", memberId);
		return memberMapper.reloadMemberData(memberId);
	}

	/**
	 * 매칭 해시로 회원 정보 조회
	 *
	 * @param memberMatchHash 매칭 해시
	 * @return 회원 정보 (없으면 null)
	 * @throws BusinessException memberMatchHash가 null이거나 빈 문자열인 경우
	 */
	public MemberDto getDtoUseHash(String memberMatchHash) {
		if (memberMatchHash == null || memberMatchHash.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "매칭 해시가 필요합니다.");
		}
		log.debug("회원 정보 조회 (해시): memberMatchHash={}", memberMatchHash);
		return memberMapper.getDtoUseHash(memberMatchHash);
	}

	/**
	 * @deprecated MatchingService.acceptMatching() 사용 권장
	 */
	@Deprecated
	@Transactional
	public void finishMatching(String myHash, String myId, String yourHash, String yourId) {
		log.warn("Deprecated method called: finishMatching() - Use MatchingService.acceptMatching()");
		memberMapper.updateMatchId(myId, yourId);
		memberMapper.updateMatchId(yourId, myId);
	}

	/**
	 * 회원 ID 중복 체크
	 *
	 * @param memberId 체크할 회원 ID
	 * @return 중복 여부 (true: 사용 가능, false: 이미 존재)
	 * @throws BusinessException memberId가 null이거나 빈 문자열인 경우
	 */
	public boolean isIdAvailable(String memberId) {
		if (memberId == null || memberId.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 ID가 필요합니다.");
		}
		log.debug("회원 ID 중복 체크: memberId={}", memberId);
		MemberDto existing = memberMapper.reloadMemberData(memberId);
		return existing == null;
	}

	/**
	 * 회원 조회 (예외 던지기)
	 *
	 * @param memberId 회원 ID
	 * @return MemberDto
	 * @throws BusinessException 회원을 찾을 수 없는 경우
	 */
	public MemberDto getMemberOrThrow(String memberId) {
		if (memberId == null || memberId.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 ID가 필요합니다.");
		}
		log.debug("회원 조회 (예외): memberId={}", memberId);
		MemberDto member = memberMapper.reloadMemberData(memberId);
		if (member == null) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "회원을 찾을 수 없습니다: memberId=" + memberId);
		}
		return member;
	}

	// ========== Private Helper Methods ==========

	/**
	 * 회원 정보 입력 검증
	 *
	 * @param member 회원 정보 DTO
	 * @param requirePassword 패스워드 필수 여부
	 * @throws BusinessException 입력 검증 실패 시
	 */
	private void validateMemberInput(MemberDto member, boolean requirePassword) {
		if (member == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 정보가 null입니다.");
		}

		if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 ID는 필수입니다.");
		}

		if (requirePassword) {
			if (member.getMemberPwd() == null || member.getMemberPwd().trim().isEmpty()) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "패스워드는 필수입니다.");
			}
			if (member.getMemberPwd().length() < 4) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "패스워드는 최소 4자 이상이어야 합니다.");
			}
		}

		// 추가 검증: 회원 이름, 이메일 등 (필요시)
		log.debug("회원 정보 검증 완료: memberId={}", member.getMemberId());
	}

}
