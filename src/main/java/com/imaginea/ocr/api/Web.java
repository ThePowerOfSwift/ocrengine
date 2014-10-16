package com.imaginea.ocr.api;

import static com.imaginea.ocr.Props.static_file_loc;
import static com.imaginea.ocr.Props.web_port;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import com.imaginea.ocr.Benchmark;

public class Web {

	private static final Logger logger = Logger.getLogger(Web.class);

	public static void main(String args[]) {

		setPort(web_port);
		staticFileLocation(static_file_loc);

		/* --- Web API End Points --- */
		
		// process a given file
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

//				return process(targetFile);
				return null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//
				return "";

			}

		);
		
		// Benchmark results 
		get("/benchmark", (request, response) -> {
			Map<String, Object> attrs = new HashMap<>();
			attrs.put("attrs", Benchmark.standardImagesSet());
	
			return new ModelAndView(attrs, "benchmark.html");
		}, new FreeMarkerEngine());
		
		// Health Check
		get("/test", (request, response) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("message", "Hello World!");
			return new ModelAndView(attributes, "index.html");
		}, new FreeMarkerEngine());

	}

}
