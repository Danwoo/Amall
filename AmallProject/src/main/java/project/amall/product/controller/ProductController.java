package project.amall.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import project.amall.product.service.ProductService;

@Controller
public class ProductController {
	
	@Autowired
	private ProductService productService;
	
}
      
