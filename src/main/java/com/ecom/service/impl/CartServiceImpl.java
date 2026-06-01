package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.controller.repository.CartRepository;
import com.ecom.controller.repository.ProductRepository;
import com.ecom.controller.repository.UserRepository;
import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDetail;
import com.ecom.service.CartService;

@Service
public class CartServiceImpl implements CartService
{
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ProductRepository productRepository;
	

	@Override
	public Cart saveCart(int productId, int userId) 
	{
		
		UserDetail cartUserDetail=userRepository.findById(userId).get();
		Product cartProduct=productRepository.findById(productId).get();
		
		Cart cartStatus=cartRepository.findByCartProductIdAndCartUserId(productId, userId);
		
		Cart cart=null;
		
		if(ObjectUtils.isEmpty(cartStatus)) 
		{
			cart=new Cart();
			cart.setCartProduct(cartProduct);
			cart.setCartUser(cartUserDetail);
			cart.setQuantity(1);
			cart.setTotalPrice(1*cartProduct.getDiscountPrice());
		}
		else 
		{
			cart=cartStatus;
			cart.setQuantity(cart.getQuantity()+1);
			cart.setTotalPrice(cart.getQuantity()*cart.getCartProduct().getDiscountPrice());
		}	
		Cart saveCart=cartRepository.save(cart);
		return saveCart;
	}

	@Override
	public List<Cart> getCartsByUser(int userId) 
	{
		List<Cart> carts = cartRepository.findByCartUserId(userId);
		
		Double totalOrderPrice=0.0;
		
		List<Cart> updatedCarts=new ArrayList<>();
		
		for(Cart c:carts) 
		{
			Double totalPrice=(c.getCartProduct().getDiscountPrice()*c.getQuantity());
			c.setTotalPrice(totalPrice);
			totalOrderPrice+=totalPrice;
			c.setTotalOrderPrice(totalOrderPrice);
			updatedCarts.add(c);
		}
		return updatedCarts;
	}

	@Override
	public Integer getCountCart(Integer cartUserId) 
	{
		Integer countByUserId=cartRepository.countByCartUserId(cartUserId);
		return countByUserId;
	}

	@Override
	public void UpadateCartQuantity(String sy, Integer cid)
	{
		
		Cart cartFindById=cartRepository.findById(cid).get();
		int updatedQuantity;
		
		if(sy.equalsIgnoreCase("de")) 
		{
			updatedQuantity=cartFindById.getQuantity()-1;
			if(updatedQuantity<=0) 			
				cartRepository.deleteById(cid);
			else
			{
				cartFindById.setQuantity(updatedQuantity);
				cartRepository.save(cartFindById);
			}
		}
		else
		{
			updatedQuantity=cartFindById.getQuantity()+1;
			cartFindById.setQuantity(updatedQuantity);
			cartRepository.save(cartFindById);
		}
	}
}
