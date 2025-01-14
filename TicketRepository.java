package com.net.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.net.Entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	Ticket findByTicketId(long ticketID);	
	
	List<Ticket> findAllByCreaterId(long empid);
	
	List<Ticket> findAllByOrderByTicketIdDesc();
	
    List<Ticket> findBySubjectContains(String searchValue);
    
    List<Ticket> findByTicketStatusContains(String status);
    
    List<Ticket> findByCreaterIdAndTicketStatusContains(long empid,String status);
    
}
