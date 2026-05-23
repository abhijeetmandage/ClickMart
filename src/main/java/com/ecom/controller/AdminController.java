package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")

public class AdminController 
{
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@GetMapping("/")
	public String index() 
	{
		return "admin/index";
	}
	
	@GetMapping("/add_product")
	public String add_product(Model m) 
	{
		List<Category> categories=categoryService.getAllCategory();
		m.addAttribute("category",categories);
		return "admin/add_product";
	}
	
	@GetMapping("/category")
	public String category(Model m)
	{
		m.addAttribute("category",categoryService.getAllCategory());
		return "admin/category";
	}
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file,HttpSession session) throws IOException
	{
		String imageNmae=file!=null?file.getOriginalFilename():"default.jpg";
		category.setImageName(imageNmae);
		if(categoryService.existCategory(category.getName())) 
			session.setAttribute("errorMsg", "category name already exists");
		else 
		{
			Category saveCategory=categoryService.saveCategory(category);
			if(ObjectUtils.isEmpty(saveCategory))
				session.setAttribute("errorMsg", "Not saved internal server error");
			else 
			{
				File saveFile=new ClassPathResource("static/img").getFile();//get path of img folder
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());//get full path
				//System.out.println(path);//full path 
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);//save img
				session.setAttribute("succMsg", " saved succsefull");
			}
		}
		
		return "redirect:/admin/category";
	}
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id,HttpSession session) 
	{
		Boolean delcategory=categoryService.deleteCategory(id);
		if(delcategory)
			session.setAttribute("succMsg", "category delete sucssefully");
		else 
			session.setAttribute("errorMsg", "Somthing wrong on server");
		
		return "redirect:/admin/category";
	}
	
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id,Model m)
	{
		m.addAttribute("category",categoryService.getCategory(id));
		return "admin/edit_category";
	}
	
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file,HttpSession session) throws IOException
	{
		Category oldCategory=categoryService.getCategory(category.getId());
		String imageName=file.isEmpty()?oldCategory.getImageName():file.getOriginalFilename();
		if(!ObjectUtils.isEmpty(category)) 
		{
			oldCategory.setName(category.getName());
			oldCategory.setImageName(imageName);
			oldCategory.setIsActive(category.getIsActive());
		}
		Category updateCategory=categoryService.saveCategory(oldCategory);
		if(!ObjectUtils.isEmpty(updateCategory)) 
		{
			if(!file.isEmpty()) 
			{
				File saveFile=new ClassPathResource("static/img").getFile();//get path of img folder
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());//get full path
				//System.out.println(path);//full path 
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);//save img
			}
			
			session.setAttribute("succMsg", "Category update successfully");
		}
		else 
			session.setAttribute("errorMsg", "Somthing wrong on server");
		
		return "redirect:/admin/loadEditCategory/"+category.getId();
	}
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product,HttpSession session,@RequestParam("file") MultipartFile image) throws IOException
	{
		String imagename=(image.isEmpty())?"default.pnj":image.getOriginalFilename();
		product.setImage(imagename);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		Product saveProduct=productService.saveProduct(product);
		if(!ObjectUtils.isEmpty(saveProduct)) {
			File saveFile=new ClassPathResource("static/img").getFile();//get path of img folder
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());//get full path
			//System.out.println(path);//full path 
			Files.copy(image.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);//save img 
			session.setAttribute("succMsg", "Product save successfully");
		}
		else
			session.setAttribute("erroeMsg", "Somthing wrong on server");
		
		return "redirect:/admin/add_product";
	}
	
	@GetMapping("/products")
	public String loadViewProduct(Model m) 
	{
		m.addAttribute("products",productService.getAllProducts());
		return "admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id,HttpSession session) 
	{
		Boolean deleteProduct=productService.deleteProduct(id);
		if(deleteProduct) 
		{
			session.setAttribute("succMsg","Product deleted sucssefully");
		}
		else 
		{
			session.setAttribute("errorMsg","Somthing  wrong on server");
		}
		return "redirect:/admin/products";
	}
	
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id,Model m) 
	{
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("category", categoryService.getCategory(id));
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image,HttpSession session)
	{
		if(product.getDiscount()<0 || product.getDiscount()>100) 
		{
			session.setAttribute("errorMsg","Enter correct discount price");
		}
		else
		{
			Product updatedproduct=productService.updateproduct(product,image);
			if(!ObjectUtils.isEmpty(updatedproduct)) 
				session.setAttribute("succMsg","Product updated sucssefully");
			else
				session.setAttribute("errorMsg","Something happen in server");
		}
		
		return "redirect:/admin/editProduct/"+ product.getId();
	}
}
