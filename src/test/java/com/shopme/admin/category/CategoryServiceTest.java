package com.shopme.admin.category;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.shopme.common.entity.Category;

@ExtendWith(MockitoExtension.class)//for creating fake repository
@ExtendWith(SpringExtension.class)
public class CategoryServiceTest {
	
	@MockBean
	private CategoryRepository repo;
	//inject fake categoryrepository at runtime
	//into the categorySerice
	
	@InjectMocks
	private CategoryService service;
	
	@Test
	public void testCheckUniqueInNewModeReturnDuplicateName() {
		Integer id = null; //testing creating new category mode
		String name = "Computers";
		String alias = "abc";
		
		Category category = new Category(id, name, alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(category);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);

		
		String result = service.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateName");
		
	}
	
	@Test
	public void testCheckUniqueInNewModeReturnDuplicateAlias() {
		Integer id = null; //testing creating new category mode
		String name = "NameABC";
		String alias = "computers";
		
		Category category = new Category(id, name, alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(category);

		
		String result = service.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateAlias");
		
	}
	
	@Test
	public void testCheckUniqueInNewModeReturnOK() {
		Integer id = null; //testing creating new category mode
		String name = "NameABC";
		String alias = "abc";
		
		Category category = new Category(id, name, alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);

		
		String result = service.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("OK");
		
	}
	@Test
	public void testCheckUniqueInEditModeReturnDuplicateName() {
		Integer id = 1; //testing creating new category mode
		String name = "NameABC";
		String alias = "abc";
		
		Category category = new Category(2, name, alias); //different id
		
		Mockito.when(repo.findByName(name)).thenReturn(category);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);

		
		String result = service.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateName");
		
	}
	
	@Test
	public void testCheckUniqueInEditModeReturnDuplicateAlias() {
		Integer id = 1; //testing creating new category mode
		String name = "NameABC";
		String alias = "computers";
		
		Category category = new Category(2, name, alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(category);

		
		String result = service.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateAlias");
		
	}
	
	
	@Test
	public void testCheckUniqueInEditModeReturnOK() {
		Integer id = 1; //testing creating new category mode
		String name = "NameABC";
		String alias = "computers";
		
		Category category = new Category(id, name, alias);
		

		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(category);

		
		String result = service.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("OK");
		
	}
}
