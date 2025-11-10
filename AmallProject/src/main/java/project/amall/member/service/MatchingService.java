package project.amall.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.amall.alarm.dto.AlarmDto;
import project.amall.alarm.service.AlarmService;
import project.amall.common.exception.BusinessException;
import project.amall.common.exception.code.ErrorCode;
import project.amall.member.dto.MemberDto;
import project.amall.member.dto.response.MatchingResponse;
import project.amall.member.mapper.MemberMapper;

/**
 * 커플 매칭 비즈니스 로직 서비스
 *
 * 기존 MemberController에 있던 복잡한 매칭 로직을 Service로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MemberMapper memberMapper;
    private final AlarmService alarmService;

    /**
     * 매칭 요청 전송
     *
     * @param myHash 내 해시 코드
     * @param yourHash 상대방 해시 코드
     * @throws BusinessException 매칭 요청 실패 시
     */
    @Transactional
    public void sendMatchingRequest(String myHash, String yourHash) {
        log.info("매칭 요청 전송 시작: myHash={}, yourHash={}", myHash, yourHash);

        // 1. 내 정보 조회
        MemberDto myDto = memberMapper.getDtoUseHash(myHash);
        if (myDto == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "본인 정보를 찾을 수 없습니다.");
        }

        // 2. 이미 매칭된 상태인지 확인
        if (myDto.getMemberMatchId() != null) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_MATCHED, "이미 매칭된 상태입니다.");
        }

        // 3. 상대방 정보 조회
        MemberDto yourDto = memberMapper.getDtoUseHash(yourHash);
        if (yourDto == null) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_HASH, "유효하지 않은 매칭 코드입니다.");
        }

        String myId = myDto.getMemberId();
        String yourId = yourDto.getMemberId();

        // 4. 자기 자신에게 매칭 요청 불가
        if (myId.equals(yourId)) {
            throw new BusinessException(ErrorCode.SELF_MATCHING_NOT_ALLOWED);
        }

        // 5. 상대방이 이미 매칭된 상태인지 확인
        if (yourDto.getMemberMatchId() != null) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_MATCHED, "상대방이 이미 매칭된 상태입니다.");
        }

        // 6. 이미 매칭 요청을 보냈는지 확인
        AlarmDto existingAlarm = alarmService.checkSend(myId, yourId);
        if (existingAlarm != null) {
            throw new BusinessException(ErrorCode.MATCHING_REQUEST_ALREADY_SENT);
        }

        // 7. 알람 ID 생성 및 매칭 메시지 전송
        String alarmId = yourDto.getMemberHash() + myDto.getMemberHash();
        alarmService.sendMatchingMassage(alarmId, myId, yourId);

        log.info("매칭 요청 전송 완료: from={} to={}", myId, yourId);
    }

    /**
     * 매칭 수락
     *
     * @param myHash 내 해시 코드
     * @param alarmId 알람 ID
     * @return MatchingResponse 매칭 결과
     * @throws BusinessException 매칭 수락 실패 시
     */
    @Transactional
    public MatchingResponse acceptMatching(String myHash, String alarmId) {
        log.info("매칭 수락 시작: myHash={}, alarmId={}", myHash, alarmId);

        // 1. 내 정보 조회
        MemberDto myDto = memberMapper.getDtoUseHash(myHash);
        if (myDto == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        String myId = myDto.getMemberId();

        // 2. 상대방 해시 코드 추출
        String yourHash = alarmId.replace(myHash, "");

        // 3. 상대방 정보 조회
        MemberDto yourDto = memberMapper.getDtoUseHash(yourHash);
        if (yourDto == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "상대방 정보를 찾을 수 없습니다.");
        }

        String yourId = yourDto.getMemberId();

        // 4. 매칭 처리
        memberMapper.updateMatchId(myId, yourId);
        memberMapper.updateMatchId(yourId, myId);

        // 5. 알람 삭제 (양쪽 모두)
        alarmService.deleteAllAlarm(myId, yourId);

        log.info("매칭 수락 완료: {}와 {} 매칭됨", myId, yourId);

        return MatchingResponse.of(myId, myDto.getMemberName(), yourId, yourDto.getMemberName());
    }

    /**
     * 매칭 해제 (추후 기능 확장용)
     *
     * @param memberId 회원 ID
     */
    @Transactional
    public void cancelMatching(String memberId) {
        log.info("매칭 해제: memberId={}", memberId);

        MemberDto member = memberMapper.reloadMemberData(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        String partnerId = member.getMemberMatchId();
        if (partnerId == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "매칭된 상대가 없습니다.");
        }

        // 양쪽 매칭 해제
        memberMapper.updateMatchId(memberId, null);
        memberMapper.updateMatchId(partnerId, null);

        log.info("매칭 해제 완료: {} <-> {}", memberId, partnerId);
    }
}
