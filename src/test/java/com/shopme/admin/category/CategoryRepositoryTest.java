package com.shopme.admin.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Category;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class CategoryRepositoryTest {

	@Autowired
	private CategoryRepository repo;

	@Test
	public void testCreateRootCategory() {
//		Category rootCategory = new Category("Computers");
		Category rootCategory = new Category("Electronics");
		Category savedCategory = repo.save(rootCategory);
		assertThat(savedCategory.getId()).isGreaterThan(0);
	}

	@Test
	public void testCreateSubCategory() {

		Category parent = new Category(7);
		Category child = new Category("iPhone", parent);
//		Category child2= new Category("Smartphones", parent);

//		repo.saveAll(List.of(child, child2));
		repo.save(child);
		assertThat(child.getId()).isGreaterThan(0);

	}

	@Test
	private void testListRootCategories() {
		List<Category> rootCat = repo.findRootCategories(Sort.by("name").ascending());
		rootCat.forEach(cat -> System.out.println(cat.getName()));
	}

	@Test
	public void testGetCategory() {
		Category category = repo.findById(1).get();
		System.out.println(category.getName());

		Set<Category> children = category.getChildren();
		for (Category subCat : children) {
			System.out.println(subCat.getName());
		}
		assertThat(children.size()).isGreaterThan(1);
	}

	@Test
	public void testPringHirarchicalCategories() {
		Iterable<Category> categories = repo.findAll();
		for (Category category : categories) {
			if (category.getParent() == null) {
				System.out.println(category.getName());

				Set<Category> children = category.getChildren();

				for (Category child : children) {
					System.out.println("--" + child.getName());
					pringChildren(child, 1);

				}
			}

		}
	}

	private void pringChildren(Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = parent.getChildren();
		for (Category subCategory : children) {
			for (int i = 0; i < newSubLevel; i++) {
				System.out.print("--");
			}
			System.out.println(subCategory.getName());

			// call inner
			pringChildren(subCategory, newSubLevel);
		}
	}
	
	
	@Test
	public void testFindByName() {
		String name = "Computers";
		Category category = repo.findByName(name);
		Category category2 = repo.findByAlias(name);
		assertNotNull(category);
		assertThat(category.getName()).isEqualTo(name);
		assertNotNull(category2);

	}

}
