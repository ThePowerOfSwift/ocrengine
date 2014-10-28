package com.imaginea.api;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageIOHelper;

import org.apache.log4j.Logger;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;

import com.imaginea.process.OtsuBinarize;
import com.sun.jna.Pointer;

/*
 *   Implements OCR on a Image and recognizes the text in it.
 *  
 *  
 */

public class ImageProcessor {

	
	private static final Logger logger = Logger.getLogger(ImageProcessor.class);
	private static final String img_dir_path = "src/main/resources/goodImages/";

	public static Map<String, Map<String, List<Float>>> benchmark() {
		logger.info("Reading images");
		Map<String, Map<String, List<Float>>> map = new HashMap<>();

		try {
			Files.walk(Paths.get(img_dir_path)).forEach(filePath -> {

				if (Files.isRegularFile(filePath)) {
					File image = new File(filePath.toString());
					logger.info("Image is sent to the processor");
					Map<String, List<Float>> op = null;
					try {
						op = Processor.newProcess(image);
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

	public static void main(String args[]) {

		setPort(4565);
		staticFileLocation("/goodImages");
		get("/", (request, response) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("message", "Hello World!");

			// The hello.ftl file is located in directory:
			// src/test/resources/spark/template/freemarker
				return new ModelAndView(attributes, "index.html");
			}, new FreeMarkerEngine());

		get("/benchmark", (request, response) -> {

			Map<String, Object> attrs = new HashMap<>();
			attrs.put("attrs", benchmark());

			return new ModelAndView(attrs, "benchmark.html");

		}, new FreeMarkerEngine());

		post("/process",
				(req, res) -> {

					MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
							"/tmp");

					req.raw().setAttribute("org.eclipse.multipartConfig",
							multipartConfigElement);
					try {
						Part file = req.raw().getPart("file");

						InputStream initialStream = file.getInputStream();

						File targetFile = new File(
								"src/main/resources/targetFile.jpg");
						OutputStream outStream = new FileOutputStream(
								targetFile);

						byte[] buffer = new byte[8 * 1024];
						int bytesRead;
						while ((bytesRead = initialStream.read(buffer)) != -1) {
							outStream.write(buffer, 0, bytesRead);
						}

						outStream.close();

						// return process(targetFile);
						return Processor.newProcess(targetFile);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					//
					return "";

				}

		);

	}


	
}
