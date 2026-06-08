package com.ecom.controller.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Category;
import com.ecom.model.Product;

public interface ProductRepository extends JpaRepository<Product,Integer> 
{

	public List<Product> findByIsActiveTrue();

	public List<Product> findByCategoryAndIsActiveTrue(String category);
	
	public List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch,String ch2);

	public Page<Product> findByIsActiveTrue(Pageable pageable);

	public Page<Product> findByCategory(Pageable pageable,String category);
	
	public Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String title,String category,Pageable pageable);
	
	Page<Product> findByCategoryAndIsActiveTrue(String category, Pageable pageable);

}
