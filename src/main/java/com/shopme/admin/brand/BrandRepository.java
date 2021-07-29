package com.shopme.admin.brand;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.shopme.common.entity.Brand;

@EnableJpaRepositories
public interface BrandRepository extends PagingAndSortingRepository<Brand, Integer> {

	Brand findByName(String name);

	Long countById(Integer id);
	
	@Query("Select b from Brand b where name like %?1%")
	public Page<Brand> findAll(String keyword, Pageable pageable);
	

	@Query("SELECT NEW Brand(b.id, b.name) FROM Brand b ORDER BY b.name ASC")
	//projection
	public List<Brand> findAll();
}
