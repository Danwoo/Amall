package project.amall.wishlist.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;
import project.amall.wishlist.mapper.WishListMapper;

@Service
public class WishListService {
	
	@Autowired
	private WishListMapper wishListMapper;

	public List<ProductDto> showThisIdWishList(String memberId) {
		return wishListMapper.showThisIdWishList(memberId);
	}

}
