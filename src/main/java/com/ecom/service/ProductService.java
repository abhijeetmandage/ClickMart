package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;

public interface ProductService
{
	
	public Product saveProduct(Product product);
	
	public List<Product> getAllProducts();
	
	public Boolean deleteProduct(int id);
	
	public Product getProductById(int id);
	
	public Product updateproduct(Product product,MultipartFile image);
	
	public List<Product> getAllActiveProduvts(String category);
	
	public List<Product> searchProduct(String ch);
	
	public Page<Product> getAllActiveProductPagination(Integer pageNo,Integer pageSize,String category);
	
	Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize);

	Page<Product> searchProductPagination(String ch, Integer pageNo, Integer pageSize);

}
