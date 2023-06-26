package project.amall.cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.amall.cart.dto.CartDto;
import project.amall.cart.mapper.CartMapper;
import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;

@Service
public class CartService {
	
	@Autowired
	private CartMapper cartMapper;

	public List<CartDto> showMyCart(String memberId) {
		return cartMapper.showMyCart(memberId);
	}
	
}
