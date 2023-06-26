package project.amall.product.dto;

import org.springframework.context.annotation.PropertySource;

import lombok.Data;
import project.amall.category.dto.CategoryDto;

@Data
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class ProductDto {
	
	private int prodNum;	
	private String prodName;
	private String prodCode;
	private int prodPrice;
	private int prodStock;
	private String prodExplanation;
	private String prodRegDate;
	private int prodHit;
	private String prodDetail;
	private String prodVal1;
	private String prodVal2;
	private String prodVal3;
	private int prodStar;
	private int prodShow;
	private String sellerId;
	private int categoryId;
	private String prodImage1;
	private String prodImage2;
	private String prodImage3;
	private int prodDeliveryfee;
	
	private CategoryDto categoryDto;
	
}