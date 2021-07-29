package com.shopme.admin.product;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopme.admin.user.UserNotFoundException;
import com.shopme.common.entity.Product;
import com.shopme.common.entity.User;

@Service
@Transactional
public class ProductService {

	@Autowired
	private ProductRepository repo;

	public List<Product> listAll() {
		return (List<Product>) repo.findAll();
	}

	public Product save(Product product) {
		if (product.getId() == null) {
			product.setCreatedTiem(new Date());
		}

		if (product.getAlias() == null || product.getAlias().isEmpty()) {
			String deraultAlias = product.getName().trim().replaceAll(" ", "_");
			product.setAlias(deraultAlias);
		} else {
			product.setAlias(product.getAlias().trim().replaceAll(" ", "_"));
		}

		product.setUpdatedTiem(new Date());

		return repo.save(product);
	}

	public String checkUniqueProduct(Integer id, String name) {
		name = name.trim();
		Product product = repo.findByName(name);

		if (product == null)
			return "OK";

		boolean isCreatingNew = (id == null);

		if (isCreatingNew) {
			if (product != null)
				return "Duplicated";
		} else {
			if (product.getId() != id)
				return "Duplicated";
		}
		return "OK";
	}

	public void updateEnableStatus(Integer id, boolean status) {

		Product product = repo.findById(id).get();
		product.setEnabled(status);

		repo.save(product);
	}

	public void delete(Integer id) throws ProductNotFoundException {
		Long count = repo.countById(id);

		if (count == null || count == 0) {
			throw new ProductNotFoundException("Could not find product with the id: " + id);
		}
		repo.deleteById(id);
	}

	public Product get(Integer id) throws ProductNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new ProductNotFoundException("Could not find product with the id: " + id);
		}
	}
}
