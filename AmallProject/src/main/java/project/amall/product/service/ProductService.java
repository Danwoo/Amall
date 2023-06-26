package project.amall.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import project.amall.product.dto.ProductDto;
import project.amall.product.mapper.ProductMapper;

@Service
public class ProductService {
	
	@Autowired
	private ProductMapper productMapper;

	public List<ProductDto> showAllProductStore() {
		return productMapper.showAllProductStore();
	}

	public List<ProductDto> showProductDetail(String prodCode) {
		return productMapper.showProductDetail(prodCode);
	}

	public List<ProductDto> showMain(String needCategory) {
		return productMapper.showMain(needCategory);
	}

}
