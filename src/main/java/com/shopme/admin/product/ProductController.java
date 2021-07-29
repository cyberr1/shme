package com.shopme.admin.product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.brand.BrandService;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Product;
import com.shopme.common.entity.ProductImage;

@Controller
public class ProductController {
	private static final Logger LOGGER =LoggerFactory.getLogger(ProductController.class);

	
	@Autowired
	private ProductService productService;
	@Autowired
	private BrandService brandService;

	@GetMapping("/products")
	public String listAll(Model model) {
		List<Product> listAll = productService.listAll();
		System.out.println(listAll.get(0).isEnabled());
		model.addAttribute("listProducts", listAll);

		return "products/products";
	}

	@GetMapping("/products/new")
	public String newProduct(Model model) {

		List<Brand> listBrands = brandService.findAll();
		Product product = new Product();
		product.setEnabled(true);
		product.setInStock(true);
		
		model.addAttribute("product", product);
		model.addAttribute("listBrands", listBrands);
		model.addAttribute("pageTitle", "Create New Product");
		model.addAttribute("numberOfExistingExtraImages", 0);

		return "products/product_form";
	}

	@PostMapping("/products/save")
	public String saveProduct(Product product, RedirectAttributes ra,
			@RequestParam("fileImage") MultipartFile mainImageMultipartFile,
			@RequestParam("extraImage") MultipartFile[] extraImageMultiparts,
			@RequestParam(name = "detailIDs", required = false) String[] detailIDs,
			@RequestParam(name = "detailNames", required = false) String[] detailNames,
			@RequestParam(name = "detailValues", required = false) String[] detailValues,
			@RequestParam(name = "imageNames", required = false) String[] imageNames,
			@RequestParam(name = "imageIDs", required = false) String[] imageIDs) throws IOException {

		Product savedProduct = new Product();

		setExistingExtraImageNames(imageIDs, imageNames, product);// calling method to update existing and remove the
																	// removed from page

		setMainImage(mainImageMultipartFile, product);
		setNewExtraImageNames(extraImageMultiparts, product);
		setProductDetails(detailIDs, detailNames, detailValues, product);

		savedProduct = productService.save(product);

		saveUploadedImages(mainImageMultipartFile, extraImageMultiparts, savedProduct);

		deleteExtraImagesWereRemovedOnForm(product);

		ra.addFlashAttribute("message", savedProduct.getName() + " has been saved successfully");
		return "redirect:/products";
	}

	private void deleteExtraImagesWereRemovedOnForm(Product product) {

		String extraImageDirectory = "../product-images/" + product.getId() + "/extras";
		Path dirPath = Paths.get(extraImageDirectory);

		try {
			Files.list(dirPath).forEach(file -> {
				String fileName = file.toFile().getName();

				if (!product.containsImageName(fileName)) {
					try {
						Files.delete(file);
						LOGGER.info("Deleted extra image: "+fileName);
					} catch (IOException e) {
						LOGGER.error("Could not delete extraImages: "+fileName);
					}
				}
			});
		} catch (IOException e) {
			LOGGER.error("Could not list Directory: "+dirPath);
		}
	}

	private void setExistingExtraImageNames(String[] imageIDs, String[] imageNames, Product product) {
		if (imageIDs == null || imageIDs.length == 0) {
			return;
		}

		Set<ProductImage> images = new HashSet<>();

		for (int i = 0; i < imageIDs.length; i++) {
			System.out.println(imageNames[i]);
			Integer id = Integer.parseInt(imageIDs[i]);
			String imageName = imageNames[i];
			images.add(new ProductImage(id, imageName, product));
		}

		product.setImages(images);
	}

	private void setProductDetails(String[] detailIDs, String[] detailNames, String[] detailValues, Product product) {
		if (detailNames == null || detailNames.length == 0)
			return;

		if (detailIDs != null) {
			for (int i = 0; i < detailNames.length; i++) {
				String name = detailNames[i];
				String value = detailValues[i];
				Integer id = Integer.parseInt(detailIDs[i]);

				if (id != 0) {// its an existing detaills
					product.addDetails(id, name, value);
				} else if (!name.isEmpty() && !value.isEmpty()) {
					product.addDetails(name, value);
				}
			}
		}
	}

	private void saveUploadedImages(MultipartFile mainImageMultipartFile, MultipartFile[] extraImageMultiparts,
			Product savedProduct) throws IOException {
		if (!mainImageMultipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(mainImageMultipartFile.getOriginalFilename());
			String uploadDir = "../product-images/" + savedProduct.getId();

			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, mainImageMultipartFile);
		}

		if (extraImageMultiparts.length > 0) {
			String uploadDir = "../product-images/" + savedProduct.getId() + "/extras";

			for (MultipartFile multipartFile : extraImageMultiparts) {
				if (multipartFile.isEmpty())
					continue;

				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

			}
		}

	}

	private void setNewExtraImageNames(MultipartFile[] extraImageMultiparts, Product product) {
		if (extraImageMultiparts.length > 0) {
			for (MultipartFile multipartFile : extraImageMultiparts) {
				if (!multipartFile.isEmpty()) {
					String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

					if (!product.containsImageName(fileName)) {
						product.addExtraImage(fileName);
					}
				}
			}
		}
	}

	private void setMainImage(MultipartFile mainImageMultipartFile, Product product) {
		if (!mainImageMultipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(mainImageMultipartFile.getOriginalFilename());
			product.setMainImage(fileName);
		}

	}

	@GetMapping("/products/{id}/enabled/{status}")
	public String updateEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean status,
			RedirectAttributes redirectAttributes) {
		productService.updateEnableStatus(id, status);
		String message = status ? "enabled" : "disabled";
		redirectAttributes.addFlashAttribute("message", "Porduct " + id + " has been " + message);

		return "redirect:/products";
	}

	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes ra) {
		try {
			productService.delete(id);
			String productExtraImagesDir = "../product-images/" + id + "/extras";
			String productImageDir = "../product-images/" + id;
			FileUploadUtil.removeDir(productExtraImagesDir);
			FileUploadUtil.removeDir(productImageDir);

			ra.addFlashAttribute("message", "Produce with id " + id + " has been deleted successfully");

		} catch (ProductNotFoundException e) {
			ra.addFlashAttribute("message", e.getMessage());
		}

		return "redirect:/products";
	}

	@GetMapping("/products/edit/{id}")
	public String editProduct(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		try {
			Product product = productService.get(id);
			model.addAttribute("product", product);
			model.addAttribute("pageTitle", "Update Product ID(" + id + ")");

			List<Brand> listBrands = brandService.findAll();
			model.addAttribute("listBrands", listBrands);

			Integer numberOfExisitingExtraImages = product.getImages().size();
			model.addAttribute("numberOfExisitingExtraImages", numberOfExisitingExtraImages);

			return "/products/product_form";
		} catch (ProductNotFoundException e) {
			ra.addFlashAttribute("message", e.getMessage());
			return "redirect:/products";
		}
	}
	
	@GetMapping("/products/detail/{id}")
	public String viewProductDetails(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		try {
			Product product = productService.get(id);
			
			model.addAttribute("product", product);
			
			return "/products/product_detail_modal";
		} catch (ProductNotFoundException e) {
			ra.addFlashAttribute("message", e.getMessage());
			return "redirect:/products";
		}
	}

}
