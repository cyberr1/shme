package com.shopme.admin.brand;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class BrandRepositoryTest {

	@Autowired
	private BrandRepository repo;
	
	@Test
	public void insertBrand() {
		Category laptops = new Category(4);
		
		Brand brand = new Brand("Apple", "brand-logo.png");
		brand.getCategories().add(laptops);
		Brand save = repo.save(brand);
		
		
		assertThat(save.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateBrand2Categories() {
		
		Brand brand = new Brand("Samsung", "brand-logo.png");

		brand.getCategories().add(new Category(24));
		brand.getCategories().add(new Category(29));

		
		Brand save = repo.save(brand);
		
		
		
		assertThat(save.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testFindAllBrands() {
		Iterable<Brand> findAll = repo.findAll();
		findAll.forEach(System.out::println);
//		System.out.println(findAll.size());
		assertThat(findAll).isNotEmpty();
	}

	@Test
	public void updateSamsung() {
		Brand brand = repo.findById(3).get();
		
		brand.setName("Samsung Electronics");
		
		Brand save = repo.save(brand);
		assertThat(save.getName()).isEqualTo("Samsung Electronics");
	}
	
	@Test
	public void deleteById() {
		Integer id = 2;
		
		repo.deleteById(id);
		Brand brand = repo.findById(id).get();
		
		assertThat(brand).isNull();
	}
}








