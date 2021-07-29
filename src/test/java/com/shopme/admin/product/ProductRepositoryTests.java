package com.shopme.admin.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.Product;

@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repo;

	@Autowired
	TestEntityManager entityManager;

	@Test
	public void createThreeProducts() {
		Brand brand = entityManager.find(Brand.class, 1);
		Category category = entityManager.find(Category.class, 5);

		Product product = new Product();

		product.setName("Acer Aspire Desktop");
		product.setAlias("acer_aspire_desktop");
		product.setShortDiscription("Short description for acer aspire");
		product.setFullDescription("full description for acer aspire");

		product.setBrand(brand);
		product.setCategory(category);

		product.setPrice(678);
		product.setCost(600);
		product.setEnabled(true);
		product.setInStock(true);

		product.setCreatedTiem(new Date());
		product.setUpdatedTiem(new Date());

		Product save = repo.save(product);

		assertThat(save).isNotNull();
		assertThat(save.getId()).isGreaterThan(0);

	}

	@Test
	public void testListAllProducts() {
		Iterable<Product> findAll = repo.findAll();
		findAll.forEach(System.out::println);

	}

	@Test
	public void testGetProduct() {
		Integer id = 2;
		Product product = repo.findById(id).get();

		assertThat(product).isNotNull();
	}

	@Test
	public void testUpdateProduct() {
		Integer id = 1;
		Product product = repo.findById(id).get();
		product.setPrice(499);
		repo.save(product);

		Product find = entityManager.find(Product.class, id);
		System.out.println(find);
		assertThat(find.getPrice()).isEqualTo(499);

	}

	@Test
	public void testDeleteProduct() {
		Integer id = 4;
		repo.deleteById(id);
		Optional<Product> findById = repo.findById(id);

		assertThat(!findById.isPresent());

	}

	@Test
	public void testtestSaveProductWIthImages() {
		Integer id = 1;
		Product product = repo.findById(id).get();

		product.setMainImage("main image.jpg");
		product.addExtraImage("extra image1.png");
		product.addExtraImage("extra image2.png");
		product.addExtraImage("extra image3.png");

		Product saved = repo.save(product);

		assertThat(saved.getImages().size()).isEqualTo(3);

	}

	@Test
	public void testaddProductWithDetails() {
		Integer id =1;
		Product product = repo.findById(id).get();

		product.addDetails("Device Memo", "128 GB");
		product.addDetails("CPU Model", "MediaTek");
		product.addDetails("OS", "Android 10");

		Product saved = repo.save(product);
		
		
		assertThat(saved.getDetails()).isNotEmpty();
		
	}
}
