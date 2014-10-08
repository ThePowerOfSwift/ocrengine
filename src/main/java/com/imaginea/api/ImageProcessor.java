package com.imaginea.api;

import static spark.Spark.get;
import static spark.Spark.post;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import javax.swing.JOptionPane;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;

import com.google.gson.Gson;
import com.imaginea.process.OtsuBinarize;



public class ImageProcessor {

	public static void main(String args[]){
		
		
		get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            // The hello.ftl file is located in directory:
            // src/test/resources/spark/template/freemarker
            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());
		
		post("/process", (req, res) -> {			
		
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
			   req.raw().setAttribute("org.eclipse.multipartConfig", multipartConfigElement);			  
			   try {
				Part file = req.raw().getPart("file");
				
				InputStream initialStream = file.getInputStream();
					 
				File targetFile = new File("src/main/resources/targetFile.jpg");
			    OutputStream outStream = new FileOutputStream(targetFile);
			 
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
	
	public static String process(File imageFile){
		//File imageFile = new File(filePath);
		try{
		
			BufferedImage inputImage = ImageIO.read(imageFile);
			BufferedImage grayScale = OtsuBinarize.toGray(inputImage);
			BufferedImage binaryImage = OtsuBinarize.binarize(grayScale);
			
			File binaryFile = new File("tempBinary-otsu.jpg");
			ImageIO.write(binaryImage, "jpg", binaryFile);
		
		
			FastBitmap fb = new FastBitmap(inputImage);
	       // fb.toRGB();
	       // JOptionPane.showMessageDialog(null, fb.toIcon(), "Original image", -1);
	        fb.toGrayscale();
	        BradleyLocalThreshold bradley = new BradleyLocalThreshold();
	        
	        bradley.setPixelBrightnessDifferenceLimit(0.1f);
	        bradley.setWindowSize(15);
	        
	        bradley.applyInPlace(fb);
	        
	        BufferedImage outputImage = fb.toBufferedImage();
	    	File binaryFile1 = new File("tempBinary-bradley-0.1.jpg");
	    	ImageIO.write(outputImage, "jpg", binaryFile1);
			
	       // JOptionPane.showMessageDialog(null, fb.toIcon(), "Result", -1);
			
		Tesseract instance = Tesseract.getInstance(); //

		try {

			String result = instance.doOCR(binaryFile1);
			String[] results = result.split("\n");
			int i=0;
			int level = 0;
			
			Map<String, String> licenseInfo = new HashMap<>();
			Map<String, String> genericInfo = new HashMap<>();
			
			while (i < results.length){
				String temp = results[i++];
				temp = temp.replaceAll("[^0-9a-zA-Z\\s]", "");
				if (!temp.trim().equals("") && !temp.trim().equals("\n")){
					
					genericInfo.put("word - "+i, temp);
					
					if (temp.contains("Nam")){
						temp = temp.substring(temp.indexOf("Nam")+5).trim();
						licenseInfo.put("firstname", temp);
					}
					
					if (temp.contains("of")){
						temp = temp.substring(temp.indexOf("of")+4).trim();
						licenseInfo.put("lastname", temp);
					}
					
					if (temp.contains("Address")){
						temp = results[i++]+" ";
						temp += results[i++];
						licenseInfo.put("address", temp);
					}
					
					/*if (level == 0){
						licenseInfo.put("header", temp);
						level ++;	
						continue;
					}
					
					if (level == 1){
						licenseInfo.put("form-info", temp);
						level ++;
						continue;
					}
					
					if (level == 2){
						level ++;
						continue;
					}
					
					if (level == 2){
						licenseInfo.put("firstname", temp);
						level ++;
						continue;
					}
					
					if (level == 3){
						licenseInfo.put("lastname", temp);
						level ++;
						continue;
					}
					
					if (level == 4){
						level ++;
						continue;
					}
					
					if (level == 4){
						licenseInfo.put("addr1", temp);
						level ++;
						continue;
					}
					
					if (level == 5){
						licenseInfo.put("addr2", temp);
						level ++;
						continue;
					}
					
					if (level == 6){
						licenseInfo.put("addr3", temp);
						level ++;
						continue;
					}*/
					
				}
								
			}
			
			
			System.out.println(result);
			
			//result = result.replaceAll("[^a-zA-Z0-9\b]","");
			Gson gson = new Gson();

			licenseInfo.putAll(genericInfo);			
			return gson.toJson(licenseInfo);
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		}
		return "No Output";
		}catch(Exception e){
			return "No Output";
		}
	}
	
}
