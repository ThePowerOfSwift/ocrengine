package com.imaginea.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;

import com.imaginea.process.OtsuBinarize;

public class Tessaract {
	private static final Logger logger = Logger.getLogger(Tesseract.class);

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

			Tesseract instance = Tesseract.getInstance();
			
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
