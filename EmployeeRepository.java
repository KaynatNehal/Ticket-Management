package com.net.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.net.Entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	
	
	Employee findByEmpId(Long empId);
	
	List<Employee> findByEmpFnameContains(String Name);
	
	List<Employee> findByEmpLnameContains(String Name);
}
