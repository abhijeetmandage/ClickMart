package com.ecom.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecom.model.UserDetail;
import com.ecom.controller.repository.UserRepository;
import com.ecom.service.UserService;

@Service
public class UserServiceImpl implements UserService 
{
	
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetail saveUser(UserDetail user) 
    {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

	@Override
	public UserDetail getUserByEmail(String email) 
	{
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDetail> getUsers(String role) {
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean upadateAcountStatus(int id, Boolean status) {
		Optional<UserDetail> findByUser = userRepository.findById(id);
		if(findByUser.isPresent()) {
			UserDetail userDetail=findByUser.get();
			userDetail.setIsEnable(status);
			userRepository.save(userDetail);
			return true;
		}
		return false;
	}
}