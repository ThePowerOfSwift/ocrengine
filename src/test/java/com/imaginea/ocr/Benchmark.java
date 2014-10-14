package com.imaginea.ocr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.imaginea.api.ImageProcessor;

public class Benchmark {

	private static final String img_dir_path = "src/test/resources/ima/goodImages/";
	private static final Logger logger = Logger.getLogger(Benchmark.class);

	//@Test
	/*public void process_image() {
		System.out.println(sample_img);

		File img = new File(sample_img);
		String op = ImageProcessor.process(img);

		System.out.println(op);

	}*/

	@Test
	public void benchmark() throws IOException {
		logger.info("Reading images");
		Map<String, String> map = new HashMap<String, String>();
		Files.walk(Paths.get(img_dir_path)).forEach(filePath -> {
			
			if (Files.isRegularFile(filePath)) {
				File image = new File(filePath.toString());

				logger.info("Image is sent to the processor");
				Map<String, String> op = ImageProcessor.process(image);

				// Log the below
				logger.info("File Name : " + filePath.getFileName());
				logger.info("Output rendered : " + op);
				map.put(filePath.getFileName().toString(), op.toString());
				
				// TODO Update this 
			}
		});

	}

}
