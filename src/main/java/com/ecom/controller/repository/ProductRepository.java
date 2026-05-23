package com.ecom.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Product;

public interface ProductRepository extends JpaRepository<Product,Integer> {

	public List<Product> findByIsActiveTrue();

	public List<Product> findByCategoryAndIsActiveTrue(String category);

}
