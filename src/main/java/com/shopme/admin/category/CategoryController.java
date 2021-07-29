package com.shopme.admin.category;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
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
import com.shopme.admin.user.export.UserCsvExporter;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.User;

@Controller
public class CategoryController {

	@Autowired
	private CategoryService service;

	@GetMapping( "/categories")
	public String listFirstPage(@Param("sortDir") String sortDir,Model model) {
		return listByPage(1, sortDir, model, null);
	}
	
	@GetMapping("/categories/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum,
			@Param("sortDir") String sortDir,Model model,
			@Param("keyword") String keyword) {

		if (sortDir == null || sortDir.isEmpty()) {
			sortDir = "asc";
		} 
		CategoryPageInfo pageInfo  = new CategoryPageInfo();
		
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		List<Category> categories = service.listByPage(pageInfo, pageNum, sortDir,keyword);
		model.addAttribute("categories", categories);
		model.addAttribute("reverseSortDir", reverseSortDir );
		model.addAttribute("totalPages",pageInfo.getTotalPages());
		model.addAttribute("totalItems",pageInfo.getTotalElements());
		model.addAttribute("currentPage",pageNum);
		
		
		long startCount = (pageNum - 1) * CategoryService.ROOT_CATEGORY_PER_PAGE + 1;
		long endCount = startCount + CategoryService.ROOT_CATEGORY_PER_PAGE - 1;
		if (endCount > pageInfo.getTotalElements()) {
			endCount = pageInfo.getTotalElements();
		}
		
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("keyword", keyword);

		model.addAttribute("sortDir", sortDir);
		model.addAttribute("sortField", "name");
		return "categories/categories";	}

	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		List<Category> listCategoriesUsedInForm = service.listCategoriesUsedInForm();
		model.addAttribute("category", new Category());
		model.addAttribute("pageTitle", "Create New Category");
		model.addAttribute("listCategories", listCategoriesUsedInForm);

		return "categories/category_form";
	}

	@PostMapping("categories/save")
	public String createNewCategory(Category category, @RequestParam("fileImage") MultipartFile multipartFile,
			RedirectAttributes redirectAttributes) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);

			
			Category savedCategory = service.save(category);
			// this to read the id and create the directory for the image as indexed by id

			String uploadDir = "../category-images/" + savedCategory.getId();

			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			service.save(category);
		}
		redirectAttributes.addFlashAttribute("message", "Category has been saved successfully.");

		return "redirect:/categories";
	}

	@GetMapping("/categories/edit/{id}")
	public String editCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, Model model) {

		try {
			Category editCategory = service.listById(id);

			model.addAttribute("category", editCategory);
			model.addAttribute("pageTitle", "Update User (" + editCategory.getName() + ")");

			List<Category> listCategoriesUsedInForm = service.listCategoriesUsedInForm();
			model.addAttribute("listCategories", listCategoriesUsedInForm);

			return "categories/category_form";
		} catch (CategoryNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/categories";
		}

	}
	
	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean status,
			RedirectAttributes redirectAttributes) {
		service.updateEnableStatus(id, status);
		String message = status ? "enabled" : "disabled";
		redirectAttributes.addFlashAttribute("message", "User " + id + " has been " + message);

		return "redirect:/categories";
	}
	
	
	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable("id") Integer id,
			RedirectAttributes redirectAttributes,
			Model model) {
		try {
			service.delete(id);
			String categoryDir = "../category-images/"+id;
			FileUploadUtil.removeDir(categoryDir);
			
			redirectAttributes.addFlashAttribute("message", "The category ID: " + id + " has been deleted successfully");

		} catch (CategoryNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		model.addAttribute(redirectAttributes);
		return "redirect:/categories";

	}

	@GetMapping("/categories/export/csv")
	public void exportCSV(HttpServletResponse response) throws IOException {
		List<Category> listCategories = service.listCategoriesUsedInForm();

		CategoryCsvExporter exporter = new CategoryCsvExporter();
		exporter.export(listCategories, response);
	}
}

















