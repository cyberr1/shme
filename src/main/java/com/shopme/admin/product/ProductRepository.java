package com.shopme.admin.product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.Product;
import com.shopme.common.entity.User;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<Product, Integer> {

	 Product findByName( String name);

	Long countById(Integer id);

	
}