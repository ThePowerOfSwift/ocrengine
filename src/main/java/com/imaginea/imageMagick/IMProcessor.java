package com.imaginea.imageMagick;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import magick.ImageInfo;
import magick.MagickImage;
import magick.util.DisplayImageMetaData;

public class IMProcessor {

	public static BufferedImage preProcess(File path) {
		BufferedImage buffImage = null;
		try {
			BufferedImage input = ImageIO.read(path);
			ImageIO.write(input, "jpg", new File(
					"src/main/resources/ImageMagick/before.jpg"));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			MagickImage Image = new MagickImage();
			Image.readImage(new ImageInfo(path.getAbsolutePath()));
			MagickImage MImage = Image.enhanceImage().reduceNoiseImage(10);

			MImage.setFilter(3); // Triangular filter
			MagickImage set = MImage.autoOrientImage();

			DisplayImageMetaData.displayMagickImage(set);

			buffImage = IMProcessor.magickImageToBufferedImage(set);

			ImageIO.write(buffImage, "jpg", new File(
					"src/main/resources/ImageMagick/after.jpg"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffImage;

	}

	/**
	 * convert from MagickImage to Buffered Image
	 * 
	 * @param magickImage
	 * @return buffered image
	 * @throws Exception
	 */

	public static BufferedImage magickImageToBufferedImage(
			MagickImage magickImage) throws Exception {
		Dimension dim = magickImage.getDimension();
		int size = dim.width * dim.height;
		byte[] pixxels = new byte[size * 3];

		magickImage.dispatchImage(0, 0, dim.width, dim.height, "RGB", pixxels);

		BufferedImage bimage = createInterleavedRGBImage(dim.width, dim.height,
				8, pixxels, false);
		ColorModel cm = bimage.getColorModel();
		Raster raster = bimage.getData();
		WritableRaster writableRaster = null;
		if (raster instanceof WritableRaster) {
			writableRaster = (WritableRaster) raster;
		} else {
			writableRaster = raster.createCompatibleWritableRaster();
		}
		BufferedImage Buffimage = new BufferedImage(cm, writableRaster, false,
				null);

		return Buffimage;

	}

	private static BufferedImage createInterleavedRGBImage(int imageWidth,
			int imageHeight, int imageDepth, byte data[], boolean hasAlpha) {
		int pixelStride, transparency;
		if (hasAlpha) {
			pixelStride = 4;
			transparency = Transparency.BITMASK;
		} else {
			pixelStride = 3;
			transparency = Transparency.OPAQUE;
		}
		int[] numBits = new int[pixelStride];
		int[] bandoffsets = new int[pixelStride];

		for (int i = 0; i < pixelStride; i++) {
			numBits[i] = imageDepth;
			bandoffsets[i] = i;
		}

		ComponentColorModel ccm = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), numBits, hasAlpha,
				false, // Alpha pre-multiplied
				transparency, DataBuffer.TYPE_BYTE);

		PixelInterleavedSampleModel csm = new PixelInterleavedSampleModel(
				DataBuffer.TYPE_BYTE, imageWidth, imageHeight, pixelStride, // Pixel
																			// stride
				imageWidth * pixelStride, // Scanline stride
				bandoffsets);

		DataBuffer dataBuf = new DataBufferByte(data, imageWidth * imageHeight
				* pixelStride);
		WritableRaster wr = Raster.createWritableRaster(csm, dataBuf,
				new Point(0, 0));
		return new BufferedImage(ccm, wr, false, null);
	}

}