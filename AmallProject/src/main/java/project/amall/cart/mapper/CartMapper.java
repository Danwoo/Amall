package project.amall.cart.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import project.amall.cart.dto.CartDto;
import project.amall.product.dto.ProductDto;

@Mapper
public interface CartMapper {

	List<CartDto> showMyCart(String memberId);
	
}
