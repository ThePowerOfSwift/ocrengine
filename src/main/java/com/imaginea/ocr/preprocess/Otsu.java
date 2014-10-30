package com.imaginea.ocr.preprocess;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class Otsu {

	private static final Logger logger = Logger.getLogger(Otsu.class);

	private static BufferedImage original, grayscale, binarized;

	public static void main(String[] args) throws IOException {

		File original_f = new File(
				"src/main/resources/testImages/IMG8_good.JPG");
		String output_f = "src/main/resources/testImages/TestImage-code";
		original = ImageIO.read(original_f);
		grayscale = toGray(original);
		binarized = binarize(grayscale);
		writeImage(output_f);

	}

	private static void writeImage(String output) throws IOException {
		File file = new File(output + ".jpg");
		ImageIO.write(binarized, "jpg", file);
	}

	/**
	 * Return histogram of grayscale image
	 * 
	 * @param input
	 *            image
	 * @return image Histogram
	 */

	public static int[] imageHistogram(BufferedImage input) {
		logger.info("processing Histrogram");
		int[] histogram = new int[256];

		for (int i = 0; i < histogram.length; i++)
			histogram[i] = 0;

		for (int i = 0; i < input.getWidth(); i++) {
			for (int j = 0; j < input.getHeight(); j++) {
				int red = new Color(input.getRGB(i, j)).getRed();
				histogram[red]++;
			}
		}
		logger.info("Done Histrogram");
		return histogram;

	}

	/**
	 * The luminance method returns grayscale image for a colored one
	 * 
	 * @param original
	 * @return grayscale image
	 */
	public static BufferedImage toGray(BufferedImage original) {

		int alpha, red, green, blue;
		int newPixel;

		BufferedImage lum = new BufferedImage(original.getWidth(),
				original.getHeight(), original.getType());
		try {
			logger.info("converting to gray scale");
			for (int i = 0; i < original.getWidth(); i++) {
				for (int j = 0; j < original.getHeight(); j++) {

					// Get pixels by R, G, B
					alpha = new Color(original.getRGB(i, j)).getAlpha();
					red = new Color(original.getRGB(i, j)).getRed();
					green = new Color(original.getRGB(i, j)).getGreen();
					blue = new Color(original.getRGB(i, j)).getBlue();

					red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
					// Return back to original format
					newPixel = colorToRGB(alpha, red, red, red);

					// Write pixels into image
					lum.setRGB(i, j, newPixel);

				}
			}
			logger.info("converted to grayscale");
			return lum;

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Image is not RGB");
			return null;
		}

	}

	// Get binary treshold using Otsu's method
	private static int otsuTreshold(BufferedImage original) {

		logger.info("Processing otsu Thresholding");

		int[] histogram = imageHistogram(original);
		int total = original.getHeight() * original.getWidth();

		float sum = 0;
		for (int i = 0; i < 256; i++)
			sum += i * histogram[i];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		int threshold = 0;
		for (int i = 0; i < 256; i++) {
			wB += histogram[i];
			if (wB == 0)
				continue;
			wF = total - wB;

			if (wF == 0)
				break;

			sumB += (float) (i * histogram[i]);
			float mB = sumB / wB;
			float mF = (sum - sumB) / wF;

			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = i;
			}

		}

		logger.info("Done OTSU Thresholding");
		return threshold;

	}

	/**
	 * 
	 * @param original
	 * @return otsu binarized image
	 */
	public static BufferedImage binarize(BufferedImage original) {

		try {
			logger.info("Binarizaing Image");
			int red;
			int newPixel;

			int threshold = otsuTreshold(original);

			System.out
					.println("========================>>>> OTSU Threshold value :"
							+ threshold);

			BufferedImage binarized = new BufferedImage(original.getWidth(),
					original.getHeight(), original.getType());

			for (int i = 0; i < original.getWidth(); i++) {
				for (int j = 0; j < original.getHeight(); j++) {

					// Get pixels
					red = new Color(original.getRGB(i, j)).getRed();
					int alpha = new Color(original.getRGB(i, j)).getAlpha();
					if (red > threshold) {
						newPixel = 255;
					} else {
						newPixel = 0;
					}
					newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
					binarized.setRGB(i, j, newPixel);

				}
			}
			logger.info("Done Binarization");
			return binarized;
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e);
			return null;
		}

	}

	// Convert R, G, B, Alpha to standard 8 bit
	private static int colorToRGB(int alpha, int red, int green, int blue) {

		int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red;
		newPixel = newPixel << 8;
		newPixel += green;
		newPixel = newPixel << 8;
		newPixel += blue;

		return newPixel;

	}

}
