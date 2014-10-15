package com.imaginea.api;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	static String datapath = "./";
	static TessAPI1.TessBaseAPI handle;
	static TessAPI1 api;
	static String language = "eng";
	private static final Logger logger = Logger.getLogger(ImageProcessor.class);
	private static final String img_dir_path = "src/main/resources/Images/";

	public static Map<String, Map<String, ArrayList>> benchmark() {
		logger.info("Reading images");
		Map<String, Map<String, ArrayList>> map = new HashMap<>();

		try {
			Files.walk(Paths.get(img_dir_path)).forEach(filePath -> {

			if (Files.isRegularFile(filePath)) {
				File image = new File(filePath.toString());
				logger.info("Image is sent to the processor");
				Map<String, ArrayList> op = null;
				try {
					op = ImageProcessor.newProcess(image);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("File Name : "+ filePath.getFileName());
				logger.info("Output rendered : " + op);
				map.put(filePath.getFileName().toString(),op);
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
	public static Map<String, ArrayList> newProcess(File imageFile)
			throws FileNotFoundException, IOException {
		Map<String, ArrayList> map = new LinkedHashMap<String, ArrayList>();
		handle = TessAPI1.TessBaseAPICreate();
		System.out.println("TessBaseAPIGetIterator");
		BufferedImage image = ImageIO.read(new FileInputStream(imageFile));
		ByteBuffer buf = ImageIOHelper.convertImageData(image);
		int bpp = image.getColorModel().getPixelSize();
		int bytespp = bpp / 8;
		int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
		TessAPI1.TessBaseAPIInit3(handle, datapath, language);
		TessAPI1.TessBaseAPISetPageSegMode(handle,
				TessAPI1.TessPageSegMode.PSM_AUTO);
		TessAPI1.TessBaseAPISetImage(handle, buf, image.getWidth(),
				image.getHeight(), bytespp, bytespl);
		TessAPI1.TessBaseAPIRecognize(handle, null);
		TessAPI1.TessResultIterator ri = TessAPI1
				.TessBaseAPIGetIterator(handle);
		TessAPI1.TessPageIterator pi = TessAPI1
				.TessResultIteratorGetPageIterator(ri);
		TessAPI1.TessPageIteratorBegin(pi);

		do {
			Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri,
					TessAPI1.TessPageIteratorLevel.RIL_WORD);
			String word = ptr.getString(0);
			TessAPI1.TessDeleteText(ptr);
			float confidence = TessAPI1.TessResultIteratorConfidence(ri,
					TessAPI1.TessPageIteratorLevel.RIL_WORD);
			IntBuffer leftB = IntBuffer.allocate(1);
			IntBuffer topB = IntBuffer.allocate(1);
			IntBuffer rightB = IntBuffer.allocate(1);
			IntBuffer bottomB = IntBuffer.allocate(1);
			TessAPI1.TessPageIteratorBoundingBox(pi,
					TessAPI1.TessPageIteratorLevel.RIL_WORD, leftB, topB,
					rightB, bottomB);
			int left = leftB.get();
			int top = topB.get();
			int right = rightB.get();
			int bottom = bottomB.get();
			ArrayList<Float> list = new ArrayList<Float>();
			/*list.add((float) left);
			list.add((float) top);
			list.add((float) right);
			list.add((float) bottom);*/
			list.add(confidence);
			word = word.replaceAll("[^0-9a-zA-Z\\s]", "");
			if (!word.trim().equals("") && !word.trim().equals("\n") && confidence > 60) {
				map.put(word, list);
			}
		} while (TessAPI1.TessPageIteratorNext(pi,
				TessAPI1.TessPageIteratorLevel.RIL_WORD) == TessAPI1.TRUE);

		return map;

	}

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

				String result = instance.doOCR(binaryFile);
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
