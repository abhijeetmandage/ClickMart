package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDetail;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommanUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController 
{
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommanUtil commanUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String home()
	{
		return "user/home";
	}
	
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
	
	@GetMapping("/addCart")
	public String addToCart(@RequestParam int pid,@RequestParam int uid,HttpSession session) {
		Cart saveCart=cartService.saveCart(pid,uid);
		
		if(ObjectUtils.isEmpty(saveCart)) 
			session.setAttribute("errorMsg", "product add to cart feild");
		else 		
			session.setAttribute("succMsg", "product added to cart feild");
		
		return "redirect:/view_product/"+pid;
	}
	
	@GetMapping("/cart")
	public String loadCartPage(Principal p,Model m)
	{
		UserDetail user=getLoggedInUserDetails(p);
		List<Cart>carts=cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		
		if(carts.size()-1>=0) 
		{
			Double totalOrderPrice=carts.get(carts.size()-1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "user/cart";
	}
	
	@GetMapping("/cardQuantityUpdate")
	public String cardQuantityUpdate(@RequestParam String sy,@RequestParam Integer cid) 
	{
		cartService.UpadateCartQuantity(sy, cid);
		return "redirect:/user/cart";
	}
	
	private UserDetail getLoggedInUserDetails(Principal p) 
	{
		String email=p.getName();
		UserDetail userDetail=userService.getUserByEmail(email);
		return userDetail;
	}
	
	@GetMapping("/order")
	public String orderPage(Principal p,Model m) 
	{
		UserDetail user=getLoggedInUserDetails(p);
		List<Cart>carts=cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		
		if(carts.size()-1>=0)
		{
			Double orderPrice=carts.get(carts.size()-1).getTotalOrderPrice();
			Double totalOrderPrice=carts.get(carts.size()-1).getTotalOrderPrice()+25+5;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/order";
	}
	
	@PostMapping("/save-Order")
	public String saveOrder(@ModelAttribute OrderRequest request,Principal p) 
	{
		
		UserDetail getLoggedUser=getLoggedInUserDetails(p);
		orderService.saveOrder(getLoggedUser.getId(), request);
		return "redirect:/user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccsesspage() 
	{
		return "/user/success";
	}
	
	@GetMapping("user-Orders")
	public String myOrder(Model m,Principal p)
	{
		UserDetail loginUser=getLoggedInUserDetails(p);
		List<ProductOrder>orders=orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}
	
	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,HttpSession session) throws Exception 
	{
		
		OrderStatus[] values=OrderStatus.values();
		String status=null;
		
		for(OrderStatus orderst:values)
		{
			if(orderst.getId().equals(st))
				status=orderst.getName();
		}
		ProductOrder updateOrder =orderService.updateOrderStatus(id, status);
		
		commanUtil.sendMailForProductOrder(updateOrder, status);
		
		if(!ObjectUtils.isEmpty(updateOrder))
			session.setAttribute("succMsg", "Status Updated");
		else
			session.setAttribute("errorMsg", "Status not updated");
		return "redirect:/user/user-Orders";
	}
	
	@GetMapping("/profile")
	public String profile()
	{
		return "/user/profile";
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
		return  "redirect:/user/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,HttpSession session)
	{
		UserDetail loggedUserDetails=getLoggedInUserDetails(p);
		
		boolean matches=passwordEncoder.matches(currentPassword, loggedUserDetails.getPassword());
		
		if(matches) 
		{
			String ecodedPassword=passwordEncoder.encode(newPassword);
			loggedUserDetails.setPassword(ecodedPassword);
			UserDetail updateUser=userService.updateUser(loggedUserDetails);
			if(ObjectUtils.isEmpty(updateUser))			
				session.setAttribute("errorMsg", "Password not changes. Error in Server");			
			else 			
				session.setAttribute("succMsg", "Password change successfully");		
		}
		else 
		{
			session.setAttribute("errorMsg", "Current Password incorect");
		}
		return "redirect:/user/profile";
	}
	
}
