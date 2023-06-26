package project.amall.category.dto;

import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Data
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class CategoryDto {
	
	private int categoryNum;
	private String categoryName;
	private String categoryList;
	
}