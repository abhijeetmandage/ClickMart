package com.ecom.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDetail;
import com.ecom.controller.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

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
        user.setAccountNonLocked(true);
        user.setFailAttempt(0);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

	@Override
	public UserDetail getUserByEmail(String email) 
	{
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDetail> getUsers(String role)
	{
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean upadateAcountStatus(int id, Boolean status) 
	{
		Optional<UserDetail> findByUser = userRepository.findById(id);
		if(findByUser.isPresent())
		{
			UserDetail userDetail=findByUser.get();
			userDetail.setIsEnable(status);
			userRepository.save(userDetail);
			return true;
		}
		return false;
	}

	@Override
	public void increaseFailAttempt(UserDetail user) 
	{
		int attempt=user.getFailAttempt()+1;
		user.setFailAttempt(attempt);
		userRepository.save(user);
	}

	@Override
	public void userAccountLock(UserDetail user)
	{
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
	}

	@Override
	public Boolean unlockAccountTimeExpired(UserDetail user) 
	{
		long lockTime=user.getLockTime().getTime();
		long unLockTime=lockTime + AppConstant.UNLOCK_DURATION_TIME;
		long currentTime=System.currentTimeMillis();
		if(unLockTime<currentTime) 
		{
			user.setAccountNonLocked(true);
			user.setFailAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttemp(int userId)
	{
		
	}

	@Override
	public void updateUserResetToken(String email, String resetToken)
	{
		UserDetail findByMail=userRepository.findByEmail(email);
		findByMail.setResetToken(resetToken);
		userRepository.save(findByMail);
	}

	@Override
	public UserDetail getUserByToken(String token)
	{
		return userRepository.findByResetToken(token);
	}

	@Override
	public UserDetail updateUser(UserDetail user)
	{
		return userRepository.save(user);
	}

	@Override
	public UserDetail updateUserProfile(UserDetail user,MultipartFile img) 
	{
		UserDetail dbUser=userRepository.findById(user.getId()).get();
		
		if(!img.isEmpty()) 		
			dbUser.setProfileImage(img.getOriginalFilename());
				
		if(!ObjectUtils.isEmpty(dbUser)) 
		{
			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			
			dbUser=userRepository.save(dbUser);
	
		}
		if(!img.isEmpty()) 
		{
			try {
				saveImageToUploadDir(img, "Profile_img");
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		return dbUser;
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

		@Override
		public UserDetail saveAdmin(UserDetail user) 
		{
			 user.setRole("ROLE_ADMIN");
		        user.setIsEnable(true);
		        user.setAccountNonLocked(true);
		        user.setFailAttempt(0);
		        user.setPassword(passwordEncoder.encode(user.getPassword()));
		        return userRepository.save(user);
		}
}