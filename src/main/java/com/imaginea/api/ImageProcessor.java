package com.imaginea.api;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

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
		Tesseract instance = Tesseract.getInstance(); //

		try {

			String result = instance.doOCR(imageFile);
			System.out.println(result);
			return result;
		} catch (TesseractException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return "No Output";

	}
	
}
