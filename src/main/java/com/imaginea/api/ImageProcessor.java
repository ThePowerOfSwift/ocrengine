package com.imaginea.api;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.log4j.Logger;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;

import com.imaginea.process.OtsuBinarize;

/*
 *   Implements OCR on a Image and recognizes the text in it.
 *  
 *  
 */

public class ImageProcessor {

	private static final Logger logger = Logger.getLogger(ImageProcessor.class);
	private static final String img_dir_path = "src/test/resources/ima/goodImages/";

	public static Map<String, Map<String, String>> benchmark() {
		logger.info("Reading images");
		Map<String, Map<String, String>> map = new HashMap<>();

		try {
			Files.walk(Paths.get(img_dir_path)).forEach(filePath -> {

				if (Files.isRegularFile(filePath)) {
					File image = new File(filePath.toString());

					logger.info("Image is sent to the processor");
					Map<String, String> op = ImageProcessor.process(image);

					// Log the below
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

	public static void main(String args[]) {

		setPort(4565);
		staticFileLocation("/goodImages");
		get("/test", (request, response) -> {
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

						return process(targetFile);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					//
					return "";

				}

		);

	}

	/*
	 * Basic code of Tesseract-OCR,Reads the image and gives the characters from
	 * it.
	 */
	public static Map<String, String> process(File imageFile) {
		try {

			BufferedImage inputImage = ImageIO.read(imageFile);
			BufferedImage grayScale = OtsuBinarize.toGray(inputImage);
			BufferedImage binaryImage = OtsuBinarize.binarize(grayScale);

			File binaryFile = new File("tempBinary-otsu.jpg");
			ImageIO.write(binaryImage, "jpg", binaryFile);

			FastBitmap fb = new FastBitmap(inputImage);

			if (fb.isRGB()) {
				fb.toGrayscale();
			}

			BradleyLocalThreshold bradley = new BradleyLocalThreshold();
			logger.info("Processing Bradley");
			bradley.setPixelBrightnessDifferenceLimit(0.05f);
			bradley.setWindowSize(10);

			bradley.applyInPlace(fb);
			logger.info("Done Bradley");
			BufferedImage outputImage = fb.toBufferedImage();
			File binaryFile1 = new File("tempBinary-bradley-0.1.jpg");

			ImageIO.write(outputImage, "jpg", binaryFile1);

			Tesseract instance = Tesseract.getInstance(); //

			// instance.doOCR(new File("file:///home/uttam/Desktop/images"));

			try {

				String result = instance.doOCR(imageFile);
				String[] results = result.split("\n");
				int i = 0;
				Map<String, String> licenseInfo = new HashMap<>();
				Map<String, String> genericInfo = new HashMap<>();

				while (i < results.length) {
					String temp = results[i++];
					temp = temp.replaceAll("[^0-9a-zA-Z\\s]", "");
					if (!temp.trim().equals("") && !temp.trim().equals("\n")) {

						genericInfo.put("word - " + i, temp);

						if (temp.contains("Nam")) {
							temp = temp.substring(temp.indexOf("Nam") + 5)
									.trim();
							licenseInfo.put("firstname", temp);
						}

						if (temp.contains("of")) {
							temp = temp.substring(temp.indexOf("of") + 4)
									.trim();
							licenseInfo.put("lastname", temp);
						}

						if (temp.contains("Address")) {
							temp = results[i++] + " ";
							temp += results[i++];
							licenseInfo.put("address", temp);
						}

					}

				}

				System.out.println(result);

				licenseInfo.putAll(genericInfo);

				return licenseInfo;
			} catch (TesseractException e) {
				System.err.println(e.getMessage());
			} catch (Exception e) {
				logger.error("Exception occurred :", e);
				System.err.println(e.getMessage());
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
