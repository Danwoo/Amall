package project.amall.product.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import project.amall.product.dto.ProductDto;

@Mapper
public interface ProductMapper {
	
	List<ProductDto> showAllProductStore();

	List<ProductDto> showProductDetail(String prodCode);

	List<ProductDto> showMain(String needCategory);
	
}