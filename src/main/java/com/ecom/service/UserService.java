package com.ecom.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDetail;

public interface UserService 
{
	public UserDetail saveUser(UserDetail user);
	
	public UserDetail getUserByEmail(String email);
	
	public List<UserDetail> getUsers(String role);
	
	public Boolean upadateAcountStatus(int id,Boolean status);
	
	public void increaseFailAttempt(UserDetail user);
	
	public void userAccountLock(UserDetail user);
	
	public Boolean unlockAccountTimeExpired(UserDetail user);
	
	public void resetAttemp(int userId);
	
	public void updateUserResetToken(String email,String resetToken);
	
	public UserDetail getUserByToken(String token);
	
	public UserDetail updateUser(UserDetail user);
	
	public UserDetail updateUserProfile(UserDetail user,MultipartFile img);
	
	public UserDetail saveAdmin(UserDetail user);
	
	public Boolean existsEmail(String email);
	
}
