package com.ecom.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

import com.ecom.controller.repository.CategoryRepository;
import com.ecom.model.Category;
import com.ecom.service.CategoryService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class CategoryServiceImpl implements CategoryService 
{
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Override
	public Category saveCategory(Category category) 
	{		
		return categoryRepository.save(category);
	}

	@Override
	public List<Category> getAllCategory() 
	{
		return categoryRepository.findAll();
	}

	@Override
	public boolean existCategory(String name) 
	{
		return categoryRepository.existsByName(name);
	}

	@Override
	public Boolean deleteCategory(int id) 
	{
		Category category=categoryRepository.findById(id).orElse(null);
		if(!ObjectUtils.isEmpty(category)) 
		{
			categoryRepository.delete(category);
			return true;
		}		
		return false;
	}

	@Override
	public Category getCategory(int id) 
	{
		Category category=categoryRepository.findById(id).orElse(null);
		return category;
	}

	@Override
	public List<Category> getAllActiveCategory() 
	{
		List<Category> categories=categoryRepository.findByIsActiveTrue();
		return categories;
	}

	@Override
	public Page<Category> getAllCategoryPagination(int pageNo, int pageSize)
	{
	    Pageable pageable = PageRequest.of(pageNo, pageSize);
	    return categoryRepository.findAll(pageable);
	}
}
