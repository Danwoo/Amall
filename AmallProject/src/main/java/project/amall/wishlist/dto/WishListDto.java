package project.amall.wishlist.dto;

import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Data
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class WishListDto {
	
	private int wishlistId;
	private String wishlistRegdate;
	private String memberId;
	private int prodNum;
	
}