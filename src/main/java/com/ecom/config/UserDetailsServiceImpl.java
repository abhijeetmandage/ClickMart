package com.ecom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecom.controller.repository.UserRepository;
import com.ecom.model.UserDetail;

@Service
public class UserDetailsServiceImpl
        implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    
    
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
    	
    	 	System.out.println("LOGIN EMAIL = "+ username);
        UserDetail user =userRepository.findByEmail(username);
        System.out.println("USER = " + user);
        
        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found");
        }
        System.out.println("DB PASSWORD = " +
                user.getPassword());
       
        return new CustomUser(user);
    }
}