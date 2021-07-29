package com.shopme.admin.brand;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.shopme.admin.category.CategoryNotFoundException;
import com.shopme.admin.category.CategoryService;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@Controller
public class BrandController {

	@Autowired
	private BrandService service;

	@Autowired
	private CategoryService catService;

	@GetMapping("/brands")
	public String findAllBrands(Model model) {
		return listByPage(1, "asc", model, null, "name");
	}

	@GetMapping("/brands/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, @Param("sortDir") String sortDir, Model model,
			@Param("keyword") String keyword, @Param("sortField") String sortField) {

		Page<Brand> page = service.listByPage(pageNum, sortField, sortDir, keyword);
		List<Brand> listBrands = page.getContent();

		
		long startCount = (pageNum - 1) * BrandService.BRANDS_PER_PAGE + 1;
		long endCount = startCount + BrandService.BRANDS_PER_PAGE - 1;

		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}

		String revString = sortDir.equals("asc") ? "desc" : "asc";

		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", revString);
		model.addAttribute("keyword", keyword);
		model.addAttribute("listBrands", listBrands);

		model.addAttribute("pageNum", pageNum);
//		List<Brand> allBrands = service.findAll();

//		model.addAttribute("brands", allBrands);

		return "brands/brands";
	}

	@GetMapping("/brands/new")
	public String newCategory(Model model) {
		List<Category> listCategoriesUsedInForm = catService.listCategoriesUsedInForm();
		model.addAttribute("brand", new Brand());
		model.addAttribute("pageTitle", "Create New Brand");
		model.addAttribute("listCategories", listCategoriesUsedInForm);

		return "brands/brand_form";
	}

	@GetMapping("/brands/edit/{id}")
	public String updateEnabledStatus(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes,
			Model model) {

		try {

			Brand findById = service.findById(id);

			model.addAttribute("pageTitle", "Update Brand (" + findById.getName() + ")");

			model.addAttribute("brand", findById);

			List<Category> listCategoriesUsedInForm = catService.listCategoriesUsedInForm();

			model.addAttribute("listCategories", listCategoriesUsedInForm);

			return "brands/brand_form";
		} catch (BrandNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
			return "redirect:/brands";
		}
	}

	@PostMapping("brands/save")
	public String createNewBrand(Brand brand, @RequestParam("fileImage") MultipartFile multipartFile,
			RedirectAttributes redirectAttributes) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			brand.setLogo(fileName);

			Brand savedCategory = service.save(brand);
			// this to read the id and create the directory for the image as indexed by id

			String uploadDir = "../brand-images/" + savedCategory.getId();

			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		} else {
			service.save(brand);
		}
		redirectAttributes.addFlashAttribute("message", "Category has been saved successfully.");

		return "redirect:/brands";
	}

	@GetMapping("/brands/delete/{id}")
	public String deleteBrand(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, Model model) {
		try {
			service.delete(id);
			String brandDir = "../brand-images/" + id;
			FileUploadUtil.removeDir(brandDir);

			redirectAttributes.addFlashAttribute("message", "The brand ID: " + id + " has been deleted successfully");

		} catch (BrandNotFoundException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		model.addAttribute(redirectAttributes);
		return "redirect:/brands";

	}

}
