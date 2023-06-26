package project.amall.wishlist.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import project.amall.product.dto.ProductDto;

@Mapper
public interface WishListMapper {

	List<ProductDto> showThisIdWishList(String memberId);

	
}
