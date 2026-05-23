package com.ecom.service;

import com.ecom.model.Category;
import java.util.List;

public interface CategoryService 
{
	public Category saveCategory(Category category);
	
	public List<Category> getAllCategory();
	
	public boolean existCategory(String name);
	
	public Boolean deleteCategory(int id);
	
	public Category getCategory(int id);
	
	public List<Category> getAllActiveCategory();

}
