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
		logger.info("Benchmarking :: processing standard set of images ...");

		Map<String, Map<String, List<Float>>> map = new HashMap<>();
		try {
			Files.walk(Paths.get(Props.STD_IMG_DIR)).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {

					File image = new File(filePath.toString());
					Map<String, List<Float>> op = null;
					try {
						op = OCR.newProcess(image);
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}

					logger.debug("File Name : " + filePath.getFileName());
					logger.debug("Output rendered : " + op);
					map.put(filePath.getFileName().toString(), op);
				}
			});

		} catch (IOException e) {
			logger.error(e.getCause());
		}

		return map;
	}

}
