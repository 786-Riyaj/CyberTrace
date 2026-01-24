package com.myproject.CyberTrace.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.CyberTrace.DTO.UserDto;
import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Enquiry;
import com.myproject.CyberTrace.Model.Notification;
import com.myproject.CyberTrace.Model.Notification.NotificationStatus;
import com.myproject.CyberTrace.Model.Users;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Model.Users.UserRole;
import com.myproject.CyberTrace.Model.Users.UserStatus;
import com.myproject.CyberTrace.Repository.ComplaintRepository;
import com.myproject.CyberTrace.Repository.EnquiryRepository;
import com.myproject.CyberTrace.Repository.NotificationRepository;
import com.myproject.CyberTrace.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Admin")
public class AdminController {
	
	@Autowired
	private HttpSession session;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private EnquiryRepository enquiryRepo;

	@Autowired
	private ComplaintRepository complaintRepo;

	@Autowired
	private NotificationRepository notificationRepo;

	@GetMapping("/Dashboard")
	public String ShowDashboard(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}

		long totalInvestigators = userRepo.findAllByRole(UserRole.INVESTIGATOR).size();
		long totalComplaints = complaintRepo.count();
		long pendingComplaints = complaintRepo.findAllByStatus(ComplaintStatus.PENDING).size();
		long rejectComplaints = complaintRepo.findAllByStatus(ComplaintStatus.TERMINATED).size();
		long processingComplaints = complaintRepo.findAllByStatus(ComplaintStatus.PROCCESSING).size();
		long resolvedComplaints = complaintRepo.findAllByStatus(ComplaintStatus.RESOLVED).size();
		long totalEnquiries = enquiryRepo.count();

		model.addAttribute("totalInvestigators", totalInvestigators);
		model.addAttribute("totalComplaints", totalComplaints);
		model.addAttribute("pendingComplaints", pendingComplaints);
		model.addAttribute("rejectComplaints", rejectComplaints);
		model.addAttribute("processingComplaints", processingComplaints);
		model.addAttribute("resolvedComplaints", resolvedComplaints);
		model.addAttribute("totalEnquiries", totalEnquiries);

		List<Enquiry> enquiries = enquiryRepo.findTop3ByOrderByEnquiryDateDesc();
		model.addAttribute("enquiries", enquiries);

		// ✅ Orders data for Chart (monthly complaint)
		List<Object[]> stats = complaintRepo.getMonthlyComplaintStats();

		Map<Integer, Long> monthCountMap = new HashMap<>();
		for (Object[] row : stats) {
			int monthNumber = ((Number) row[0]).intValue(); // 1-12
			long count = ((Number) row[1]).longValue();

			monthCountMap.put(monthNumber, count);
		}

		List<String> complaintMonths = new ArrayList<>();
		List<Long> complaintCounts = new ArrayList<>();

		String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

		for (int i = 1; i <= 12; i++) {
			complaintMonths.add(monthNames[i - 1]);
			complaintCounts.add(monthCountMap.getOrDefault(i, 0L)); // agar nahi mila to 0
		}
		model.addAttribute("complaintMonths", complaintMonths);
		model.addAttribute("complaintCounts", complaintCounts);

