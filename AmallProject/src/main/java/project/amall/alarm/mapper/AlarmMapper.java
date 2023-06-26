package project.amall.alarm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import project.amall.alarm.dto.AlarmDto;

@Mapper
public interface AlarmMapper {

	void sendMatchingMassage(@Param("alarmId") String alarmId, @Param("myId") String myId, @Param("matchId") String matchId);

	AlarmDto CheckSend(@Param("myId") String myId, @Param("matchId") String matchId);

	List<Map<String, Object>> getMyAlarm(String memberId);

	void deleteMyPostAlarm(String Id);
	
}
