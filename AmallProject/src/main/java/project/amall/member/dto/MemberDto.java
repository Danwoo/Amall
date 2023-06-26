package project.amall.member.dto;

import org.springframework.context.annotation.PropertySource;

import lombok.Data;
import project.amall.cart.dto.CartDto;

@Data
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class MemberDto {
	
	private String memberId;	
	private String memberName;
	private String memberBirth;
	private String memberPwd;
	private String memberEmail;
	private String memberPhone;
	private int memberSex;
	private String memberRegDate;
	private String memberConnectDate;
	private String memberHash;
	private String memberMatchId;
	private String memberPostCode;
	private String memberAddress;
	private String memberDetailAddress;
	private String memberExtraAddress;
	
	private CartDto cartDto;
	
}