package project.amall.cart.dto;

import org.springframework.context.annotation.PropertySource;

import lombok.Data;
import project.amall.product.dto.ProductDto;

@Data
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class CartDto {
	
	private int cartId;
	private int cartQuantity;
	private String cartRegDate;
	private String memberId;
	private int prodNum;
	
	private ProductDto productDto;
	
}