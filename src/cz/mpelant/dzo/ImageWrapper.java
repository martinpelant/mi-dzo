
package cz.mpelant.dzo;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageWrapper {
	private BufferedImage img;
	private int[][] r, b, g, grayscale;

	public ImageWrapper(BufferedImage img) {
		this.img = img;
	}

	public ImageWrapper(int[][] r, int[][] g, int[][] b) {
		this.r = r;
		this.b = b;
		this.g = g;
	}

	public int getWidth() {
		return getImage().getWidth();
	}

	public int getHeight() {
		return getImage().getHeight();
	}

	public ImageWrapper(int[][] grayscale) {
		this(grayscale, grayscale, grayscale);
		this.grayscale = grayscale;
	}

	public ImageWrapper toGrayscale() {
		int[][] g = getGrayscaleArray();
		return new ImageWrapper(g);
	}

	public void setGrayscale(int[][] grayscale) {
		this.grayscale = grayscale;
		r = grayscale;
		g = grayscale;
		b = grayscale;
		img = null;

	}

	public int[][] getGrayscaleArray() {
		if (grayscale == null) {
			grayscale = rgbToGray(imageToRGBArray(img));
		}
		return grayscale;
	}

	public BufferedImage getImage() {
		if (img == null) {
			img = rgbToImage(r, g, b);
		}
		return img;
	}

	private int[][][] imageToRGBArray(BufferedImage image) {
		int[][][] rgb = new int[image.getWidth()][image.getHeight()][3];

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color color = new Color(image.getRGB(x, y));
				rgb[x][y][0] = color.getRed();
				rgb[x][y][1] = color.getGreen();
				rgb[x][y][2] = color.getBlue();
			}
		}

		return rgb;
	}

	private int[][] rgbToGray(int[][][] rgb) {
		int[][] result = new int[rgb.length][rgb[0].length];
		for (int x = 0; x < result.length; x++) {
			for (int y = 0; y < result[0].length; y++) {
				result[x][y] = (rgb[x][y][0] + rgb[x][y][1] + rgb[x][y][2]) / 3;
			}
		}
		return result;
	}

	private BufferedImage rgbToImage(int[][] r, int[][] g, int[][] b) {
		BufferedImage img = new BufferedImage(r.length, r[0].length, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < r.length; x++) {
			for (int y = 0; y < r[0].length; y++) {
				img.setRGB(x, y, new Color(r[x][y], g[x][y], b[x][y]).getRGB());
			}
		}
		return img;
	}

}
