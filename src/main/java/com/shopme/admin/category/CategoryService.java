package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import com.shopme.common.entity.Category;

@Service
@Transactional // for update query
public class CategoryService {

	public static final int ROOT_CATEGORY_PER_PAGE = 4;
	@Autowired
	private CategoryRepository catRepo;

	public List<Category> listByPage(CategoryPageInfo pageInfo, int pageNum, String sortDir, String keyword) {
		Sort sort = Sort.by("name");

		if (sortDir.equals("asc")) {
			sort = Sort.by("name").ascending();

		} else if (sortDir.equals("desc")) {
			sort = Sort.by("name").descending();

		}

		Pageable pageable = PageRequest.of(pageNum - 1, ROOT_CATEGORY_PER_PAGE, sort);
		Page<Category> pageCategories = null;
		if (keyword != null && !keyword.isEmpty()) {
			pageCategories = catRepo.search(keyword, pageable);

		} else {
			pageCategories = catRepo.findRootCategories(pageable);
		}
		List<Category> rootCategories = pageCategories.getContent();

		//
		pageInfo.setTotalElements(pageCategories.getTotalElements());
		pageInfo.setTotalPages(pageCategories.getTotalPages());
		// check this different

		if (keyword != null && !keyword.isEmpty()) {
			List<Category> searchResult = pageCategories.getContent();
			for(Category category: searchResult) {
				category.setHasChildren(category.getChildren().size()>0);
			}
			
			return searchResult;
		} else {
			return listHierarchicalCategories(rootCategories, sortDir);
		}
	}

	private List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir) {
		List<Category> hirarchicalCategories = new ArrayList<>();
		for (Category rootCategory : rootCategories) {
			hirarchicalCategories.add(Category.copyFull(rootCategory));

			Set<Category> children = sortSubCategories(rootCategory.getChildren(), sortDir);// sorting the children

			for (Category subCategory : children) {
				String name = "--" + subCategory.getName();
				hirarchicalCategories.add(Category.copyFull(subCategory, name));

				listSubHierarchicalCategories(hirarchicalCategories, subCategory, 1, sortDir);

			}

		}
		return hirarchicalCategories;
	}

	private void listSubHierarchicalCategories(List<Category> hirarchicalCategories, Category parent, int level,
			String sortDir) {
		Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);// sorting the children
		int newSubLevel = level + 1;

		for (Category subCategory : children) {
			String name = "--";
			for (int i = 0; i < level; i++) {
				name += "--";
			}
			name += subCategory.getName();
			hirarchicalCategories.add(Category.copyFull(subCategory, name));

			listSubHierarchicalCategories(hirarchicalCategories, subCategory, newSubLevel, sortDir);
		}
	}

	public List<Category> listCategoriesUsedInForm() {
		List<Category> categoriesInForm = new ArrayList<>();

		Iterable<Category> findAll = catRepo.findRootCategories(Sort.by("name").ascending());

		for (Category category : findAll) {
			if (category.getParent() == null) {
				categoriesInForm.add(Category.copyCategory(category));

				Set<Category> children = sortSubCategories(category.getChildren());

				for (Category child : children) {
					categoriesInForm.add(Category.copyCategory(child.getId(), "--" + child.getName()));

					pringChildren(categoriesInForm, child, 1);

				}
			}

		}

		return categoriesInForm;
	}

	private void pringChildren(List<Category> categoriesInForm, Category parent, int subLevel) {
		int newSubLevel = subLevel + 1;
		Set<Category> children = sortSubCategories(parent.getChildren());
		for (Category subCategory : children) {
			String name = "";
			for (int i = 0; i < newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			categoriesInForm.add(Category.copyCategory(subCategory.getId(), name));
			// call inner
			pringChildren(categoriesInForm, subCategory, newSubLevel);
		}
	}

	public Category save(Category category) {

		return catRepo.save(category);
	}

	public Category listById(Integer id) throws CategoryNotFoundException {
		try {
			return catRepo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new CategoryNotFoundException("Could not find category with the id: " + id);
		}
	}

	public Category getByName(String name) {
		return catRepo.findByName(name);
	}

	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreating = (id == null || id == 0);

		name = name.trim();
		alias = alias.trim();
		Category categoryByName = catRepo.findByName(name);

		if (isCreating) {
			if (categoryByName != null) {
				return "DuplicateName";
			} else {
				Category categoryByAlias = catRepo.findByAlias(alias);
				if (categoryByAlias != null) {
					return "DuplicateAlias";
				}
			}
		} else {// in update (editing mode)
			if (categoryByName != null && categoryByName.getId() != id) {
				return "DuplicateName";
			}

			Category categoryByAlias = catRepo.findByAlias(alias);
			if (categoryByAlias != null && categoryByAlias.getId() != id) {
				return "DuplicateAlias";
			}
		}

		return "OK";
	}

	private SortedSet<Category> sortSubCategories(Set<Category> children) {
		return sortSubCategories(children, "asc");
	}

	private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
		SortedSet<Category> sortedSet = new TreeSet<>(new Comparator<Category>() {

			@Override
			public int compare(Category cat1, Category cat2) {
				if (sortDir.equals("asc")) {
					return cat1.getName().compareTo(cat2.getName());
				} else {
					return cat2.getName().compareTo(cat1.getName());
				}

			}
		});

		sortedSet.addAll(children);

		return sortedSet;

	}

	public void updateEnableStatus(Integer id, boolean status) {
		catRepo.updateEnabledStatus(id, status);
	}

	public void delete(Integer id) throws CategoryNotFoundException {
		Long count = catRepo.countById(id);
		if (count == null || count == 0) {
			throw new CategoryNotFoundException("Could not find user with the id: " + id);
		}
		catRepo.deleteById(id);
	}
}
