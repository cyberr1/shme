package com.shopme.admin.user.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopme.admin.user.UserRepository;
import com.shopme.admin.user.UserService;

@RestController
public class UserRestController {
	
	@Autowired
	private UserService service;
	
	private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

	
	
	@PostMapping("/users/check_email")
	public String checkDuplicateEmail(@Param("id") Integer id, 
						@Param("email") String email) {
	//	if(service.isEmailUnique(id, email))System.out.println("OK" ); else System.out.println( "Duplicated");
		return service.isEmailUnique(id, email)? "OK" : "Duplicated";
	}
	@PostMapping("/users/checkpass")
	public boolean checkValidPass(@Param("pass") String pass) {
		Matcher matcher = pattern.matcher(pass);
        return matcher.matches();
	}
}
