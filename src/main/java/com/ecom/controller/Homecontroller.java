package com.ecom.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import org.springframework.ui.Model;

@Controller
public class Homecontroller 
{
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@GetMapping("/")
	public String index() 
	{
		return "index";
	}
	
	@GetMapping("/base")
	public String base() 
	{
		return "base";
	}
	
	@GetMapping("/login")
	public String login() 
	{
		return "login";
	}
	
	@GetMapping("/register")
	public String register() 
	{
		return "register";
	}
	
	@GetMapping("/product")
	public String product(Model m,@RequestParam(value="category", defaultValue="") String category) 
	{
		List<Category>categoreis=categoryService.getAllActiveCategory();
		List<Product>products=productService.getAllActiveProduvts(category);
		m.addAttribute("categoreis", categoreis);
		m.addAttribute("products", products);
		m.addAttribute("paramValue", category);
		return "product";
	}
	
	@GetMapping("/view_product/{id}")
	public String view_product(@PathVariable int id,Model m) 
	{
		Product productById=productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}
}
