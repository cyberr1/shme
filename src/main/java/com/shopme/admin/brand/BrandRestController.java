package com.shopme.admin.brand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@RestController
public class BrandRestController {
	@Autowired
	private BrandService service;

	@PostMapping("/brands/check_unique")
	public String checkUnique(@Param("id") Integer id, @Param("name") String name) {

		return service.checkUnique(id, name);
	}
	
	
	
	//this is to handle new product form when selecting a brand then show in the category lest just the categories for that brand
	
	
	@GetMapping("/brands/{id}/categories")
	public List<CategoryDTO> listCategoriesByBrand(@PathVariable("id") Integer id) throws BrandNotFoundRestException {
		
		List<CategoryDTO> listCategory= new ArrayList<>();
		try {
			Brand brand = service.findById(id);
			Set<Category> categories = brand.getCategories();
			
			for(Category category : categories) {
				CategoryDTO dto = new CategoryDTO(category.getId(), category.getName());
				listCategory.add(dto);
			}
			
			return listCategory;
		} catch (BrandNotFoundException e) {
			throw new BrandNotFoundRestException();
		}
	}
}
