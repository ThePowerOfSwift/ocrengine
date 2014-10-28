package com.imaginea.ocr;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
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

import com.imaginea.api.Processor;
import com.imaginea.process.OtsuBinarize;
import com.sun.jna.Pointer;

public class OCR {

	private static final Logger logger = Logger.getLogger(OCR.class);

	static TessAPI1.TessBaseAPI handle;
	static TessAPI1 api;
	static String language = "eng";
	static String datapath = "./";
		/*
	 * Basic code of Tesseract-OCR,Reads the image and gives the characters from
	 * it.
	 */
	public static Map<String, List<Float>> newProcess(File imageFile)
			throws FileNotFoundException, IOException {
		Map<String, List<Float>> map = new LinkedHashMap<>();
	//	Map<String, List<Float>> map2 = new LinkedHashMap<>();
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

		String pathToFile = imageFile.getPath().concat("txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(pathToFile));
		float meanConfidence = 0;
		int counter = 0;
		ArrayList<Float> list;
		do {
			Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri,
					TessAPI1.TessPageIteratorLevel.RIL_TEXTLINE);
			String word = ptr.getString(0);

			TessAPI1.TessDeleteText(ptr);
			float LineConfidence = TessAPI1.TessResultIteratorConfidence(ri,
					TessAPI1.TessPageIteratorLevel.RIL_TEXTLINE);

			IntBuffer leftB = IntBuffer.allocate(1);
			IntBuffer topB = IntBuffer.allocate(1);
			IntBuffer rightB = IntBuffer.allocate(1);
			IntBuffer bottomB = IntBuffer.allocate(1);

			TessAPI1.TessPageIteratorBoundingBox(pi,
					TessAPI1.TessPageIteratorLevel.RIL_TEXTLINE, leftB, topB,
					rightB, bottomB);
			list = new ArrayList<Float>();
		
			
			word = word.replaceAll("[^0-9a-zA-Z\\s]", "");
			String line = word + "-" + String.valueOf(LineConfidence);
			bw.write(line);
			bw.write("\n");

			if (!word.trim().equals("") && !word.trim().equals("\n")&& LineConfidence>=55 && word.length()>=5) {
				list.add(LineConfidence);
				meanConfidence += LineConfidence;
				counter++;
				map.put(word, list);
			}

		} while (TessAPI1.TessPageIteratorNext(pi,
				TessAPI1.TessPageIteratorLevel.RIL_TEXTLINE) == TessAPI1.TRUE);

		meanConfidence = meanConfidence / counter;
		String name = imageFile.getName().concat("-binary");
		TessAPI1.TessBaseAPIDumpPGM(handle, name);
		bw.close();
		ArrayList<Float> meanConfidenceList = new ArrayList<Float>();
		meanConfidenceList.add(meanConfidence);
		map.put("meanConfidence", meanConfidenceList);
		System.out.println("========================================>> ");
		Collections.sort(list);
		//System.out.println(list.get(0));
		if (meanConfidence >= 65 && map.size()>=4 ) 
			return map;
			return null;
	}

}
