package com.ecom.service;

import java.util.List;

import com.ecom.model.UserDetail;

public interface UserService 
{
	public UserDetail saveUser(UserDetail user);
	
	public UserDetail getUserByEmail(String email);
	
	public List<UserDetail> getUsers(String role);
	
	public Boolean upadateAcountStatus(int id,Boolean status);
}
