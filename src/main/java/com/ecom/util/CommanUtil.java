package com.ecom.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.ProductOrder;
import com.ecom.model.UserDetail;
import com.ecom.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommanUtil 
{
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private  UserService userService;
	
	public Boolean sendMail(String url,String toSendemail) throws UnsupportedEncodingException, MessagingException 
	{
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage);
		helper.setFrom("abhijeetmandage993@gmail.com", "From ClickMart");
		helper.setTo(toSendemail);
		String content =
			    "<p>Hello,</p>"
			    + "<p>You have requested to reset your password.</p>"
			    + "<p>Click the link below to change your password:</p>"
			    + "<p><a href=\"" + url
			    + "\">Change my password</a></p>";
		helper.setSubject("Password Reset");
		helper.setText(content,true);
		mailSender.send(mimeMessage);
		return true;
	}
	
	public static String generateUrl(HttpServletRequest request)
	{
		String siteUrl= request.getRequestURL().toString();
		return siteUrl.replace(request.getServletPath(), "");
		
	}
	
	String msg=null;
	
	public Boolean sendMailForProductOrder(ProductOrder order,String status) throws Exception 
	{		
		msg =
		        "<div style='line-height:1; margin:0; padding:0;'>"
		        + "<div style='margin:0;'>Hello [[Name]], Thank you for order. Please check below details</div>"
		        + "<div style='margin:0;'>Order Status : <b>[[orderStatus]]</b></div>"
		        + "<div style='margin:0;'>Product : [[productName]]</div>"
		        + "<div style='margin:0;'>Category : [[category]]</div>"
		        + "<div style='margin:0;'>Quantity : [[quantity]]</div>"
		        + "<div style='margin:0;'>Price : [[price]]</div>"
		        + "<div style='margin:0;'>Payment Type : [[paymentType]]</div>"
		        + "</div>";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(message);
		helper.setFrom("abhijeetmandage993@gmail.com", "From ClickMart");
		helper.setTo(order.getOrderAress().getEmail());
		
		msg=msg.replace("[[Name]]",order.getOrderAress().getFirstName());
		msg=msg.replace("[[orderStatus]]",status);
		msg=msg.replace("[[productName]]", order.getProduct().getTitle());
		msg=msg.replace("[[category]]", order.getProduct().getCategory());
		msg=msg.replace("[[quantity]]", order.getQuantity().toString());
		msg=msg.replace("[[price]]", order.getPrice().toString());
		msg=msg.replace("[[paymentType]]", order.getPaymentType());
		
		helper.setSubject("Order Status");
		helper.setText(msg,true);
		mailSender.send(message);
		return true;
	}
	
	public  UserDetail getLoggedInUserDetails(Principal p) 
	{
		String email=p.getName();
		UserDetail userDetail=userService.getUserByEmail(email);
		return userDetail;
	}
}
