
package cz.mpelant.dzo;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class ConvoutionUtils {

	public static ImageWrapper convolve(ImageWrapper img, ImageWrapper kernel) {
		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());

		double[][] imgc = convertToCplx(img);
		double[][] kernelc = convertToCplx(kernel);
		normalizeKernel(kernelc);

		fft.complexForward(imgc);
		fft.complexForward(kernelc);

		double[][] result = multiplyComplex(imgc, kernelc);

		fft.complexInverse(result, true);
		int[][] resultImg = extractImage(result);
		rotateImage(resultImg);
		return new ImageWrapper(resultImg);

	}

	public static ImageWrapper deconvolve(ImageWrapper img, ImageWrapper kernel) {
		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());

		double[][] imgc = convertToCplx(img);
		double[][] kernelc = convertToCplx(kernel);
		normalizeKernel(kernelc);

		fft.complexForward(imgc);
		fft.complexForward(kernelc);

		double[][] result = divideComplex(imgc, kernelc);

		fft.complexInverse(result, true);

		int[][] resultImg = extractImage(result);
		rotateImage(resultImg);

		return new ImageWrapper(resultImg);

	}

	public static ImageWrapper getAmplitude(ImageWrapper img) {

		double[][] c = convertToCplx(img);

		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());
		fft.complexForward(c);

		int[][] amplitude = normalizeAmplitude(getAmplitudeFromComplex(c));

		rotateImage(amplitude);
		return new ImageWrapper(amplitude);
	}

	private static double[][] convertToCplx(ImageWrapper img) {
		int[][] image = img.getGrayscaleArray();
		double[][] result = new double[image[0].length][image.length * 2];
		for (int x = 0; x < image.length; x++) {
			for (int y = 0; y < image[0].length; y++) {
				result[y][x * 2] = image[x][y];
			}
		}
		return result;
	}

	private static int[][] extractImage(double[][] imageCplx) {
		int[][] img = new int[imageCplx.length][imageCplx.length];
		for (int x = 0; x < img.length; x++) {
			for (int y = 0; y < img[0].length; y++) {
				img[x][y] = (int) Math.abs(imageCplx[y][x * 2]);
				if (img[x][y] > 255) {
					img[x][y] = 255;
				}
			}
		}
		return img;
	}

	private static int[][] extractNormalizedImage(double[][] imageCplx) {
		int[][] img = new int[imageCplx.length][imageCplx.length];
		for (int x = 0; x < img.length; x++) {
			for (int y = 0; y < img[0].length; y++) {
				img[x][y] = (int) imageCplx[y][x * 2];
			}
		}
		return normalizeImage(img);
	}

	private static void rotateImage(int[][] img) {
		for (int i = 0; i < img.length; i++) {
			rotateArray(img[i], img[i].length / 2);
		}
		rotateArray(img, img.length / 2);
	}

	private static double[][] multiplyComplex(double[][] x, double[][] y) {
		double[][] result = new double[x.length][x[0].length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[0].length; j += 2) {
				double a = x[i][j];
				double b = x[i][j + 1];
				double c = y[i][j];
				double d = y[i][j + 1];
				result[i][j] = a * c - b * d;
				result[i][j + 1] = a * d + b * c;
			}
		}
		return result;
	}

	private static double[][] divideComplex(double[][] x, double[][] y) {
		double[][] result = new double[x.length][x[0].length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[0].length; j += 2) {
				double p = x[i][j];
				double q = x[i][j + 1];
				double r = y[i][j];
				double s = y[i][j + 1];

				double dividedBy = (Math.pow(r, 2) + Math.pow(s, 2));
				if (dividedBy == 0) {
					dividedBy = 0.000000001;
				}
				result[i][j] = (p * r + q * s) / dividedBy;
				result[i][j + 1] = (q * r - p * s) / dividedBy;

			}
		}
		return result;
	}

	private static void normalizeKernel(double[][] kernelCplx) {
		double sum = 0;
		for (int i = 0; i < kernelCplx.length; i++) {
			for (int j = 0; j < kernelCplx[0].length; j += 2) {
				sum += kernelCplx[i][j];
			}
		}

		double scale = 1.0 / sum;
		for (int i = 0; i < kernelCplx.length; i++) {
			for (int j = 0; j < kernelCplx[0].length; j += 2) {
				kernelCplx[i][j] *= scale;
			}
		}
	}

	private static double amplitude(double real, double imaginary) {
		return Math.sqrt(Math.pow(real, 2) + Math.pow(imaginary, 2));
	}

	private static double[][] getAmplitudeFromComplex(double[][] c) {
		double[][] ampl = new double[c.length][c[0].length / 2];
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c[0].length; j += 2) {
				double real = c[i][j];
				double imag = c[i][j + 1];
				ampl[i][j / 2] = amplitude(real, imag);
			}
		}
		return ampl;
	}


	private static int[][] normalizeImage(int[][] a) {
		int minValue = Integer.MAX_VALUE;
		int maxValue = Integer.MIN_VALUE;

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				int value = a[i][j];
				if (value < minValue) {
					minValue = value;
				}
				if (value > maxValue) {
					maxValue = value;
				}
			}
		}

		int[][] normImg = new int[a.length][a[0].length];

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				double value = a[i][j];
				value = Math.log((value - minValue) + 1) / Math.log(maxValue - minValue + 1);
				normImg[i][j] = (int) (value * 255);
			}
		}

		return normImg;
	}



	private static int[][] normalizeAmplitude(double[][] a) {
		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				double value = a[i][j];
				if (value < minValue) {
					minValue = value;
				}
				if (value > maxValue) {
					maxValue = value;
				}
			}
		}

		int[][] normAmpl = new int[a.length][a[0].length];

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				double value = a[i][j];
				value = Math.log((value - minValue) + 1) / Math.log(maxValue - minValue + 1);
				normAmpl[i][j] = (int) (value * 255);
			}
		}

		return normAmpl;
	}

	private static void rotateArray(int[] array, int index) {
		int[] result;

		result = new int[array.length];

		System.arraycopy(array, index, result, 0, array.length - index);
		System.arraycopy(array, 0, result, array.length - index, index);

		System.arraycopy(result, 0, array, 0, array.length);
	}

	private static void rotateArray(int[][] array, int index) {
		int[][] result;

		result = new int[array.length][];

		System.arraycopy(array, index, result, 0, array.length - index);
		System.arraycopy(array, 0, result, array.length - index, index);

		System.arraycopy(result, 0, array, 0, array.length);
	}

}
