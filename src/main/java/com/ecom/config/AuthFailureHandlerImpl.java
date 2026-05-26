package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.controller.repository.UserRepository;
import com.ecom.model.UserDetail;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException 
	{
		String email=request.getParameter("username");
		UserDetail userDetail=userRepository.findByEmail(email);
		
		if(userDetail.getIsEnable()) 
		{
			if(userDetail.getAccountNonLocked()) 
			{
				if(userDetail.getFailAttempt()<AppConstant.ATTEMPT_TIME)
				{
					userService.increaseFailAttempt(userDetail);
				}
				else
				{
					userService.userAccountLock(userDetail);
					exception=new LockedException("your account is locked Failed attempt 3");
				}
			}
			else
			{
				if(userService.unlockAccountTimeExpired(userDetail)) 
				{
					exception=new LockedException("your account is unlocked please try to login");
				}
				else 
				{
					exception=new LockedException("your account is Locked please try after sometimes");
				}
			}
		}
		else 
		{
			exception=new LockedException("your account is inactive");
		}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}
