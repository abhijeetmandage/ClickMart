package com.ecom.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder,Integer>
{

	public ProductOrder save(ProductOrder order);

	public List<ProductOrder> findByUserDetailId(Integer userid);
	
	public ProductOrder findByOrderId(String orderId);

}
