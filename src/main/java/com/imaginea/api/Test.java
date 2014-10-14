package com.imaginea.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Files.walk(Paths.get("/home/manojkumar/Downloads/images/can")).forEach(filePath -> {
		    if (Files.isRegularFile(filePath)) {
		        System.out.println(filePath);
		        File binaryFile = new File("filePath");
		    }
		});
	}

}
