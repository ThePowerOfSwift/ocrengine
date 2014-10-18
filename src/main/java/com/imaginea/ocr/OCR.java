package com.imaginea.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageIOHelper;

import org.apache.log4j.Logger;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;

import com.imaginea.process.OtsuBinarize;
import com.sun.jna.Pointer;

public class OCR {

	private static final Logger logger = Logger.getLogger(OCR.class);

	// Tessaract
	private static TessAPI1.TessBaseAPI tessHandle;
	private static TessAPI1 tessApi;

	public static Map<String, String> process(File imFile) {
		try {

			BufferedImage inputImage = ImageIO.read(imFile);
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
							temp = temp.substring(temp.indexOf("Nam") + 5).trim();
							licenseInfo.put("firstname", temp);
						}

						if (temp.contains("of")) {
							temp = temp.substring(temp.indexOf("of") + 4).trim();
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

	public static Map<String, List<Float>> processForConfidenceValues(File imageFile) {
		try {
			Map<String, List<Float>> map = new LinkedHashMap<>();
			tessHandle = TessAPI1.TessBaseAPICreate();
			System.out.println("TessBaseAPIGetIterator");
			BufferedImage image = ImageIO.read(new FileInputStream(imageFile));
			ByteBuffer buf = ImageIOHelper.convertImageData(image);
			int bpp = image.getColorModel().getPixelSize();
			int bytespp = bpp / 8;
			int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
			TessAPI1.TessBaseAPIInit3(tessHandle, Props.TRAIN_DATA_DIR, Props.OCR_LANG);
			TessAPI1.TessBaseAPISetPageSegMode(tessHandle, TessAPI1.TessPageSegMode.PSM_AUTO);
			TessAPI1.TessBaseAPISetImage(tessHandle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
			TessAPI1.TessBaseAPIRecognize(tessHandle, null);
			TessAPI1.TessResultIterator ri = TessAPI1.TessBaseAPIGetIterator(tessHandle);
			TessAPI1.TessPageIterator pi = TessAPI1.TessResultIteratorGetPageIterator(ri);
			TessAPI1.TessPageIteratorBegin(pi);
			do {
				Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri, TessAPI1.TessPageIteratorLevel.RIL_WORD);
				String word = ptr.getString(0);
				TessAPI1.TessDeleteText(ptr);
				float confidence = TessAPI1.TessResultIteratorConfidence(ri, TessAPI1.TessPageIteratorLevel.RIL_WORD);
				IntBuffer leftB = IntBuffer.allocate(1);
				IntBuffer topB = IntBuffer.allocate(1);
				IntBuffer rightB = IntBuffer.allocate(1);
				IntBuffer bottomB = IntBuffer.allocate(1);
				TessAPI1.TessPageIteratorBoundingBox(pi, TessAPI1.TessPageIteratorLevel.RIL_WORD, leftB, topB, rightB,
						bottomB);
				int left = leftB.get();
				int top = topB.get();
				int right = rightB.get();
				int bottom = bottomB.get();
				ArrayList<Float> list = new ArrayList<Float>();
				/*
				 * list.add((float) left); list.add((float) top); list.add((float) right); list.add((float) bottom);
				 */
				list.add(confidence);
				word = word.replaceAll("[^0-9a-zA-Z\\s]", "");
				if (!word.trim().equals("") && !word.trim().equals("\n") && confidence > 60) {
					map.put(word, list);
				}
			} while (TessAPI1.TessPageIteratorNext(pi, TessAPI1.TessPageIteratorLevel.RIL_WORD) == TessAPI1.TRUE);
			return map;
		} catch (Exception e) {
			// TODO: handle exception
		}

		return null;

	}

}
