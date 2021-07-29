package com.shopme.admin.security;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.common.entity.User;

import net.bytebuddy.utility.RandomString;


@Controller
public class ForgetPassword {

	@Autowired
	UserService userService;

	@Autowired
    private JavaMailSender javaMailSender;
	
	@GetMapping("forgot_password")
	public String showForgotForm() {
		return "users/reset_password";
	}
	@PostMapping("forgotpassword")
	public String ressetPassword(HttpServletRequest request,RedirectAttributes redirectAttributes) {
		String emailByForm = request.getParameter("email");
		User user = userService.getByEmail(emailByForm);
		if(user!=null) {
			user.setResetPasswordToken(RandomString.make(30).toString());
			userService.updateAccount(user);
			sendEmail(user.getResetPasswordToken());
			return "login";
		}
		redirectAttributes.addFlashAttribute("message", "No user in db with this email.");

		return "redirect:/users/reset_password";
	}
	
	@GetMapping("reset")
	public String reset(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
		User user = userService.findByToken(token);
		System.out.println(user);
		if(user!=null) {
			model.addAttribute("user",user);
			return "users/changepass";
		} 
		redirectAttributes.addFlashAttribute("message", "Link used before!");
		return "redirect:/users/reset_password";
	}
    void sendEmail(String pt) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("omariit@gmail.com");

        msg.setSubject("Testing from Spring Boot");
        msg.setText("Click on this link to reset your account password http://localhost/ShopmeAdmin/reset?token="+pt);

        javaMailSender.send(msg);

    }
    @PostMapping("savenewpass")
    public String change(User user) throws UserNotFoundException {
    	
    	try {
			User inDB = userService.get(user.getId());
			inDB.setPassword(user.getPassword());
			userService.UpdatePassword(inDB);
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			throw new UserNotFoundException("no user with this id");
		}
    	return "login";
    }

}
