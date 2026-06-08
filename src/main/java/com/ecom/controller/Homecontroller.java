package com.ecom.controller;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDetail;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommanUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
public class Homecontroller 
{
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommanUtil commanUtil;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private CartService cartService;
	
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
	public String index(Model m) 
	{
		List<Category> allActiveCategory=categoryService.getAllActiveCategory().stream()
				.sorted((c1,c2)->c2.getId().compareTo(c1.getId())).limit(6).toList();
		List<Product> allActiveProduct=productService.getAllActiveProduvts("").stream()
				.sorted((p1,p2)->p2.getId().compareTo(p1.getId())).limit(12).toList();
		m.addAttribute("homecategory", allActiveCategory);
		m.addAttribute("homeproducts", allActiveProduct);
		return "index";
	}
	
	@GetMapping("/base")
	public String base() 
	{
		return "base";
	}
	
	@GetMapping("/signin")
	public String login() 
	{
		return "login";
	}
	
	@GetMapping("/register")
	public String register() 
	{
		return "register";
	}
	
	@GetMapping("/product")
	public String product(Model m,@RequestParam(value="category", defaultValue="") String category,
			@RequestParam(name="pageNo",defaultValue="0") Integer pageNo,
			@RequestParam(name="pageSize",defaultValue="8") Integer pageSize) 
	{
		List<Category>categoreis=categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categoreis", categoreis);
		
		Page<Product>page=productService.getAllActiveProductPagination(pageNo, pageSize,category);
		List<Product> products=page.getContent();
		m.addAttribute("products",products);
		m.addAttribute("productsSize", products.size());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize",pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		return "product";
	}
	
	@GetMapping("/view_product/{id}")
	public String view_product(@PathVariable int id,Model m) 
	{
		Product productById=productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDetail user,@RequestParam("img") MultipartFile file,HttpSession session ) throws IOException 
	{		
		Boolean existsEmail=userService.existsEmail(user.getEmail());		
		if(existsEmail) 
		{
			session.setAttribute("errorMsg", "Email already exists");
		}
		else
		{
			String imgName=file.isEmpty()?"default.png":file.getOriginalFilename();
			user.setProfileImage(imgName);
			UserDetail saveUser=userService.saveUser(user);
			if(!ObjectUtils.isEmpty(saveUser)) 
			{
				if(!file.isEmpty()) 
				{
					saveImageToUploadDir(file, "Profile_img");
				}
				session.setAttribute("succMsg", "Data save successfully");
			}else
			{
				session.setAttribute("errorMsg", "Somthing wrong on server");
			}
		}
		return "redirect:/register";
	}

	//forgot password
	@GetMapping("/forgot-Password")
	public String forgotPassword() 
	{
		return "forgot_password";
	}
	
	@PostMapping("/forgot-Password")
	public String ProcessForgotPassword(@RequestParam String email,HttpSession session,HttpServletRequest request) throws UnsupportedEncodingException, MessagingException 
	{
		UserDetail userByEmail=userService.getUserByEmail(email);
		if(ObjectUtils.isEmpty(userByEmail)) 
		{
			session.setAttribute("errorMsg", "Invalid Email");
		}
		else
		{
			String resetToken=UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);
			
			String url=CommanUtil.generateUrl(request)+"/reset-Password?token="+resetToken;
			
			Boolean sendMail=commanUtil.sendMail(url,email);
			if(sendMail)
			{
				session.setAttribute("succMsg", "please check your Email passowrd reset link send in emil");
			}
			else
			{
				session.setAttribute("errorMsg", "Something wrong on server mail not send");
			}
		}
		return "redirect:/forgot-Password";
	}
	
	@GetMapping("/reset-Password")
	public String showResetPassword(@RequestParam String token,HttpSession session,Model m)
	{
		UserDetail userByTiken=userService.getUserByToken(token);
		if(userByTiken==null)
		{
			System.out.println("token is="+userByTiken);
			m.addAttribute("errorMsg", "Your link is invalid or expired");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}
	
	@PostMapping("/reset-Password")
	public String resetPassword(@RequestParam String token,@RequestParam String password,HttpSession session,Model m) {
		UserDetail userByToken=userService.getUserByToken(token);
		System.out.println("Received token = " + token);
		if(userByToken==null) 
		{
			m.addAttribute("errorMsg", "Your link is invalid or expired");
			return "message";
		}
		else 
		{
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			m.addAttribute("succMsg", "password change succsesfully");
			return "message";
		}
		
	}
	
	//fixing the images not apear bug
		private void saveImageToUploadDir(MultipartFile file, String subFolder) throws IOException 
		{
		    String uploadDir = System.getProperty("user.home") 
		                     + File.separator + "ecom_uploads" 
		                     + File.separator + subFolder;
		    Path uploadPath = Paths.get(uploadDir);
		    if (!Files.exists(uploadPath)) {
		        Files.createDirectories(uploadPath); // creates folder automatically
		    }
		    Files.copy(file.getInputStream(), 
		               uploadPath.resolve(file.getOriginalFilename()), 
		               StandardCopyOption.REPLACE_EXISTING);
		}
		//end 
		
		@GetMapping("/search")
		public String searchProduct(@RequestParam String ch, Model m) 
		{
		    List<Product> searchProducts = productService.searchProduct(ch);

		    m.addAttribute("products", searchProducts);
		    m.addAttribute("productsSize", searchProducts.size());

		    List<Category> categoreis = categoryService.getAllActiveCategory();
		    m.addAttribute("categoreis", categoreis);

		    m.addAttribute("totalElements", searchProducts.size());
		    m.addAttribute("totalPages", 1);
		    m.addAttribute("pageNo", 0);
		    m.addAttribute("pageSize", 8);
		    m.addAttribute("isFirst", true);
		    m.addAttribute("isLast", true);

		    if (!searchProducts.isEmpty())		   
		        m.addAttribute("paramValue",searchProducts.get(0).getCategory());		   
		    else 	    
		        m.addAttribute("paramValue", "");		    
		    return "product";
		}
}
