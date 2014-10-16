package com.imaginea.ocr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.imaginea.api.ImageProcessor;

public class Benchmark {

	private static final Logger logger = Logger.getLogger(Benchmark.class);

	public static Map<String, Map<String, List<Float>>> standardImagesSet() {
		logger.info("Reading images");
		Map<String, Map<String, List<Float>>> map = new HashMap<>();

		try {
			Files.walk(Paths.get(Props.std_img_dir_path)).forEach(filePath -> {

				if (Files.isRegularFile(filePath)) {
					File image = new File(filePath.toString());
					logger.info("Image is sent to the processor");
					Map<String, List<Float>> op = null;
					try {
						op = ImageProcessor.newProcess(image);
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();

					}
					
					logger.info("File Name : " + filePath.getFileName());
					logger.info("Output rendered : " + op);
					map.put(filePath.getFileName().toString(), op);
				}
			});

		} catch (IOException e) {
			logger.error(e.getCause());
		}

		return map;
	}

}