		return "Admin/Dashboard";
	}

	@GetMapping("/Add-Investigator")
	public String ShowAddInvestigator(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}

		UserDto dto = new UserDto();
		model.addAttribute("dto", dto);

		return "Admin/AddInvestigator";
	}

	@PostMapping("/Add-Investigator")
	public String AddInvestigator(@ModelAttribute("dto") UserDto dto, RedirectAttributes attributes) {
		try {
			String storageProfilePicName = System.currentTimeMillis() + "_"
					+ dto.getProfilePic().getOriginalFilename().replaceAll("\\s+", "_");
			String storageIdProofName = System.currentTimeMillis() + "_"
					+ dto.getGovtIdProof().getOriginalFilename().replaceAll("\\s+", "_");

			String profileUploadDir = "Public/Profile/";
			String idProofUploadDir = "Public/IdProof/";

			File profileFolder = new File(profileUploadDir);
			File idProofFolder = new File(idProofUploadDir);

			if (!profileFolder.exists()) {
				profileFolder.mkdirs();
			}
			if (!idProofFolder.exists()) {
				idProofFolder.mkdirs();
			}

			Path profilePath = Paths.get(profileUploadDir, storageProfilePicName);
			Path idProofPath = Paths.get(idProofUploadDir, storageIdProofName);

			Files.copy(dto.getProfilePic().getInputStream(), profilePath, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(dto.getGovtIdProof().getInputStream(), idProofPath, StandardCopyOption.REPLACE_EXISTING);

			// Setting data into database

			Users investigator = new Users();
			investigator.setName(dto.getName());
			investigator.setGender(dto.getGender());
			investigator.setContactNo(dto.getContactNo());
			investigator.setEmail(dto.getEmail());
			investigator.setAddress(dto.getAddress());
			investigator.setPassword(dto.getPassword());
			investigator.setProfilePic(storageProfilePicName);
			investigator.setGovtIdProof(storageIdProofName);
			investigator.setStatus(UserStatus.UNBLOCKED);
			investigator.setRole(UserRole.INVESTIGATOR);
			investigator.setRegDate(LocalDateTime.now());

			userRepo.save(investigator);
			attributes.addFlashAttribute("msg", "Data Successfully Added");

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Admin/Add-Investigator";
	}

	// Manage Investigator
	@GetMapping("/Manage-Investigator")
	public String ShowManageInvestigator(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		List<Users> investigators = userRepo.findAllByRole(UserRole.INVESTIGATOR);
		model.addAttribute("investigators", investigators);
		return "Admin/ManageInvestigator";
	}

	@GetMapping("/UpdateStatus/{id}")
	public String UpdateStatus(@PathVariable long id) {
		Users investigator = userRepo.findById(id).get();
		if (investigator.getStatus().equals(UserStatus.UNBLOCKED)) {
			investigator.setStatus(UserStatus.BLOCKED);
		} else if (investigator.getStatus().equals(UserStatus.BLOCKED)) {
			investigator.setStatus(UserStatus.UNBLOCKED);
		}
		userRepo.save(investigator);
		return "redirect:/Admin/Manage-Investigator";
	}

	@GetMapping("/PendingComplaint")
	public String ShowPendingComplaint(Model model) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		List<Complaint> complaints = complaintRepo.findAllByStatus(ComplaintStatus.PENDING);
		model.addAttribute("complaints", complaints);
		List<Users> investigators = userRepo.findAllByRole(UserRole.INVESTIGATOR);
		model.addAttribute("investigators", investigators);
		return "Admin/PendingComplaint";
	}

	@PostMapping("/AssignComplaint")
	public String AssignComplaint(@RequestParam("assignTo") long uid, @RequestParam("cid") long cid,
			RedirectAttributes attributes) {
		try {
			Users investigator = userRepo.findById(uid).get();
			Complaint complaint = complaintRepo.findById(cid).get();

			complaint.setAssignedTo(investigator);
			complaint.setStatus(ComplaintStatus.PROCCESSING);
			complaintRepo.save(complaint);
			attributes.addFlashAttribute("msg", "Successfully Assigned");

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Admin/PendingComplaint";
	}

	@PostMapping("/RejectComplaint")
	public String RejectComplaint(@RequestParam("cid") long cid, @RequestParam("message") String message,
			RedirectAttributes attributes) {
		Complaint complaint = complaintRepo.findById(cid).get();
		complaint.setMessage(message);
		complaint.setStatus(ComplaintStatus.TERMINATED);
		complaintRepo.save(complaint);
		attributes.addFlashAttribute("msg", "Complaint Successfully Rejected");

		return "redirect:/Admin/PendingComplaint";

	}

	@GetMapping("/ManageComplaint")
	public String ManageComplaint(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		List<Complaint> complaints = complaintRepo.findAllByStatusOrStatus(ComplaintStatus.PROCCESSING,
				ComplaintStatus.RESOLVED);
		model.addAttribute("complaints", complaints);
		return "Admin/ManageComplaint";
	}

	@GetMapping("/RejectComplaint")
	public String RejectComplaint(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		List<Complaint> complaints = complaintRepo.findAllByStatus(ComplaintStatus.TERMINATED);
		model.addAttribute("complaints", complaints);
		return "Admin/RejectComplaint";
	}

	// admin profile pic
	@GetMapping("/UpdateProfilePic")
	public String ShowUpdateProfilePic() {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}

		return "Admin/UpdateProfilePic";

	}

	@PostMapping("/UpdateProfilePic")
	public String UpdateProfilePic(@RequestParam("profilePic") MultipartFile profilePic,
			RedirectAttributes attributes) {
		try {

			Users admin = (Users) session.getAttribute("loggedInAdmin");

			String profilePicName = System.currentTimeMillis() + "_" + profilePic.getOriginalFilename();
			String uploadDir = "public/Profile/";

			Files.copy(profilePic.getInputStream(), Paths.get(uploadDir + profilePicName),
					StandardCopyOption.REPLACE_EXISTING);
			admin.setProfilePic(profilePicName);
			session.setAttribute("loggedInAdmin", admin);
			userRepo.save(admin);
			attributes.addFlashAttribute("msg", "Profile Pic Successfully Changes");

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Admin/UpdateProfilePic";
	}

	@GetMapping("/ChangePassword")
	public String ShowChangePassword() {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "rediredct:/Login";
		}

		return "Admin/ChangePassword";
	}

	@PostMapping("/ChangePassword")
	public String ChangePassword(HttpServletRequest request, RedirectAttributes attributes) {

		try {
			String oldPass = request.getParameter("oldPass");
			String newPass = request.getParameter("newPass");
			String confirmPass = request.getParameter("confirmPass");

			Users admin = (Users) session.getAttribute("loggedInAdmin");

			if (!newPass.equals(confirmPass)) {
				attributes.addFlashAttribute("msg", "New Password and Confirm must be same");
				return "redirect:/Admin/ChangePassword";
			}
			if (newPass.equals(admin.getPassword())) {
				attributes.addFlashAttribute("msg", "New Password and Old Password can  not be same !!!");
				return "redirect:/Admin/ChangePassword";
			}

			if (oldPass.equals(admin.getPassword())) {
				admin.setPassword(confirmPass);
				userRepo.save(admin);

				session.removeAttribute("loggedInAdmin");
				attributes.addFlashAttribute("msg", "Password Change Sucessfully");
				return "redirect:/Login";

			} else {
				attributes.addFlashAttribute("msg", "Invalid Old Password !");
			}

		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());

		}

		return "redirect:/Admin/ChangePassword";
	}

	// Enquiry Details
	@GetMapping("/Enquiry")
	public String ShowEnquiry(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}

		List<Enquiry> enquiries = enquiryRepo.findAll();
		model.addAttribute("enquiries", enquiries);
		return "Admin/Enquiry";
	}

	// Enquiry Details Delete button
	@GetMapping("/DeleteEnquiry/{id}")
	public String deleteEnquiry(@PathVariable long id) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}

		enquiryRepo.deleteById(id);
		return "redirect:/Admin/Enquiry";
	}

	@GetMapping("/Logout")
	public String Logout(RedirectAttributes attributes) {
		session.removeAttribute("loggedInAdmin");
		attributes.addFlashAttribute("msg", "Logout Successful");
		return "redirect:/Login";
	}

	// Add Notification
	@GetMapping("/AddNotification")
	public String ShowAddNotification(Model model) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		List<Notification> notifications = notificationRepo.findAll();
		model.addAttribute("notifications", notifications);
		return "Admin/AddNotification";
	}

	@PostMapping("/AddNotification")
	public String publishNotification(@RequestParam("message") String message, RedirectAttributes attributes) {

		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}

		Notification notification = new Notification();
		notification.setMessage(message);
		notification.setPublishedAt(LocalDateTime.now());
		notification.setStatus(NotificationStatus.RUNNING);
		notificationRepo.save(notification);
		attributes.addFlashAttribute("msg", "Notification Published  Succesfully ");
		return "redirect:/Admin/AddNotification";
	}

	// publish Notification Status End button
	@GetMapping("/EndNotification/{id}")
	public String EndNotification(@PathVariable long id) {
		Notification notification = notificationRepo.findById(id).get();
		notification.setStatus(NotificationStatus.ENDED);
		notificationRepo.save(notification);
		return "redirect:/Admin/AddNotification";
	}

	// publish Notification Status Delete button
	@GetMapping("/DeleteNotification/{id}")
	public String deleteNotification(@PathVariable long id) {
		if (session.getAttribute("loggedInAdmin") == null) {
			return "redirect:/Login";
		}
		notificationRepo.deleteById(id);
		return "redirect:/Admin/AddNotification";
	}
	
	
	
	
	

