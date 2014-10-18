package com.imaginea.imageMagick;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class DPI {
       
	
	
	public static void saveGridImage(File output,BufferedImage input) throws IOException {
	    output.delete();
BufferedImage gridImage =input; 
	    final String formatName = "jpeg";

	    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
	       ImageWriter writer = iw.next();
	       ImageWriteParam writeParam = writer.getDefaultWriteParam();
	       ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
	       IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
	       if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
	          continue;
	       }

	       setDPI(metadata);

	       final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
	       try {
	          writer.setOutput(stream);
	          writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
	       } finally {
	          stream.close();
	       }
	       break;
	    }
	 }

	 private static void setDPI(IIOMetadata metadata) throws IIOInvalidTreeException {

	    // for PMG, it's dots per millimeter
	    double dotsPerMilli = 300 ;

	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    root.appendChild(dim);

	    metadata.mergeTree("javax_imageio_1.0", root);
	 }
}
