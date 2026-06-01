package com.ecom.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Cart;

public interface CartRepository extends JpaRepository<Cart,Integer>
{

	public Cart findByCartProductIdAndCartUserId(int pid,int uid);

	public Integer countByCartUserId(Integer cartUserId);
	
	public List<Cart> findByCartUserId(Integer cartUserId);
}
