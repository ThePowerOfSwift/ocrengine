package com.imaginea.api;

import static spark.Spark.get;
import static spark.Spark.post;

import java.awt.Color;
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

import com.google.gson.Gson;
import com.imaginea.process.OtsuBinarize;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;



public class ImageProcessor {

	public static void main(String args[]){
		
		
		get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            // The hello.ftl file is located in directory:
            // src/test/resources/spark/template/freemarker
            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());
		
		post("/hello", (req, res) -> {			
		
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
			
			File binaryFile = new File("tempBinaryFile.jpg");
			ImageIO.write(binaryImage, "jpg", binaryFile);
		
		
		Tesseract instance = Tesseract.getInstance(); //

		try {

			String result = instance.doOCR(binaryFile);
			String[] results = result.split("\n");
			int i=0;
			int level = 0;
			
			Map<String, String> licenseInfo = new HashMap<>();
			while (i < results.length){
				String temp = results[i++];
				temp = temp.replaceAll("[^0-9a-zA-Z\\s]", "");
				if (!temp.trim().equals("") && !temp.trim().equals("\n")){
					
					if (level == 0){
						licenseInfo.put("header", temp);
						level ++;	
						continue;
					}
					
					if (level == 1){
						licenseInfo.put("form-info", temp);
						level ++;
						continue;
					}
					
					/*if (level == 2){
						level ++;
						continue;
					}*/
					
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
					
					/*if (level == 4){
						level ++;
						continue;
					}*/
					
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
					}
					
				}
								
			}
			
			
			System.out.println(result);
			
			//result = result.replaceAll("[^a-zA-Z0-9\b]","");
			Gson gson = new Gson();
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
