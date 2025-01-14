package com.net.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.net.Entity.Admin;
import com.net.Entity.Employee;
import com.net.Entity.Ticket;
import com.net.Repository.AdminRepository;
import com.net.Repository.EmployeeRepository;
import com.net.Repository.TicketRepository;
import com.net.Service.EmailService;

@Controller
public class HomeController {

	@Autowired
	private EmployeeRepository employeeRepository;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private TicketRepository ticketRpository;
	
	@Autowired
	private EmailService emailService;
	
	
	@RequestMapping("/StarDesk")
	public String home()
	{
		return "index";
	}
	
	@RequestMapping("/login")
	public String login(@RequestParam Long loginId, @RequestParam String loginPassword, Model themodel) 
	{
	    Employee employee = employeeRepository.findById(loginId).orElse(null);
	    Admin admin = adminRepository.findById(loginId).orElse(null);
	    
	    if (employee != null) {
	        String empPass = employee.getEmpPass(); 
	        if (loginPassword.equals(empPass)) {
	        	themodel.addAttribute("empData", employee);
	        	Map<String,Integer> stats = new HashMap<>();
	        	stats.put("allTickets",(ticketRpository.findAllByCreaterId(loginId)).size());
	        	stats.put("openTickets",(ticketRpository.findByCreaterIdAndTicketStatusContains(loginId, "Open")).size());
	        	stats.put("closedTickets",(ticketRpository.findByCreaterIdAndTicketStatusContains(loginId, "Closed")).size());
	        	themodel.addAttribute("stats", stats);
	        	return "Employee/Dashboard";
	        }
	    } else if (admin != null) {
	        String adminPass = admin.getAdminPass(); 
	        if (loginPassword.equals(adminPass)) {
	        	themodel.addAttribute("adminData", admin);
	        	Map<String,Integer> stats = new HashMap<>();
	    	    stats.put("allTickets", (ticketRpository.findAll()).size());
	    	    stats.put("openTickets", (ticketRpository.findByTicketStatusContains("Open")).size());
	    	    stats.put("closedTickets", (ticketRpository.findByTicketStatusContains("Closed")).size());
	    	    stats.put("empCount", (employeeRepository.findAll()).size());
	    	    themodel.addAttribute("stats", stats);
	            return "Admin/AdminDashboard";
	        }
	    } 
	    themodel.addAttribute("error", "Invalid credentials");
	    return "index";
	}
	
	@RequestMapping("/registerNew")
	public String registerNew()
	{
		return "RegisterNew";
	}
	
	@RequestMapping("/Register")
	public String register(@RequestParam String firstName, @RequestParam String lastName, 
            @RequestParam Long userId, @RequestParam String email, 
            @RequestParam String password, @RequestParam String role)
	{
		if ("employee".equals(role)) {
            Employee employee = new Employee();
            employee.setEmpFname(firstName);
            employee.setEmpLname(lastName);
            employee.setEmpId(userId);
            employee.setEmpEmail(email);
            employee.setEmpPass(password);
            employeeRepository.save(employee);
        } else if ("admin".equals(role)) {
            Admin admin = new Admin();
            admin.setAdminFname(firstName);
            admin.setAdminLname(lastName);
            admin.setAdminId(userId);
            admin.setAdminEmail(email);
            admin.setAdminPass(password);
            adminRepository.save(admin);
        }
		emailService.sendEmail(email,firstName);
		return "index";
	}

	@RequestMapping("/Employee/Dash-home")
	public String empDashHome(@RequestParam("userid") long empid, Model themodel) 
	{
		Employee employee = employeeRepository.findByEmpId(empid);
	    themodel.addAttribute("empData", employee);
	    
	    Map<String,Integer> stats = new HashMap<>();
    	stats.put("allTickets",(ticketRpository.findAllByCreaterId(empid)).size());
    	stats.put("openTickets",(ticketRpository.findByCreaterIdAndTicketStatusContains(empid, "Open")).size());
    	stats.put("closedTickets",(ticketRpository.findByCreaterIdAndTicketStatusContains(empid, "Closed")).size());
    	themodel.addAttribute("stats", stats);
		return "Employee/Dashboard";
	}
	
	@RequestMapping("/Employee/create-ticket")
	public String createTicket(@RequestParam("userid") long empid,  Model themodel) 
	{
		 Employee employee = employeeRepository.findByEmpId(empid);
		 themodel.addAttribute("empData", employee);
		return "Employee/CreateTicket";
	}
	
