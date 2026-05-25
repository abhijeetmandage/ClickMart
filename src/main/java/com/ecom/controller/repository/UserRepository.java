package com.ecom.controller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.UserDetail;

public interface UserRepository extends JpaRepository<UserDetail, Integer> {

    public UserDetail findByEmail(String email);

	public List<UserDetail> findByRole(String role);
}