//	Edit Investigator Profile 
	
	
	@GetMapping("/AdminEdit/{id}")
	public String adminEditPage(@PathVariable("id") long id, Model model, HttpSession session) {

	    if (session.getAttribute("loggedInAdmin") == null) {
	        return "redirect:/Login";
	    }

	    Users investigator = userRepo.findById(id).orElse(null);
	    if (investigator == null) {
	        return "redirect:/Admin/Manage-Investigator";
	    }

	    model.addAttribute("user", investigator);  

	    return "Admin/AdminEdit";
	}


	
//Edit 

	
	@PostMapping("/UpdateProfile")
	public String updateProfile(@ModelAttribute("user") Users investigator,
	                            HttpSession session,
	                            RedirectAttributes ra) {
	    
	    Users existing = userRepo.findById(investigator.getId()).orElse(null);
	    
	    if(existing != null) {
	        // 1. Data Update karein
	        existing.setName(investigator.getName());
	        existing.setGender(investigator.getGender());
	        existing.setContactNo(investigator.getContactNo());
	        existing.setEmail(investigator.getEmail());
	        existing.setAddress(investigator.getAddress());
	        userRepo.save(existing);

	        // 2. Session Check (Safe Way)
	        Users admin = (Users) session.getAttribute("loggedInAdmin");
	        
	        // Null check lagana zaroori hai taaki crash na ho
	        if(admin != null) {
	            if(admin.getId() == existing.getId()) {
	                session.setAttribute("loggedInAdmin", existing);
	            }
	        } else {
	            // Agar admin null hai toh wapas login pe bhej do
	            return "redirect:/login"; 
	        }

	        ra.addFlashAttribute("msg", "Profile updated successfully");
	    }

	    return "redirect:/Admin/Manage-Investigator";
	}


}