	@RequestMapping("/generate-ticket")
	public String generateTicket(@ModelAttribute Ticket ticket, @RequestParam("snapshotFile") MultipartFile snapshotFile, Model themodel)
	{
		
		if (!snapshotFile.isEmpty()) {
	        try {
	            // Save the image file to the static folder
	            Path imagePath = Paths.get("src/main/resources/static/snapshots/" + snapshotFile.getOriginalFilename());
	            Files.copy(snapshotFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
	            
	            // Store the image path in the Ticket entity
	            ticket.setSnapshot("/snapshots/" + snapshotFile.getOriginalFilename());
	        } catch (IOException e) {
	           System.out.println(e.getMessage());
	        }
	    }
		ticketRpository.save(ticket);
		Long userid = ticket.getCreaterId();
		Employee employee = employeeRepository.findByEmpId(userid);
		themodel.addAttribute("empData", employee);
		
		
		Map<String,Integer> stats = new HashMap<>();
    	stats.put("allTickets",(ticketRpository.findAllByCreaterId(userid)).size());
    	stats.put("openTickets",(ticketRpository.findByCreaterIdAndTicketStatusContains(userid, "Open")).size());
    	stats.put("closedTickets",(ticketRpository.findByCreaterIdAndTicketStatusContains(userid, "Closed")).size());
    	themodel.addAttribute("stats", stats);
		return "Employee/Dashboard";
	}
	
	@RequestMapping("/show-my-ticket")
    public String showTickets(@RequestParam("userid") long empid, Model themodel)
    {
		List<Ticket> tickets = ticketRpository.findAllByCreaterId(empid);
		themodel.addAttribute("tickets", tickets);
		Employee employee = employeeRepository.findByEmpId(empid);
	    themodel.addAttribute("empData", employee);
		return "Employee/ShowTickets";
    }
	
	@RequestMapping("/view-full-ticket")
	public String viewFullTicket(@RequestParam("ticketId") long ticketId, @RequestParam("createrId") long empid, Model themodel)
	{
		Ticket ticket = ticketRpository.findByTicketId(ticketId);
		themodel.addAttribute("ticket", ticket);
		Employee employee = employeeRepository.findByEmpId(empid);
	    themodel.addAttribute("empData", employee);
		return "/Employee/FullTicket";
	}
	
	@RequestMapping("/search-ticket")
	public String searchPage(@RequestParam("userid") long empid, Model themodel)
	{
		Employee employee = employeeRepository.findByEmpId(empid);
	    themodel.addAttribute("empData", employee);
		return "/Employee/SearchTicket";
	}
	
	@RequestMapping("/Employee/Search-ticket-containing")
	public String Search(@RequestParam("searchValue") String searchValue,@RequestParam("userid") long empid, Model themodel)
	{
			List<Ticket> ticket = ticketRpository.findBySubjectContains(searchValue);
			themodel.addAttribute("ticket",ticket);
			Employee employee = employeeRepository.findByEmpId(empid);
		    themodel.addAttribute("empData", employee);
		
		return "/Employee/SearchTicket";
	}
	
	@RequestMapping("/Employee/delete-ticket")
	public String delTicket(@RequestParam("ticketId") long ticketId, @RequestParam("userid") long empid, Model themodel)
	{
		ticketRpository.deleteById(ticketId);
		List<Ticket> tickets = ticketRpository.findAllByCreaterId(empid);
		themodel.addAttribute("tickets", tickets);
		Employee employee = employeeRepository.findByEmpId(empid);
	    themodel.addAttribute("empData", employee);
	    
		return "Employee/ShowTickets";
	}	
	
	//Admin related methods//
	
	@RequestMapping("/Admin/view-all-tickets")
	public String allTickets(@RequestParam("userid") long adminId, Model theModel) 
	{
		List<Ticket> alltickets = ticketRpository.findAllByOrderByTicketIdDesc();
		theModel.addAttribute("alltickets",alltickets);
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
		
        return "Admin/AllTickets";
	}
	
	@RequestMapping("/Admin/ViewTicket")
	public String viewTicket(@RequestParam("userid") long adminId,@RequestParam("ticketId") long ticketId,  Model theModel)
	{
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
	    Ticket ticket = ticketRpository.findByTicketId(ticketId);
	    theModel.addAttribute("ticket",ticket);
	    
		return"Admin/ViewTicket";
	}
	
	@RequestMapping("/Admin/view-all-employees")
	public String allEmployees(@RequestParam("userid") long adminId, Model theModel) 
	{
		List<Employee> allemployees = employeeRepository.findAll();
		theModel.addAttribute("allemployees",allemployees);
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
		
        return "Admin/AllEmployees";
	}
	
	@RequestMapping("/Admin/Dashboard")
	public String adminDash(@RequestParam("userid") long adminId, Model theModel)
	{
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
	   
	    Map<String,Integer> stats = new HashMap<>();
	    stats.put("allTickets", (ticketRpository.findAll()).size());
	    stats.put("openTickets", (ticketRpository.findByTicketStatusContains("Open")).size());
	    stats.put("closedTickets", (ticketRpository.findByTicketStatusContains("Closed")).size());
	    stats.put("empCount", (employeeRepository.findAll()).size());
	    theModel.addAttribute("stats", stats);
	    
		return "Admin/AdminDashboard";
	}
	
	@RequestMapping("/Admin/delete-ticket")
	public String deleteTicket(@RequestParam("ticketId") long ticketId, @RequestParam("userid") long adminId, Model theModel)
	{
		ticketRpository.deleteById(ticketId);
		List<Ticket> alltickets = ticketRpository.findAllByOrderByTicketIdDesc();
		theModel.addAttribute("alltickets",alltickets);
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
	    
		return "Admin/AllTickets";
	}
	
	@RequestMapping("/Admin/close-ticket")
	public String closeTicket(@RequestParam("ticketId") long ticketId,@ModelAttribute("ticket") Ticket ticketData, @RequestParam("userid") long adminId, Model theModel)
	{
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
	    Ticket ticket = ticketRpository.findByTicketId(ticketId); 
	    ticketData.setTicketStatus("Closed");
	    ticketData.setSubject(ticket.getSubject());
	    ticketData.setCreaterEmail(ticket.getCreaterEmail());
	    ticketData.setCreaterId(ticket.getCreaterId());
	    ticketData.setCreaterName(ticket.getCreaterName());
	    ticketData.setDescription(ticket.getDescription());
	    ticketData.setTime(ticket.getTime());
	    ticketData.setSnapshot(ticket.getSnapshot());
	    ticketRpository.save(ticketData);
	    List<Ticket> alltickets = ticketRpository.findAllByOrderByTicketIdDesc();
		theModel.addAttribute("alltickets",alltickets);
	    
		return "Admin/AllTickets";
	}
	
	@RequestMapping("/Admin/search-Tickets")
	public String adminSearchPage(@RequestParam("userid") long adminId, Model theModel)
	{
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
		return "/Admin/SearchTicket";
	}
	
	@RequestMapping("/Admin/Searchticket-containing")
	public String searchTicket(@RequestParam("searchValueId") String Id,
			                   @RequestParam("searchValueSubj") String Subj,
			                   @RequestParam("userid") long adminId, Model theModel)
	{
		long ID = 0 ;
		if(Id != "") {
			ID = Long.parseLong(Id);
		}
		
		List<Ticket> ticket = new ArrayList<Ticket>();
			if(Subj != "") {
				 ticket.addAll(ticketRpository.findBySubjectContains(Subj));  
			} 
			if(ID != 0) {
				Ticket ticketFound = ticketRpository.findByTicketId(ID);
				if(ticketFound != null) {
					ticket.add(ticketFound);
				}
			}
	    theModel.addAttribute("ticket",ticket);
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
		return "/Admin/SearchTicket";
	}
	
	@RequestMapping("/Admin/view-full-ticket")
	public String adminViewFullTicket(@RequestParam("userid") long adminId,@RequestParam("ticketId") long ticketId, Model theModel)
	{
		Ticket ticket = ticketRpository.findByTicketId(ticketId);
		theModel.addAttribute("ticket", ticket);
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
		return "/Admin/ViewTicket";
	}
	
	@RequestMapping("/Admin/search-Employees")
	public String adminSearchEmpPage(@RequestParam("userid") long adminId, Model theModel)
	{
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
		return "/Admin/SearchEmployee";
	}
	
	@RequestMapping("/Admin/searchEmployee-containing")
	public String searchEmployee(@RequestParam("searchedId") String Id,
			                   @RequestParam("searchedName") String Name,
			                   @RequestParam("userid") long adminId, Model theModel)
	{
		long ID = 0 ;
		if(Id != "") {
			ID = Long.parseLong(Id);
		}
		
		List<Employee> employee = new ArrayList<Employee>();
			if(Name != "") {
				 employee.addAll(employeeRepository.findByEmpFnameContains(Name));  
				 employee.addAll(employeeRepository.findByEmpLnameContains(Name));
			} 
			if(ID != 0) {
				Employee emp = employeeRepository.findByEmpId(ID);
				if(emp != null) {
					employee.add(emp);
				}
			}
	    theModel.addAttribute("employee",employee);
		Admin adminData = adminRepository.findByAdminId(adminId);
	    theModel.addAttribute("adminData", adminData);
		return "/Admin/SearchEmployee";
	}	
	
	
}
