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

}
