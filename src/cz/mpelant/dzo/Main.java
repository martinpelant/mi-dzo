
package cz.mpelant.dzo;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {
	private static String IMAGE="metro.jpg";
	private static String KERNEL="circle.png";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		blurDeblur();
		blurImage();
		deblurImage();
		amplitude(KERNEL);
		amplitude(IMAGE);
		amplitude("outBlurred.png");
		amplitude("outDeblurred.png");
		amplitude("outBlurDeblur.png");

	}



	private static void amplitude(String fname) throws IOException {
		ImageWrapper src = loadImage(fname);

		ImageWrapper out = ConvoutionUtils.getAmplitude(src);

		saveImageAsPng(out, "outAmplitude_"+fname);
	}


	private static void blurDeblur() throws IOException {
		ImageWrapper src = loadImage(IMAGE);
		ImageWrapper kernel = loadImage(KERNEL);

		ImageWrapper out = ConvoutionUtils.convolveDeconvolve(src, kernel);

		saveImageAsPng(out, "outBlurDeblur");
	}

	private static void deblurImage() throws IOException {
		String fname = "outBlurred.png";
		// fname = "lena.jpg";
		ImageWrapper src = loadImage(fname);
		ImageWrapper kernel = loadImage(KERNEL);

		ImageWrapper out = ConvoutionUtils.deconvolve(src, kernel);

		saveImageAsPng(out, "outDeBlurred");
	}

	private static void blurImage() throws IOException {
		ImageWrapper src = loadImage(IMAGE);
		ImageWrapper kernel = loadImage(KERNEL);

		ImageWrapper out = ConvoutionUtils.convolve(src, kernel);

		saveImageAsPng(out, "outBlurred");

	}

	private static ImageWrapper loadImage(String fileName) throws IOException {
		return new ImageWrapper(ImageIO.read(new File(fileName)));
	}

	static void saveImageAsPng(ImageWrapper image, String fileNameWithoutExtension) throws IOException {
		ImageIO.write(image.getImage(), "png", new File(fileNameWithoutExtension + ".png"));
	}

}
