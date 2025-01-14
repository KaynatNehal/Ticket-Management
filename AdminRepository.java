package com.net.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.net.Entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

	Admin findByAdminId(Long adminId);
	
}
