package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDetail;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommanUtil;
import com.ecom.util.OrderStatus;

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
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommanUtil commanUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m)
	{
		if(p!=null)
		{
			String email=p.getName();
			UserDetail userDetail=userService.getUserByEmail(email);
			m.addAttribute("user", userDetail);
			Integer countCart=cartService.getCountCart(userDetail.getId());
			m.addAttribute("countCart", countCart);
		}
		
		List<Category> allActivecategory=categoryService.getAllActiveCategory();
		m.addAttribute("activeCategories", allActivecategory);
	}
	
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
	public String category(Model m,@RequestParam(defaultValue = "0") Integer pageNo,@RequestParam(defaultValue = "5") Integer pageSize) 
	{

	    Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
	    List<Category> categories = page.getContent();

	    m.addAttribute("categories", categories);
	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());
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
				saveImageToUploadDir(file, "category_img");
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
				saveImageToUploadDir(file, "category_img");
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
		String imagename=(image.isEmpty())?"default.png":image.getOriginalFilename();
		product.setImage(imagename);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		Product saveProduct=productService.saveProduct(product);
		if(!ObjectUtils.isEmpty(saveProduct)) 
		{ 
			saveImageToUploadDir(image, "product_img");
			session.setAttribute("succMsg", "Product save successfully");
		}
		else
			session.setAttribute("erroeMsg", "Somthing wrong on server");
		
		return "redirect:/admin/add_product";
	}
		
	@GetMapping("/products")
	public String loadViewProduct(Model m,@RequestParam(defaultValue = "") String ch,@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,@RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize)
	{
		Page<Product> page = null;

		if (ch != null && ch.length() > 0) 		
			page = productService.searchProductPagination(ch, pageNo, pageSize);		
		else 		
			page = productService.getAllProductsPagination(pageNo, pageSize);		

		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("ch", ch);
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id,HttpSession session) 
	{
		Boolean deleteProduct=productService.deleteProduct(id);
		if(deleteProduct) 		
			session.setAttribute("succMsg","Product deleted sucssefully");		
		else 		
			session.setAttribute("errorMsg","Somthing  wrong on server");		
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
	
	@GetMapping("/users")
	public String getAllUsers(Model m,@RequestParam Integer type) 
	{
		List<UserDetail> users=null;
		if(type==1) 
			users=userService.getUsers("ROLE_USER");		
		else		
			users=userService.getUsers("ROLE_ADMIN");
				
		m.addAttribute("userType", type);
		m.addAttribute("users", users);
		return "/admin/users"; 
	}
	
	@GetMapping("/updateStatus")
	public String updateuserAccountStatus(@RequestParam Boolean status,@RequestParam int id,@RequestParam Integer type,HttpSession session) 
	{
		Boolean u=userService.upadateAcountStatus(id, status);
		if(u) 
			session.setAttribute("succMsg","Account status updated successfully");
		else 
			session.setAttribute("errorMsg","Something wrong on server");		
		return "redirect:/admin/users?type="+type;
	}
	
	//fixing the images not apear bug
	private void saveImageToUploadDir(MultipartFile file, String subFolder) throws IOException 
	{
	    String uploadDir = System.getProperty("user.home") 
	                     + File.separator + "ecom_uploads" 
	                     + File.separator + subFolder;
	    Path uploadPath = Paths.get(uploadDir);
	    if (!Files.exists(uploadPath))
	    {
	        Files.createDirectories(uploadPath); // creates folder automatically
	    }
	    Files.copy(file.getInputStream(), 
	               uploadPath.resolve(file.getOriginalFilename()), 
	               StandardCopyOption.REPLACE_EXISTING);
	}
	//end 
	
	@GetMapping("/orders")
	public String getAllOrders(Model m,@RequestParam(name="pageNo",defaultValue="0") Integer pageNo,@RequestParam(name="pageSize",defaultValue="8") Integer pageSize)
	{
		
		Page<ProductOrder> page=orderService.getAllOrdersPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch", false);
		
		m.addAttribute("pageNo",page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements",page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		return "/admin/orders"; 
	}
	
	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,HttpSession session) throws Exception {
		
		OrderStatus[] values=OrderStatus.values();
		String status=null;
		
		for(OrderStatus orderst:values) {
			if(orderst.getId().equals(st))
				status=orderst.getName();
		}
		
		ProductOrder updateOrder =orderService.updateOrderStatus(id, status);
		
		commanUtil.sendMailForProductOrder(updateOrder, status);
		
		if(!ObjectUtils.isEmpty(updateOrder))
			session.setAttribute("succMsg", "Status Updated");
		else
			session.setAttribute("errorMsg", "Status not updated");
		return "redirect:/admin/orders";
	}
	
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String porderId,Model m,HttpSession session,@RequestParam(name="pageNo",defaultValue="0") Integer pageNo,
			@RequestParam(name="pageSize",defaultValue="8") Integer pageSize) 
	{	
		if(porderId!=null && porderId.length()>0) 
		{			
			ProductOrder orderSearch=orderService.getOrdersByOrderId(porderId.trim());		
			if(ObjectUtils.isEmpty(orderSearch)) {
				session.setAttribute("errorMsg", "Incorect orderId");
				m.addAttribute("orderDetails", null);
			}
			else 
			{
				m.addAttribute("orderDetails", orderSearch);
			}
			m.addAttribute("srch", true);
		}
		else 
		{
			Page<ProductOrder>page=orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page.getContent());
			m.addAttribute("srch", false);
			
			m.addAttribute("pageNo",page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements",page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
		}
		return "/admin/orders";
	}	
	
	@GetMapping("/add-admin")
	public String loadAdminPage()
	{
		return "/admin/add_admin";
	}
	
	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDetail user,@RequestParam("img") MultipartFile file,HttpSession session ) throws IOException 
	{
		String imgName=file.isEmpty()?"default.png":file.getOriginalFilename();
		user.setProfileImage(imgName);
		UserDetail saveUser=userService.saveAdmin(user);
		if(!ObjectUtils.isEmpty(saveUser)) 
		{
			if(!file.isEmpty()) 
			{
				saveImageToUploadDir(file, "Profile_img");
			}
			session.setAttribute("succMsg", "Data save successfully");
		}
		else
		{
			session.setAttribute("errorMsg", "Somthing wrong on server");
		}
		return "redirect:/admin/add-admin";
	}
	
	@GetMapping("/profile")
	public String profile()
	{
		return "/admin/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDetail user,@RequestParam MultipartFile img,HttpSession session)
	{
		UserDetail updatedProfile=userService.updateUserProfile(user, img);
		
		if(ObjectUtils.isEmpty(updatedProfile))
		{
			session.setAttribute("errorMsg", "Profile not updated");
		}else 
		{
			session.setAttribute("succMsg", "Profile Updated Successfully");
		}
		return  "redirect:/admin/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,HttpSession session)
	{
		UserDetail loggedUserDetails=commanUtil.getLoggedInUserDetails(p);
		
		boolean matches=passwordEncoder.matches(currentPassword, loggedUserDetails.getPassword());
		
		if(matches) 
		{
			String ecodedPassword=passwordEncoder.encode(newPassword);
			loggedUserDetails.setPassword(ecodedPassword);
			UserDetail updateUser=userService.updateUser(loggedUserDetails);
			if(ObjectUtils.isEmpty(updateUser))
			{
				session.setAttribute("errorMsg", "Password not changes. Error in Server");
			}
			else 
			{
				session.setAttribute("succMsg", "Password change successfully");
			}
		}
		else 
		{
			session.setAttribute("errorMsg", "Current Password incorect");
		}
		return "redirect:/admin/profile";
	}
	
}
