package com.shopme.admin.brand;

import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Brand;

@Service
@Transactional
public class BrandService {

	public static final int BRANDS_PER_PAGE =10;
	@Autowired
	BrandRepository repo;

	public List<Brand> findAll() {
		return (List<Brand>) repo.findAll();
	}
	public Page<Brand> listByPage(int pageNum, String sortField,  String sortDir, String keyword){
		Sort sort = Sort.by(sortField);
		
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum -1, BRANDS_PER_PAGE, sort);
		
		if(keyword != null ) {
			return repo.findAll(keyword, pageable);
		}
		
			
		return repo.findAll(pageable);
	}

	public Brand findById(Integer id) throws BrandNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new BrandNotFoundException("Could not find category with the id: " + id);
		}

	}

	public Brand save(Brand brand) {

		return repo.save(brand);
	}

	public String checkUnique(Integer id, String name) {
		boolean isCreating = (id == null || id == 0);

		name = name.trim();
		Brand brandByName = repo.findByName(name);

		if (isCreating) {
			if (brandByName != null) {
				return "Duplicated";
			}
		} else {// in update (editing mode)
			if (brandByName != null && brandByName.getId() != id) {
				return "Duplicated";
			}
		}

		return "OK";	}

	public void delete(Integer id) throws BrandNotFoundException {
		Long count = repo.countById(id);
		if (count == null || count == 0) {
			throw new BrandNotFoundException("Could not find Brand with the id: " + id);
		}
		repo.deleteById(id);
	}

}
