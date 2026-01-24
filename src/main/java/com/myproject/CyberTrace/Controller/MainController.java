package com.myproject.CyberTrace.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.NativeQuery.ReturnableResultNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.CyberTrace.API.SendEmail;
import com.myproject.CyberTrace.DTO.ComplaintDto;
import com.myproject.CyberTrace.DTO.EnquiryDto;
import com.myproject.CyberTrace.DTO.LoginDto;
import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Model.Enquiry;
import com.myproject.CyberTrace.Model.Notification;
import com.myproject.CyberTrace.Model.Notification.NotificationStatus;
import com.myproject.CyberTrace.Model.Users;
import com.myproject.CyberTrace.Model.Users.UserRole;
import com.myproject.CyberTrace.Repository.ComplaintRepository;
import com.myproject.CyberTrace.Repository.EnquiryRepository;
import com.myproject.CyberTrace.Repository.NotificationRepository;
import com.myproject.CyberTrace.Repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private EnquiryRepository enquiryRepo;
	
	@Autowired
	private ComplaintRepository complaintRepo;
	
	@Autowired
	private SendEmail sendEmail;
		
	@Autowired
	private NotificationRepository notificationRepo;
		
	
	
	@GetMapping("/AboutUs")
	public String ShowAboutUs()
	{
		return "AboutUs";
	}
	
	
	@GetMapping("/Services")
	public String ShowServices()
	{
		return "Services";
	}
	
	@GetMapping("/ContactUs")
	public String ShowContactUs(Model model)
	{
		EnquiryDto dto = new EnquiryDto();
		model.addAttribute("dto", dto);
		return "ContactUs";
	}
	
	@GetMapping("/")
	public String ShowIndex(Model model) {
		List<Notification> notifications = notificationRepo.findAllByStatus(NotificationStatus.RUNNING);
		model.addAttribute("notifications", notifications);
		return "index";
	}
	
	
	@PostMapping("/ContactUs")
	public String SubmitQuery(@ModelAttribute("dto") EnquiryDto dto, RedirectAttributes attributes)
	{
		Enquiry enquiry = new Enquiry();
		enquiry.setName(dto.getName());
		enquiry.setGender(dto.getGender());
		enquiry.setContactNo(dto.getContactNo());
		enquiry.setEmail(dto.getEmail());
		enquiry.setAddress(dto.getAddress());
		enquiry.setSubject(dto.getSubject());
		enquiry.setMessage(dto.getMessage());
		enquiry.setEnquiryDate(LocalDateTime.now());
		
		enquiryRepo.save(enquiry);
		attributes.addFlashAttribute("msg", "Query Successfully Submitted");
		
		return "redirect:/ContactUs";
	}
	
	@GetMapping("/Login")
	public String ShowLogin(Model model)
	{
		LoginDto dto = new LoginDto();
		model.addAttribute("dto", dto);
		return "Login";
	}
	
	
	@PostMapping("/Login")
	public String Login(@ModelAttribute("dto") LoginDto dto, RedirectAttributes attributes, HttpSession session)
	{
		try {
			if(!userRepo.existsByEmail(dto.getEmail())) {
				attributes.addFlashAttribute("msg", "User not found");
				return "redirect:/Login";
			}
			
			Users user = userRepo.findByEmail(dto.getEmail());
			
			if (user.getPassword().equals(dto.getPassword()) && user.getEmail().equals(dto.getEmail())) {
				if (user.getRole().equals(UserRole.ADMIN)) { //admin
					session.setAttribute("loggedInAdmin", user);
					return "redirect:/Admin/Dashboard";
				}
				else {
					//investigator
					session.setAttribute("loggedInUser", user);
					return "redirect:/Investigator/Dashboard";
				}
			}
			else {
				attributes.addFlashAttribute("msg", "Wrong userid or password");
			}
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Login";
	}
	
	
	
	@GetMapping("/ReportScam")
	public String ShowReportScam(Model model)
	{
		ComplaintDto dto = new ComplaintDto();
		model.addAttribute("dto", dto);
		return "ReportScam";
	}

	
	
	@PostMapping("/ReportScam")
	public String ReportScam(@ModelAttribute("dto") ComplaintDto dto, @RequestParam("evidenceImages") MultipartFile[] evidenceImages, RedirectAttributes attributes) throws IOException
	{
		
		
		try {
			if (evidenceImages.length==0 || evidenceImages[0].isEmpty()) {
				attributes.addFlashAttribute("msg", "You have to upload atleast 1 images.");
				return "redirect:/ReportScam";
			}
			if (evidenceImages.length > 5) {
				attributes.addFlashAttribute("msg", "You can upload only maximum 5 images");
				return "redirect:/ReportScam";
			}
			String uploadDir = "public/uploads/";
			File folder = new File(uploadDir);

			if (!folder.exists()) {
			    folder.mkdirs();   // FIXED
			}

			List<String> fileNames = new ArrayList<>();

			for (MultipartFile file : evidenceImages) {

			    if (file.isEmpty()) continue; // skip empty files

			    String storageFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			    Path uploadPath = Paths.get(uploadDir, storageFileName);

			    Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
			    fileNames.add(storageFileName);
			}

			
			Complaint complaint = new Complaint();
			String cid = "CT-"+System.currentTimeMillis();
			
			complaint.setComplaintId(cid);
			complaint.setName(dto.getName());
			complaint.setContactNo(dto.getContactNo());
			complaint.setWhatsappNo(dto.getWhatsappNo());
			complaint.setEmail(dto.getEmail());
			complaint.setAddress(dto.getAddress());
			complaint.setLostAmount(dto.getLostAmount());
			complaint.setPlatform(dto.getPlatform());
			complaint.setScammer(dto.getScammer());
			complaint.setTitle(dto.getTitle());
			complaint.setDescription(dto.getDescription());
			complaint.setTypeOfScam(dto.getTypeOfScam());
			complaint.setScamDateTime(null);
			complaint.setRegDateTime(LocalDateTime.now());
			complaint.setStatus(ComplaintStatus.PENDING);
			complaint.setEvidence(fileNames);	
			complaintRepo.save(complaint);		
			sendEmail.sendComplaintSuccessMail(complaint);
			attributes.addFlashAttribute("msg", "your complaint successfully registered");
				
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}	
		return "redirect:/ReportScam";
	}
	
	
	
	
	@GetMapping("/TrackComplaint")
	public String ShowTrackComplaint(Model model) {
		ComplaintDto dto = new ComplaintDto();
		model.addAttribute("dto",dto);
		return "TrackComplaint";
	}
	
	@PostMapping("/TrackComplaint")
	public  String  TrackComplaint(@RequestParam("cid") String cid, Model model, RedirectAttributes attributes) {
		if(!complaintRepo.existsByComplaintId(cid)) {
			attributes.addFlashAttribute("msg", "Invalid Complaint Id");
			return "redirect:/TrackComplaint";
		}
			
		Complaint complaint = complaintRepo.findByComplaintId(cid);
		model.addAttribute("complaint",complaint);
		return "TrackComplaint";
	}
	
}
