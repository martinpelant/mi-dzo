
package cz.mpelant.dzo;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {
	public static final String TAG = "Main";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String kernelFname = "circle.png";
		blurDeblur(kernelFname);
		blurImage(kernelFname);
		deblurImage(kernelFname);
		amplitude(kernelFname);

	}



	private static void amplitude(String fname) throws IOException {
		ImageWrapper src = loadImage(fname);

		ImageWrapper out = ConvoutionUtils.getAmplitude(src);

		saveImageAsPng(out, "outBlurAmplitude");
	}


	private static void blurDeblur(String kernelFname) throws IOException {
		String fname =  "lena.jpg";
		ImageWrapper src = loadImage(fname);
		ImageWrapper kernel = loadImage(kernelFname);

		ImageWrapper out = ConvoutionUtils.convolveDeconvolve(src, kernel);

		saveImageAsPng(out, "outBlurDeblur");
	}

	private static void test() throws IOException {
		String fname =  "lena.jpg";
		ImageWrapper src = loadImage(fname);

		ImageWrapper out = ConvoutionUtils.testFT(src);

		saveImageAsPng(out, "outTest");
	}

	private static void deblurImage(String kernelFname) throws IOException {
		String fname = "outBlurred.png";
		// fname = "lena.jpg";
		ImageWrapper src = loadImage(fname);
		ImageWrapper kernel = loadImage(kernelFname);

		ImageWrapper out = ConvoutionUtils.deconvolve(src, kernel);

		saveImageAsPng(out, "outDeBlurred");
	}

	private static void blurImage(String kernelFname) throws IOException {
		String fname = "lena.jpg";
		// fname = "lena.jpg";
		ImageWrapper src = loadImage(fname);
		ImageWrapper kernel = loadImage(kernelFname);

		ImageWrapper out = ConvoutionUtils.convolve(src, kernel);

		saveImageAsPng(out, "outBlurred");

	}

	private static ImageWrapper loadImage(String fileName) throws IOException {
		return new ImageWrapper(ImageIO.read(new File(fileName)));
	}

	private static void saveImageAsPng(ImageWrapper image, String fileNameWithoutExtension) throws IOException {
		ImageIO.write(image.getImage(), "png", new File(fileNameWithoutExtension + ".png"));
	}

}
