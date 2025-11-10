package project.amall.alarm.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.amall.alarm.dto.AlarmDto;
import project.amall.alarm.mapper.AlarmMapper;

@Service
public class AlarmService {
	
	@Autowired
	private AlarmMapper alarmMapper;

	public void sendMatchingMassage(String alarmId, String myId, String matchId) {
		alarmMapper.sendMatchingMassage(alarmId, myId, matchId);
	}

	public AlarmDto CheckSend(String myId, String matchId) {
		return alarmMapper.CheckSend(myId, matchId);
	}

	public List<Map<String, Object>> getMyAlarm(String memberId) {
		return alarmMapper.getMyAlarm(memberId);
	}

	public void deleteAllAlarm(String myId, String yourId) {
		alarmMapper.deleteMyPostAlarm(myId);
		alarmMapper.deleteMyPostAlarm(yourId);
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
	public void createGiftNotification(String orderId, String senderId, String receiverId) {
		// ALARM_ID에 타입 정보 포함: "GIFT-{orderId}"
		String alarmId = "GIFT-" + orderId;
		alarmMapper.sendMatchingMassage(alarmId, senderId, receiverId);
	}

}
