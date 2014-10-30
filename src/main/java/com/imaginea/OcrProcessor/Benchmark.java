package com.imaginea.OcrProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.imaginea.api.Web;

public class Benchmark {
	private static final Logger logger = Logger.getLogger(Web.class);
	private static final String img_dir_path = "src/main/resources/goodImages/";

	/**
	 * Benchmarking for different images are done.
	 * 
	 * @return Benchmarked images and their corresponding Text.
	 */
	public static Map<String, Map<String, List<Float>>> standardImagesSet() {
		logger.info("Reading images");
		Map<String, Map<String, List<Float>>> map = new HashMap<>();

		try {
			Files.walk(Paths.get(img_dir_path)).forEach(filePath -> {

				if (Files.isRegularFile(filePath)) {
					File image = new File(filePath.toString());
					logger.info("Image is sent to the processor");
					Map<String, List<Float>> op = null;
					try {
						op = OCR.newProcess(image);
					} catch (Exception e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("File Name : " + filePath.getFileName());
				logger.info("Output rendered : " + op);
				map.put(filePath.getFileName().toString(), op);
			}
		}	);

		} catch (IOException e) {
			logger.error(e.getCause());
		}

		return map;
	}

}
