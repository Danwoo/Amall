package project.amall.alarm.dto;

import org.springframework.context.annotation.PropertySource;

import lombok.Data;
import project.amall.member.dto.MemberDto;

@Data
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class AlarmDto {
	
	private String alarmId;
	private String senderId;
	private String getterId;
	
	private MemberDto memberDto;
	
}