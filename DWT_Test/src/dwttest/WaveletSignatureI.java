package dwttest;

import java.util.Arrays;
import processing.core.PImage;
import org.apache.commons.math3.*;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class WaveletSignatureI {

	WaveletSignatureFactory factory;
	// Actually I'm using YCbCr; to be precise, the same conversion found in
	// JPEG/JFIF
	// that is discarding the 'head-room' and 'leg-room' in traditional YCbCr to
	// use
	// the full 8bit range (0 - 255)
	private double[][] Y, U, V;
	public double[][] Ys, Us, Vs;
	public double sigY, sigU, sigV;

	public WaveletSignatureI(WaveletSignatureFactory factory, PImage src) {
		PImage tmp;
		double[][] a, b, c;
		int j;

		this.factory = factory;

		try {
			tmp = (PImage) src.clone();

			if (tmp.width != factory.sampleSize
					|| tmp.height != factory.sampleSize)
				tmp.resize(factory.sampleSize, factory.sampleSize);

			a = new double[tmp.width][tmp.width];
			b = new double[tmp.width][tmp.width];
			c = new double[tmp.width][tmp.width];

			getData(tmp, a, b, c);

			a = factory.tf.forward(a);
			b = factory.tf.forward(b);
			c = factory.tf.forward(c);

			// discard higher order coeff. and invert that and then sample the
			// data to make Y,U,V arrays of factory.size

//			discard(a, 3, true);
//			discard(b, 3, true);
//			discard(c, 3, true);

//			a = factory.tf.reverse(a);
//			b = factory.tf.reverse(b);
//			c = factory.tf.reverse(c);

			Y = new double[factory.size][factory.size];
			U = new double[factory.size][factory.size];
			V = new double[factory.size][factory.size];
			
			for (j = factory.size - 1; j >= 0; j--) {
				System.arraycopy(a[j], 0, Y[j], 0, factory.size);
				System.arraycopy(b[j], 0, U[j], 0, factory.size);
				System.arraycopy(c[j], 0, V[j], 0, factory.size);
			}
			
			// Now do the SD calculations
			double[] ysd = arrayUnfold(Y);
			StandardDeviation SD = new StandardDeviation();
			System.out.println(SD.evaluate(ysd));
			ysd = arrayUnfold(U);
			System.out.println(SD.evaluate(ysd));
			ysd = arrayUnfold(V);
			System.out.println(SD.evaluate(ysd));

		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	private void getData(PImage img, double[][] Y, double[][] U, double[][] V) {
		int i, j, r, g, b;
		double y, u, v;

		// I'm expecting the arrays to be initialised and of the right size; or
		// else...
		// Y = new int[size][size];
		// U = new int[size][size];
		// V = new int[size][size];

		img.loadPixels();

		for (j = 0; j < factory.sampleSize; j++)
			for (i = 0; i < factory.sampleSize; i++) {
				b = img.pixels[j * factory.sampleSize + i]; // 32bits
				r = (b & 0x00FF0000) >> 16;
				g = (b & 0x0000FF00) >> 8;
				b = b & 0x000000FF;

				y = r;
				u = g;
				v = b;

				Y[j][i] = 0.299 * y + 0.587 * u + 0.114 * v;
				U[j][i] = 128 - 0.168736 * y - 0.331264 * u + 0.5 * v;
				V[j][i] = 128 + 0.5 * y - 0.418688 * u - 0.081312 * v;
			}

	}

	public PImage getImage() {
		int i, j, r, g, b;
		double y, u, v;
		double[][] t1, t2, t3;
		PImage ret = new PImage(factory.size, factory.size, PImage.RGB);
		// createImage(size, size, PConstants.RGB);

		// t1 = Y;
		// t2 = U;
		// t3 = V;
//		t1 = new double[factory.sampleSize][factory.sampleSize];
//		t2 = new double[factory.sampleSize][factory.sampleSize];
//		t3 = new double[factory.sampleSize][factory.sampleSize];
//
//		for (j = factory.size - 1; j >= 0; j--) {
//			System.arraycopy(Y[j], 0, t1[j], 0, factory.size);
//			System.arraycopy(U[j], 0, t2[j], 0, factory.size);
//			System.arraycopy(V[j], 0, t3[j], 0, factory.size);
//		}

//		t1 = factory.tf.reverse(t1);
//		t2 = factory.tf.reverse(t2);
//		t3 = factory.tf.reverse(t3);

		ret.loadPixels();

		for (j = 0; j < factory.size; j++)
			for (i = 0; i < factory.size; i++) {
				y = Y[j][i];
				u = U[j][i] - 128;
				v = V[j][i] - 128;
				r = clamp((int) (y + 1.402 * v));
				g = clamp((int) (y - 0.34414 * u - 0.71414 * v));
				b = clamp((int) (y + 1.772 * u));

				ret.pixels[j * factory.size + i] = (r << 16) | (r << 8) | r;
//				ret.pixels[j * factory.size + i] = (r << 16) | (g << 8) | b;
			}

		ret.updatePixels();
		return ret;
	}

	// private int map(double d, double min, double max, int i, int j) {
	// return (int) (i + (d - min) * (j - i) / (max - min));
	// }

	private double map(double d, double min, double max, double i, double j) {
		return i + (d - min) * (j - i) / (max - min);
	}

	private void discard(double[][] data, int level, boolean cumulative) {
		if (level > factory.threshold || level < 1)
			return;

		int wid = (int) (data.length / Math.pow(2, level));

		if (cumulative) {
			nullify(data, wid, 0, data.length - wid, data.length);
			nullify(data, 0, wid, wid, data.length - wid);
		} else {
			nullify(data, wid, 0, wid, wid * 2);
			nullify(data, 0, wid, wid, wid);
		}
	}

	private void nullify(double[][] data, int x, int y, int wid, int hei) {
		int j, xx = x + wid;

		for (j = y + hei - 1; j >= y; j--) {
			Arrays.fill(data[j], x, xx, 0.0);
		}
	}

	private double[] arrayUnfold(double[][] array) {
		int stride = array.length, i, pos;
		double[] ret = new double[stride * stride];
		for (i = 0, pos = 0; i < stride; i++, pos += stride)
			System.arraycopy(array[i], 0, ret, pos, stride);
		return ret;
	}

	private double[][] arrayFold(double[] array) {
		int stride = (int) Math.sqrt(array.length), i, pos;
		double[][] ret = new double[stride][stride];
		for (i = 0, pos = 0; i < stride; i++, pos += stride)
			System.arraycopy(array, pos, ret[i], 0, stride);
		return ret;
	}

	@SuppressWarnings("unused")
	private void scale(double[][] array) {
		int a = array.length, b = array[0].length, i, j;
		double max, min;
		min = max = Math.abs(array[0][0]);

		for (j = a - 1; j >= 0; j--)
			for (i = b - 1; i >= 0; i--) {
				array[i][j] = Math.abs(array[i][j]);
				min = (array[i][j] < min) ? array[i][j] : min;
				max = (array[i][j] > max) ? array[i][j] : max;
			}

		for (j = 0; j < a; j++)
			for (i = 0; i < b; i++) {
				array[i][j] = map(array[i][j], min, max, 0.0, 1.0);
			}
	}

	private int clamp(int c) {
		return (c > 255) ? 255 : (c < 0) ? 0 : c;
	}

	// private void getMinMax(double[][] array, int x, int y, int wid, int hei)
	// {
	// int i, j, xx = x + wid;
	// double max, min;
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