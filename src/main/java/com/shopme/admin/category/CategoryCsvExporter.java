package com.shopme.admin.category;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.formula.functions.Replace;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.shopme.admin.user.export.AbstractExporter;
import com.shopme.common.entity.Category;

public class CategoryCsvExporter extends AbstractExporter {

	public void export(List<Category> listCategory, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "text/csv", ".csv");
		
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		String[] csvHeader = {"Category ID", "Category Name" };
		String[] fieldMapping= {"id", "name"};
		
		csvWriter.writeHeader(csvHeader);
		
		for(Category category : listCategory) {
			//category.setName(category.getName().Replace("--", "  "));
			csvWriter.write(category, fieldMapping);
		}
		csvWriter.close();
	}
}
