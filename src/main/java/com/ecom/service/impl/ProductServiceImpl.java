package com.ecom.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.controller.repository.ProductRepository;
import com.ecom.model.Product;
import com.ecom.service.ProductService;
@Service
public class ProductServiceImpl implements ProductService 
{

	@Autowired
	private ProductRepository productRepository;
	
	@Override
	public Product saveProduct(Product product) 
	{
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts()
	{
		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(int id) 
	{
		Product product=productRepository.findById(id).orElse(null);
		if(!ObjectUtils.isEmpty(product)) 
		{
			productRepository.delete(product);
			return true;
		}
		return false;
	}

	@Override
	public Product getProductById(int id) 
	{
		Product product=productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public Product updateproduct(Product product, MultipartFile image) 
	{
		Product oldproduct=getProductById(product.getId());
		String imageName=image.isEmpty()?oldproduct.getImage():image.getOriginalFilename();
		
		oldproduct.setTitle(product.getTitle());
		oldproduct.setStock(product.getStock());
		oldproduct.setPrice(product.getPrice());
		oldproduct.setDescription(product.getDescription());
		oldproduct.setCategory(product.getCategory());
		oldproduct.setImage(imageName);
		oldproduct.setDiscount(product.getDiscount());
		oldproduct.setIsActive(product.getIsActive());
		
		Double discount=product.getPrice()*(product.getDiscount()/100.0);
		Double discountPrice=product.getPrice()-discount;
		oldproduct.setDiscountPrice(discountPrice);
		
		Product saveProduct=productRepository.save(oldproduct);
		if(!ObjectUtils.isEmpty(oldproduct)) 
		{
			if(!image.isEmpty()) 
			{
				try
				{
					File saveFile=new ClassPathResource("static/img").getFile();//get path of img folder
					Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());//get full path
					System.out.println(path);//full path 
					Files.copy(image.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);//save img
				}
				catch(Exception e)
				{
					System.out.println("error occured"+e.getMessage());
				}
			}
			return product;
		}
		return null;
	}

	@Override
	public List<Product> getAllActiveProduvts(String category) 
	{
		List<Product>products=null;
		if(ObjectUtils.isEmpty(category)) 
		{
			products=productRepository.findByIsActiveTrue();
		}
		else
		{
			products=productRepository.findByCategoryAndIsActiveTrue(category);
		}
		
		return products;
	}
}
