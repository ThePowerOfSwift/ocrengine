package com.imaginea.ocr.api;

import static com.imaginea.ocr.Props.STATIC_FILE_LOC;
import static com.imaginea.ocr.Props.WEB_PORT;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import com.imaginea.api.ImageProcessor;
import com.imaginea.ocr.Benchmark;
import com.imaginea.ocr.OCR;
import com.imaginea.ocr.Props;

public class Web {

	private static final Logger logger = Logger.getLogger(Web.class);

	// Constants
	private static final String MULTIPART_CONFIG = "org.eclipse.multipartConfig";

	public static void main(String args[]) {

		setPort(WEB_PORT);
		staticFileLocation(STATIC_FILE_LOC);

		/* --- Web API End Points --- */

		// process a given file
		post("/process", (req, res) -> {
			logger.info("Web request : /process");

			try {
				MultipartConfigElement multipartConfigElement = new MultipartConfigElement(Props.TEMP_DIR);
				req.raw().setAttribute(MULTIPART_CONFIG, multipartConfigElement);

				Part part = req.raw().getPart("file");
				File imFile = extractFile(part);

				// Process the file
				return OCR.process(imFile);

			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

			return null;

		});

		// Benchmark results
		get("/benchmark", (request, response) -> {
			logger.info("Web request : /benchmark");

			Map<String, Object> attrs = new HashMap<>();
			attrs.put("attrs", Benchmark.standardImagesSet());

			return new ModelAndView(attrs, "benchmark.html");
		}, new FreeMarkerEngine());

		// Health Check
		get("/test", (request, response) -> {
			logger.info("Web request : /test");

			Map<String, Object> attributes = new HashMap<>();
			attributes.put("message", "Hello World!");

			return new ModelAndView(attributes, "index.html");
		}, new FreeMarkerEngine());

	}

	/**
	 * Extracts file file from "Part"
	 */
	private static File extractFile(Part file) {
		File targetFile = null;
		try {
			String tempFileName = "temp-" + System.currentTimeMillis() + ".jpg";
			targetFile = new File(tempFileName);

			InputStream initialStream = file.getInputStream();
			OutputStream outStream = new FileOutputStream(targetFile);

			byte[] buffer = new byte[8 * 1024];
			int bytesRead;
			while ((bytesRead = initialStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}

			outStream.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return targetFile;
	}

}
