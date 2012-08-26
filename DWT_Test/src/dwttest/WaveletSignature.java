package dwttest;

import java.util.Arrays;
import math.transform.jwave.Transform;
import math.transform.jwave.handlers.DiscreteWaveletTransform;
import math.transform.jwave.handlers.TransformInterface;
import math.transform.jwave.handlers.wavelets.Daub04;
import processing.core.PConstants;
import processing.core.PImage;

public class WaveletSignature {

	WaveletSignatureFactory factory;
	// Actually I'm using YCbCr; to be precise, the same conversion found in
	// JPEG/JFIF
	// that is discarding the 'head-room' and 'leg-room' in traditional YCbCr to
	// use
	// the full 8bit range (0 - 255)
	private double[][] Y, U, V;

	public WaveletSignature(WaveletSignatureFactory factory, PImage src) {
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

			a = factory.t.forward(a);
			b = factory.t.forward(b);
			c = factory.t.forward(c);

			Y = new double[factory.size][factory.size];
			U = new double[factory.size][factory.size];
			V = new double[factory.size][factory.size];

			for (j = factory.size - 1; j >= 0; j--) {
				System.arraycopy(a[j], 0, Y[j], 0, factory.size);
				System.arraycopy(b[j], 0, U[j], 0, factory.size);
				System.arraycopy(c[j], 0, V[j], 0, factory.size);
			}

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

//		t1 = Y;
//		t2 = U;
//		t3 = V;
		t1 = new double[factory.size][factory.size];
		t2 = new double[factory.size][factory.size];
		t3 = new double[factory.size][factory.size];
		t1 = factory.t.reverse(Y);
		t2 = factory.t.reverse(U);
		t3 = factory.t.reverse(V);

//		 scale(t1);
//		 scale(t2);
//		 scale(t3);

		ret.loadPixels();

		for (j = 0; j < factory.size; j++)
			for (i = 0; i < factory.size; i++) {
				y = t1[j][i];
				u = t2[j][i] - 128;
				v = t3[j][i] - 128;
				r = clamp((int) (y + 1.402 * v));
				g = clamp((int) (y - 0.34414 * u - 0.71414 * v));
				b = clamp((int) (y + 1.772 * u));

				ret.pixels[j * factory.size + i] = (r << 16) | (g << 8) | b;
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

	// private void discard(double[][] data, int level, boolean cumulative) {
	// if (level > threshold || level < 1)
	// return;
	//
	// int wid = (int) (this.width / Math.pow(2, level)), hei = (int)
	// (this.height / Math
	// .pow(2, level));
	//
	// if (cumulative) {
	// nullify(data, wid, 0, this.width - wid, this.height);
	// nullify(data, 0, hei, wid, this.height - hei);
	// } else {
	// nullify(data, wid, 0, wid, hei * 2);
	// nullify(data, 0, hei, wid, hei);
	// }
	// }

	private void nullify(double[][] data, int x, int y, int wid, int hei) {
		int j, xx = x + wid;

		for (j = y + hei - 1; j >= y; j--) {
			Arrays.fill(data[j], x, xx, 0.0);
		}
	}

	@SuppressWarnings("unused")
	private void scale(double[][] array) {
		int a = array.length, b = array[0].length, i, j;
		double max, min;
		min = max = array[0][0];

		for (j = a - 1; j >= 0; j--)
			for (i = b - 1; i >= 0; i--) {
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
