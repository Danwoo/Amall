package project.amall.alarm.service;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.amall.alarm.dto.AlarmDto;
import project.amall.alarm.mapper.AlarmMapper;
import project.amall.common.constants.AppConstants;

/**
 * 알림 서비스
 *
 * 매칭 알림, 선물 알림 등 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmService {

	private final AlarmMapper alarmMapper;

	/**
	 * 선물 알림 ID 접두사
	 */
	private static final String GIFT_ALARM_PREFIX = "GIFT-";

	/**
	 * 매칭 메시지 전송
	 *
	 * @param alarmId 알림 ID
	 * @param myId 발신자 ID
	 * @param matchId 수신자 ID
	 */
	@Transactional
	public void sendMatchingMassage(String alarmId, String myId, String matchId) {
		log.info("매칭 알림 전송: alarmId={}, from={}, to={}", alarmId, myId, matchId);
		alarmMapper.sendMatchingMassage(alarmId, myId, matchId);
		log.debug("매칭 알림 전송 완료");
	}

	/**
	 * 매칭 알림 확인
	 *
	 * @param myId 내 ID
	 * @param matchId 상대방 ID
	 * @return 알림 정보
	 */
	public AlarmDto checkSend(String myId, String matchId) {
		log.debug("매칭 알림 확인: myId={}, matchId={}", myId, matchId);
		return alarmMapper.CheckSend(myId, matchId);
	}

	/**
	 * 내 알림 목록 조회
	 *
	 * @param memberId 회원 ID
	 * @return 알림 목록
	 */
	public List<Map<String, Object>> getMyAlarm(String memberId) {
		log.debug("알림 목록 조회: memberId={}", memberId);
		List<Map<String, Object>> alarms = alarmMapper.getMyAlarm(memberId);
		log.info("알림 목록 조회 완료: memberId={}, count={}", memberId, alarms.size());
		return alarms;
	}

	/**
	 * 모든 알림 삭제
	 *
	 * @param myId 내 ID
	 * @param yourId 상대방 ID
	 */
	@Transactional
	public void deleteAllAlarm(String myId, String yourId) {
		log.info("알림 전체 삭제: myId={}, yourId={}", myId, yourId);
		alarmMapper.deleteMyPostAlarm(myId);
		alarmMapper.deleteMyPostAlarm(yourId);
		log.debug("알림 전체 삭제 완료");
	}

	/**
	 * 선물 알림 생성
	 *
	 * 선물 주문 생성 시 선물 받는 사람에게 알림 전송
	 *
	 * @param orderId 주문 ID
	 * @param senderId 선물 보낸 사람 ID
	 * @param receiverId 선물 받는 사람 ID
	 */
	@Transactional
	public void createGiftNotification(String orderId, String senderId, String receiverId) {
		log.info("선물 알림 생성: orderId={}, from={}, to={}", orderId, senderId, receiverId);

		// ALARM_ID에 타입 정보 포함: "GIFT-{orderId}"
		String alarmId = GIFT_ALARM_PREFIX + orderId;
		alarmMapper.sendMatchingMassage(alarmId, senderId, receiverId);

		log.info("선물 알림 생성 완료: alarmId={}", alarmId);
	}

}
