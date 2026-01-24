package com.myproject.CyberTrace.API;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.myproject.CyberTrace.Model.Complaint;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SendEmail {

	@Autowired
	private JavaMailSender mailSender;

	
	public void sendComplaintSuccessMail(Complaint complaint) 
	{
		String subject = "Complaint Submission Successfully - Check Your Status";
	
		//Variables
		String userName = complaint.getName();
		String complaintId = complaint.getComplaintId();
		String trackLink = "#";

		// Message with variables directly inserted
		String message = "<!DOCTYPE html>\n" +
		"<html lang=\"en\">\n" +
		"<head>\n" +
		"<meta charset=\"UTF-8\">\n" +
		"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
		"<title>Complaint Registration Successful</title>\n" +
		"<style>\n" +
		"    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f8; margin: 0; padding: 0; }\n" +
		"    .container { width: 100%; max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.5); overflow: hidden; border:1px solid grey; }\n" +
		"    .header { background-color: #4CAF50; color: #ffffff; text-align: center; padding: 20px; }\n" +
		"    .header h1 { margin: 0; font-size: 24px; }\n" +
		"    .content { padding: 30px; color: #333333; line-height: 1.6; }\n" +
		"    .complaint-card { background-color: #f1f8e9; border-left: 6px solid #4CAF50; padding: 15px 20px; margin: 20px 0; border-radius: 5px; display: flex; align-items: center; gap: 15px; }\n" +
		"    .complaint-card img { width: 40px; height: 40px; }\n" +
		"    .button { display: inline-block; padding: 12px 20px; background-color: #4CAF50; color: #ffffff; text-decoration: none; border-radius: 5px; margin-top: 20px; font-weight: bold; }\n" +
		"    .footer { text-align: center; color: #888888; font-size: 12px; padding: 15px; }\n" +
		"</style>\n" +
		"</head>\n" +
		"<body>\n" +
		"<div class=\"container\">\n" +
		"    <div class=\"header\">\n" +
		"        <h1>Complaint Registration Successful</h1>\n" +
		"    </div>\n" +
		"    <div class=\"content\">\n" +
		"        <p>Dear <strong>" + userName + "</strong>,</p>\n" +
		"        <p>Your complaint has been successfully registered in our system. Please find your complaint details below:</p>\n" +
		"        <div class=\"complaint-card\">\n" +
		"            <img src=\"https://img.icons8.com/color/48/000000/ticket.png\" alt=\"Complaint ID\">\n" +
		"            <div>\n" +
		"                <strong>Complaint ID:</strong> <span>" + complaintId + "</span><br>\n" +
		"                You can check the status or progress of your complaint anytime.\n" +
		"            </div>\n" +
		"        </div>\n" +
		"        <p>Click the button below to track your complaint:</p>\n" +
		"        <a href=\"" + trackLink + "\" class=\"button\">Track Complaint</a>\n" +
		"        <p>Thank you for reaching out to us. We will resolve your complaint as soon as possible.</p>\n" +
		"        <p>Regards,<br>Customer Support Team</p>\n" +
		"    </div>\n" +
		"    <div class=\"footer\">\n" +
		"        &copy; 2025 Your Company Name. All Rights Reserved.\n" +
		"    </div>\n" +
		"</div>\n" +
		"</body>\n" +
		"</html>";
		
		
		
		
		
		

	
		//helper.setFrom(null);
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
			
			
			helper.setTo(complaint.getEmail());
			helper.setSubject(subject);
			helper.setText(message,true);
			
			mailSender.send(mimeMessage);
			System.err.println("Mail Send to : "+ complaint.getEmail());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error : " +e.getMessage());
		}
		

	}
}
