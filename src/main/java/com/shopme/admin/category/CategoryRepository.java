package com.shopme.admin.category;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.shopme.common.entity.Category;


public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer> {

	
	@Query("Select c from Category c where c.parent.id is NULL")
	public List<Category> findRootCategories(Sort sort);
	
	

	@Query("Select c from Category c where c.parent.id is NULL")
	public Page<Category> findRootCategories(Pageable pageable);
	
	
//	@Query("SELECT c from Category c where c.name Like %?1%")
	public Category findByName(String name);
	
	public Category findByAlias(String alias);

	@Query("Update Category c set c.enabled = ?2 where c.id=?1")
	@Modifying
	public void updateEnabledStatus(Integer id, boolean enabled);
	

	public Long countById(Integer id);

	
	@Query("SELECT c from Category c where CONCAT(c.id, c.name, c.alias) LIKE %?1% or "+
			"CONCAT(c.id, c.name, c.alias) LIKE %?1%")
	public Page<Category> search(String keyword, Pageable pageable);




}
