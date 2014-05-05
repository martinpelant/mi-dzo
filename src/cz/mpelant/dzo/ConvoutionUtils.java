
package cz.mpelant.dzo;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class ConvoutionUtils {

	public static ImageWrapper testFT(ImageWrapper img) {
		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());

		double[][] imgc = convertToCplx(img);

		fft.complexForward(imgc);
		fft.complexInverse(imgc, true);
		return extractNormalizedImage(imgc);
	}

	public static ImageWrapper convolveDeconvolve(ImageWrapper img, ImageWrapper kernel) {
		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());

		double[][] imgc = convertToCplx(img);
		double[][] kernelc = convertToCplx(kernel);
		normalizeKernel(kernelc);

		fft.complexForward(imgc);
		fft.complexForward(kernelc);
		double[][] result = multiplyComplex(imgc, kernelc);
		fft.complexInverse(result, true);

		//		result = convertToCplx(extractNormalizedImage(result));

		fft.complexForward(result);
		double[][] result2;
		result2 = divideComplex(result, kernelc);

		fft.complexInverse(result2, true);
		ImageWrapper resultImg = extractNormalizedImage(result2);
		// rotateImage(resultImg);
		return resultImg;

	}

	public static ImageWrapper convolve(ImageWrapper img, ImageWrapper kernel) {
		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());

		double[][] imgc = convertToCplx(img);
		double[][] kernelc = convertToCplx(kernel);
		normalizeKernel(kernelc);
		// to01(imgc);
		fft.complexForward(imgc);
		fft.complexForward(kernelc);

		double[][] result = multiplyComplex(imgc, kernelc);

		fft.complexInverse(result, true);
		ImageWrapper resultImg = extractNormalizedImage(result);
		rotateImage(resultImg);
		return resultImg;

	}

	public static ImageWrapper deconvolve(ImageWrapper img, ImageWrapper kernel) {
		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());
		double[][] imgc = convertToCplx(img);
		double[][] kernelc = convertToCplx(kernel);
		normalizeKernel(kernelc);
		// to01(imgc);
		fft.complexForward(imgc);
		fft.complexForward(kernelc);

		double[][] result = divideComplex(imgc, kernelc);

		fft.complexInverse(result, true);
		ImageWrapper resultImg = extractNormalizedImage(result);
		rotateImage(resultImg);

		return resultImg;

	}

	public static ImageWrapper getAmplitude(ImageWrapper img) {

		double[][] c = convertToCplx(img);

		DoubleFFT_2D fft = new DoubleFFT_2D(img.getHeight(), img.getWidth());
		fft.complexForward(c);

		ImageWrapper amplitude = extractNormalizedAmplitude(getAmplitudeFromComplex(c));

		rotateImage(amplitude);
		return amplitude;
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

	private static void to01(double[][] array) {
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[0].length; y++) {
				array[x][y] /= 256;
			}
		}
	}

	private static ImageWrapper extractImage(double[][] imageCplx) {
		int[][] img = new int[imageCplx.length][imageCplx.length];
		for (int x = 0; x < img.length; x++) {
			for (int y = 0; y < img[0].length; y++) {
				img[x][y] = (int) Math.abs(imageCplx[y][x * 2]);
				if (img[x][y] > 255) {
					img[x][y] = 255;
				}
			}
		}
		return new ImageWrapper(img);
	}

	private static ImageWrapper extractNormalizedImage(double[][] imageCplx) {
		double[][] img = new double[imageCplx.length][imageCplx.length];
		for (int x = 0; x < img.length; x++) {
			for (int y = 0; y < img[0].length; y++) {
				img[x][y] = imageCplx[y][x * 2];
			}
		}
		return new ImageWrapper(normalizeArray(img, 255));
	}

	private static void rotateImage(ImageWrapper imgW) {
		int[][] img = imgW.getGrayscaleArray();
		for (int i = 0; i < img.length; i++) {
			rotateArray(img[i], img[i].length / 2);
		}
		rotateArray(img, img.length / 2);
		imgW.setGrayscale(img);
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
				double realTop = (p * r + q * s);
				double imaginaryTop = (q * r - p * s);
				if (dividedBy == 0) {
					if ((p * r + q * s) == 0) {
						result[i][j] = 0;
					} else {
						result[i][j] = Math.signum(realTop) * Double.MAX_VALUE;
					}

					if ((q * r - p * s) == 0) {
						result[i][j + 1] = 0;
					} else {
						result[i][j + 1] = Math.signum(imaginaryTop) * Double.MAX_VALUE;
					}

				} else {
					result[i][j] = realTop / dividedBy;
					result[i][j + 1] = imaginaryTop / dividedBy;
				}

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

	private static int[][] normalizeImageLog(int[][] a) {
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

	private static int[][] normalizeArray(double[][] a, int pMaxValue) {
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

		int[][] normImg = new int[a.length][a[0].length];

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				double value = a[i][j];
				value = (value - minValue) / (maxValue - minValue);
				normImg[i][j] = (int) (value * pMaxValue);
			}
		}

		return normImg;
	}

	private static ImageWrapper extractNormalizedAmplitude(double[][] a) {
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

		return new ImageWrapper(normAmpl);
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
