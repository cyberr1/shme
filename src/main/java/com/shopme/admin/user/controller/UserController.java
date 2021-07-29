package com.shopme.admin.user.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.user.UserNotFoundException;
import com.shopme.admin.user.UserService;
import com.shopme.admin.user.export.PDFExporter;
import com.shopme.admin.user.export.UserCsvExporter;
import com.shopme.admin.user.export.UserExcelExporter;
import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Controller
public class UserController {

	@Autowired
	private UserService service;

	@GetMapping("/users")
	public String listFirstPage(Model model) {
		return listByPage(1, model, "firstName", "asc", null);
	}

	@GetMapping("/users/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNumb, Model model,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "keyword", required = false) String keyword) {

//		if(sortField == null || sortField.isEmpty()) sortField = "id";
//		if(sortDir== null) sortDir = "asc";

		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		System.out.println("Sort Field: " + sortField);
		System.out.println("Sort Dir: " + sortDir);

		Page<User> page = service.listByPage(pageNumb, sortField, sortDir, keyword);
		List<User> listUsers = page.getContent();

		long startCount = (pageNumb - 1) * UserService.USERS_PER_PAGE + 1;
		long endCount = startCount + UserService.USERS_PER_PAGE - 1;

		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("currentPage", pageNumb);
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);

		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("listUsers", listUsers);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);

		return "users/users";
	}

	@GetMapping("/users/new")
	public String addNewUserPage(Model model) {
		List<Role> listRoles = service.listRoles();
		User user = new User();

		user.setEnabled(true);

		model.addAttribute("user", user);
		model.addAttribute("listRoles", listRoles);
		model.addAttribute("pageTitle", "Create New User");
		return "users/user_form";
	}

	@PostMapping("users/save")
	public String saveUser(User user, RedirectAttributes redirectAttributes,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			user.setPhotos(fileName);
			User savedUser = service.save(user);
			String uploadDir = "user-photos/" + savedUser.getId();
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			if (user.getPhotos().isEmpty())
				user.setPhotos(null);
			service.save(user);
		}

		redirectAttributes.addFlashAttribute("message", "The user has been saved successfully.");

		return getRedirectURLtoAffectedUser(user);
	}

	private String getRedirectURLtoAffectedUser(User user) {
		String firstPartOfEmail = user.getEmail();// .split("@")[0];
		return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword=" + firstPartOfEmail;
	}

	@GetMapping("/users/edit/{id}")
	public String editUser(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, Model model) {

		try {
			User user = service.get(id);
			model.addAttribute("user", user);
			model.addAttribute("pageTitle", "Update User (" + user.getFirstName() + " " + user.getLastName() + ")");

			List<Role> listRoles = service.listRoles();
			model.addAttribute("listRoles", listRoles);

			return "users/user_form";
		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
			model.addAttribute(redirectAttributes);
			return "redirect:/users";

		}

	}

	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes,
			Model model) {
		try {
			service.delete(id);
			redirectAttributes.addFlashAttribute("message", "The user ID: " + id + " has been deleted successfully");

		} catch (UserNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		model.addAttribute(redirectAttributes);
		return "redirect:/users";

	}

	@GetMapping("/users/{id}/enabled/{status}")
	public String updateEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean status,
			RedirectAttributes redirectAttributes) {
		service.updateEnableStatus(id, status);
		String message = status ? "enabled" : "disabled";
		redirectAttributes.addFlashAttribute("message", "User " + id + " has been " + message);

		return "redirect:/users";
	}

	@GetMapping("/users/export/csv")
	public void exportCSV(HttpServletResponse response) throws IOException {
		List<User> listUsers = service.listAll();

		UserCsvExporter exporter = new UserCsvExporter();
		exporter.export(listUsers, response);
		// return "redirect:/users";
	}

	@GetMapping("/users/export/excel")
	public void exportExcel(HttpServletResponse response) throws IOException {
		List<User> listUsers = service.listAll();
		
		UserExcelExporter exporter = new UserExcelExporter();
		exporter.export(listUsers, response);
	}
	
	@GetMapping("/users/export/pdf")
	public void exportPDF(HttpServletResponse response) throws IOException {
		List<User> listUsers = service.listAll();
		
		PDFExporter exporter = new PDFExporter();
		exporter.export(listUsers, response);
	}

}













