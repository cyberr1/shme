package com.shopme.admin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


public class FileUploadUtil {
	private static final Logger LOGGER =LoggerFactory.getLogger(FileUploadUtil.class);
	public static void saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
		Path uploadPath = Paths.get(uploadDir);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		try (InputStream inputStream = multipartFile.getInputStream()) {
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new IOException("Could not save file: " + fileName, e);
		}
	}

	public static void cleanDir(String directory) {
		Path dirPath = Paths.get(directory);
		
		try {
			Files.list(dirPath).forEach(file ->{
				if(!Files.isDirectory(file)) {
					try {
					Files.delete(file);
					}catch(IOException ex) {
						LOGGER.error("Couldn't delete the file: "+file);
//						System.out.println("Couldn't delete the file: "+file);
					}				}
			});
			
		} catch (IOException e) {
			LOGGER.error("Couldn't list directory: "+dirPath);

//			System.out.println("Couldn't list directory: "+dirPath);
		}finally {
			
		}

	}

	public static void removeDir(String categoryDir) {

		cleanDir(categoryDir);
		
		try {
			Files.delete(Paths.get(categoryDir));
		}catch (IOException e) {
			LOGGER.error("Could not remove directory "+categoryDir);
		}
	}

}
