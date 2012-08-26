package dwttest;

import java.util.Arrays;
import math.transform.jwave.Transform;
import math.transform.jwave.handlers.DiscreteWaveletTransform;
import math.transform.jwave.handlers.TransformInterface;
import math.transform.jwave.handlers.wavelets.Daub04;
import processing.core.PConstants;
import processing.core.PImage;

public class WaveletSignature {
	private static int DEFAULT_SIZE = 256, DEFAULT_THRESHOLD = 5,
			DEFAULT_DETAIL_LEVEL = 1;
	private PImage sig;

	// Actually I'm using YCbCr; to be precise, the same conversion found in
	// JPEG/JFIF
	// that is discarding the 'head-room' and 'leg-room' in traditional YCbCr to
	// use
	// the full 8bit range (0 - 255)
	private int[][] Y, U, V;

	private int size, threshold;

	Transform t = new Transform(
			(TransformInterface) new DiscreteWaveletTransform(new Daub04(),
					threshold));
	
	// TODO delete this! for testing
	public PImage Image;

	public WaveletSignature(PImage src, int size, int threshold) {
		PImage tmp;
		try {
			tmp = (PImage) src.clone();

			this.size = size;
			this.threshold = threshold;

			if (tmp.width != size || tmp.height != size)
				tmp.resize(size, size);

			Y = new int[size][size];
			U = new int[size][size];
			V = new int[size][size];
			getData(tmp, Y, U, V);
			
			Image = getImage(Y, U, V);

		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	private void getData(PImage img, int[][] Y, int[][] U, int[][] V) {
		int i, j, r, g, b;

		// I'm expecting the arrays to be initialised and of the right size; or
		// else...
		// Y = new int[size][size];
		// U = new int[size][size];
		// V = new int[size][size];

		img.loadPixels();

		for (j = 0; j < size; j++)
			for (i = 0; i < size; i++) {
				b = img.pixels[j * size + i]; // 32bits
				r = (b & 0x00FF0000) >> 16;
				g = (b & 0x0000FF00) >> 8;
				b = b & 0x000000FF;

				Y[j][i] = clamp((int) (0.299 * r + 0.587 * g + 0.114 * b));
				U[j][i] = clamp((int) (128 - 0.168736 * r - 0.331264 * g + 0.5 * b));
				V[j][i] = clamp((int) (128 + 0.5 * r - 0.418688 * g - 0.081312 * b));
			}

	}

	private PImage getImage(int[][] Y, int[][] U, int[][] V) {
		int i, j, r, g, b, y, u, v;
		PImage ret = new PImage(size, size, PImage.RGB);
		// createImage(size, size, PConstants.RGB);

		ret.loadPixels();

		for (j = 0; j < size; j++)
			for (i = 0; i < size; i++) {
				y = Y[j][i];
				u = U[j][i] - 128;
				v = V[j][i] - 128;
				r = clamp((int) (y + 1.402 * v));
				g = clamp((int) (y - 0.34414 * u - 0.71414 * v));
				b = clamp((int) (y + 1.772 * u));

				ret.pixels[j * size + i] = (r << 16) | (g << 8) | b;
			}
		
		ret.updatePixels();
		return ret;
	}

	private int map(double d, double min, double max, int i, int j) {
		return (int) (i + (d - min) * (j - i) / (max - min));
	}

//	private void discard(double[][] data, int level, boolean cumulative) {
//		if (level > threshold || level < 1)
//			return;
//
//		int wid = (int) (this.width / Math.pow(2, level)), hei = (int) (this.height / Math
//				.pow(2, level));
//
//		if (cumulative) {
//			nullify(data, wid, 0, this.width - wid, this.height);
//			nullify(data, 0, hei, wid, this.height - hei);
//		} else {
//			nullify(data, wid, 0, wid, hei * 2);
//			nullify(data, 0, hei, wid, hei);
//		}
//	}

	private void nullify(double[][] data, int x, int y, int wid, int hei) {
		int j, xx = x + wid;

		for (j = y + hei - 1; j >= y; j--) {
			Arrays.fill(data[j], x, xx, 0.0);
		}
	}

	@SuppressWarnings("unused")
	private void round(double[][] array) {
		int a = array.length, b = array[0].length, i, j;

		for (j = 0; j < a; j++)
			for (i = 0; i < b; i++) {
				array[i][j] = (int) Math.round(array[i][j]);
			}
	}

	private int clamp(int c) {
		c=Math.abs(c);
		return (c > 255) ? 255 : (c < 0) ? 0 : c;
	}

//	private void getMinMax(double[][] array, int x, int y, int wid, int hei) {
//		int i, j, xx = x + wid;
//		double max, min;
//		min = max = array[x][y];
//
//		for (j = y + hei - 1; j >= y; j--)
//			for (i = x + wid - 1; i >= x; i--) {
//				min = (array[i][j] < min) ? array[i][j] : min;
//				max = (array[i][j] > max) ? array[i][j] : max;
//			}
//
//		println("(min, max) = ( " + min + ", " + max + " )");
//
//	}

	// private double[] getSD(double[][] array) {
	// int i, j, xx = x + wid;
	// double max, min, mean;
	// min = max = array[x][y];
	//
	// for (j = y + hei - 1; j >= y; j--)
	// for (i = x + wid - 1; i >= x; i--) {
	// min = (array[i][j] < min) ? array[i][j] : min;
	// max = (array[i][j] > max) ? array[i][j] : max;
	// }
	//
	// println("(min, max) = ( " + min + ", " + max + " )");
	//
	// }

	private double[][] dup(double[][] array) {
		int a = array.length, b = array[0].length, i;
		double[][] ret = new double[a][b];
		for (i = 0; i < a; i++) {
			System.arraycopy(array[i], 0, ret[i], 0, b);
		}
		return ret;
	}
}
