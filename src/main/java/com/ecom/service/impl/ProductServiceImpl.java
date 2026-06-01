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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.controller.repository.ProductRepository;
import com.ecom.model.Product;
import com.ecom.service.ProductService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
		if(!ObjectUtils.isEmpty(saveProduct)) //changes old to save
		{
			if(!image.isEmpty()) 
			{
				try
				{
					String uploadDir = System.getProperty("user.home") 
			                 + File.separator + "ecom_uploads" 
			                 + File.separator + "product_img";
					Path uploadPath = Paths.get(uploadDir);
					if (!Files.exists(uploadPath)) 
						Files.createDirectories(uploadPath);
					Files.copy(image.getInputStream(), uploadPath.resolve(image.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
				}
				catch(Exception e)
				{
					System.out.println("error occured"+e.getMessage());
				}
			}
			return saveProduct; 
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

	@Override
	public List<Product> searchProduct(String ch)
	{
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize,String category) 
	{	
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product>pageProduct=null;
		
		if(ObjectUtils.isEmpty(category)) 		
			pageProduct=productRepository.findByIsActiveTrue(pageable);		
		else		
			pageProduct=productRepository.findByCategory(pageable,category);		
		return pageProduct;
	}
		
	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) 
	{
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findAll(pageable);
	}

	@Override
	public Page<Product> searchProductPagination(String ch, Integer pageNo, Integer pageSize) 
	{
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
	}
}
