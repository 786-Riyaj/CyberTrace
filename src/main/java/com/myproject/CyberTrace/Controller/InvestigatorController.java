
package com.myproject.CyberTrace.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Users;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Repository.ComplaintRepository;
import com.myproject.CyberTrace.Repository.UserRepository;
import com.myproject.CyberTrace.Repository.UserRepository;

import jakarta.mail.Session;
import jakarta.persistence.Column;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Investigator")
public class InvestigatorController {

    @Autowired
    private ComplaintRepository complaintRepo;

    @Autowired
    private HttpSession session;
    
    @Autowired
    private UserRepository userRepo;
    
    @Column(unique = true)
    private String complaintId;

    

    // ================= ASSIGNED COMPLAINTS =================
    @GetMapping("/AssignedComplaints")
    public String showAssignedComplaints(HttpSession session, Model model) {

        Users investigator = (Users) session.getAttribute("loggedInUser");
        if (investigator == null) {
            return "redirect:/Login";
        }

        List<Complaint> complaints =
                complaintRepo.findAllByStatusAndAssignedTo(
                        ComplaintStatus.PROCCESSING, investigator);

        model.addAttribute("complaints", complaints);
        return "Investigator/AssignedComplaints";
    }

    // ================= CLOSE COMPLAINT =================
    @PostMapping("/CloseComplaint")
    public String closeComplaint(@RequestParam long cid,
                                 @RequestParam String message,
                                 RedirectAttributes attributes) {

        try {
            Complaint complaint = complaintRepo.findById(cid)
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));

            complaint.setMessage(message);
            complaint.setStatus(ComplaintStatus.RESOLVED);
            complaint.setSolvedAt(LocalDateTime.now());
            complaintRepo.save(complaint);

            attributes.addFlashAttribute("msg", "Complaint Successfully Closed");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/Investigator/AssignedComplaints";
    }

    // ================= CLOSED COMPLAINTS =================
    @GetMapping("/ClosedComplaints")
    public String showClosedComplaints(HttpSession session, Model model) {

        Users investigator = (Users) session.getAttribute("loggedInUser");
        if (investigator == null) {
            return "redirect:/Login";
        }

        List<Complaint> complaints =
                complaintRepo.findAllByStatusAndAssignedTo(
                        ComplaintStatus.RESOLVED, investigator);

        model.addAttribute("complaints", complaints);
        return "Investigator/ClosedComplaints";
    }

    // ================= REJECTED COMPLAINTS =================
    @GetMapping("/RejectedComplaints")
    public String showRejectedComplaints(HttpSession session, Model model) {

        Users investigator = (Users) session.getAttribute("loggedInUser");
        if (investigator == null) {
            return "redirect:/Login";
        }

        List<Complaint> complaints =
                complaintRepo.findAllByStatusAndAssignedTo(
                        ComplaintStatus.TERMINATED, investigator);

        model.addAttribute("complaints", complaints);
        return "Investigator/RejectedComplaints";
    }

    // ================= REJECT COMPLAINT =================
    @PostMapping("/RejectComplaint")
    public String rejectComplaint(@RequestParam long cid,
                                  @RequestParam String message,
                                  RedirectAttributes attributes) {

        try {
            Complaint complaint = complaintRepo.findById(cid)
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));

            complaint.setMessage(message);
            complaint.setStatus(ComplaintStatus.TERMINATED);
            complaint.setSolvedAt(LocalDateTime.now());
            complaintRepo.save(complaint);

            attributes.addFlashAttribute("msg", "Complaint successfully rejected");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/Investigator/RejectedComplaints";
    }

    
    
    // ================= VIEW PROFILE =================
    @GetMapping("/ViewProfile")
    public String showViewProfile(HttpSession session, Model model) {

        Users user = (Users) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/Login";
        }

        model.addAttribute("user", user);
        return "Investigator/ViewProfile";
    }

    // ================= UPDATE PROFILE PIC =================
    
    @GetMapping("/UpdateProfilePic")
    public String ShowUpdateProfilePic() {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/Login";
        }

        return "Investigator/UpdateProfilePic";
    }

    
    @PostMapping("/UpdateProfilePic")
    public String UpdateProfilePic(@RequestParam("profilePic") MultipartFile profilePic,
                                   RedirectAttributes attributes) {

        try {

            Users user = (Users) session.getAttribute("loggedInUser");

            if (user == null) {
                return "redirect:/Login";
            }

            String profilePicName = System.currentTimeMillis()
                    + "_" + profilePic.getOriginalFilename();

            String uploadDir = "public/Profile/";

            Files.copy(profilePic.getInputStream(),
                    Paths.get(uploadDir + profilePicName),
                    StandardCopyOption.REPLACE_EXISTING);

            user.setProfilePic(profilePicName);
            session.setAttribute("loggedInUser", user);
            userRepo.save(user);

            attributes.addFlashAttribute("msg",
                    "Profile Pic Successfully Changed");

        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }

        return "redirect:/Investigator/UpdateProfilePic";
    }


    
    
    // ================= CHANGE PASSWORD =================
	
    
    @GetMapping("/ChangePassword")
    public String ShowChangePassword() {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/Login";
        }

        return "Investigator/ChangePassword";
    }


    @PostMapping("/ChangePassword")
    public String ChangePassword(HttpServletRequest request,
                                 RedirectAttributes attributes) {

        try {

            String oldPass = request.getParameter("oldPassword");
            String newPass = request.getParameter("newPassword");
            String confirmPass = request.getParameter("confirmPassword");

            Users user = (Users) session.getAttribute("loggedInUser");

            if (!newPass.equals(confirmPass)) {
                attributes.addFlashAttribute("msg",
                        "New Password and Confirm must be same");
                return "redirect:/Investigator/ChangePassword";
            }

            if (newPass.equals(user.getPassword())) {
                attributes.addFlashAttribute("msg",
                        "New Password and Old Password cannot be same");
                return "redirect:/Investigator/ChangePassword";
            }

            if (oldPass.equals(user.getPassword())) {

                user.setPassword(confirmPass);
                userRepo.save(user);

                session.removeAttribute("loggedInUser");
                attributes.addFlashAttribute("msg",
                        "Password Changed Successfully, Please Login Again");

                return "redirect:/Login";

            } else {
                attributes.addFlashAttribute("msg", "Invalid Old Password !");
            }

        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }

        return "redirect:/Investigator/ChangePassword";
    }


    

    // ================= LOGOUT =================
    @GetMapping("/Logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/Login";
    }
    
   
    
    @GetMapping("/Dashboard")
    public String investigatorDashboard(Model model, HttpSession session) {

        Users investigator = (Users) session.getAttribute("loggedInUser");

        if (investigator == null) {
            return "redirect:/Login";
        }

        long totalCases = complaintRepo.countByAssignedTo(investigator);

        long ongoingCases = complaintRepo.countByAssignedToAndStatus(
                investigator, ComplaintStatus.PROCCESSING);

        long completedCases = complaintRepo.countByAssignedToAndStatus(
                investigator, ComplaintStatus.RESOLVED);

        long pendingCases = complaintRepo.countByAssignedToAndStatus(
                investigator, ComplaintStatus.PENDING);

        List<Complaint> assignedCases =
                complaintRepo.findByAssignedTo(investigator);

        model.addAttribute("totalCases", totalCases);
        model.addAttribute("ongoingCases", ongoingCases);
        model.addAttribute("completedCases", completedCases);
        model.addAttribute("pendingCases", pendingCases);
        model.addAttribute("assignedCases", assignedCases);
        model.addAttribute("investigator", investigator);

        return "Investigator/Dashboard";
    }


}
