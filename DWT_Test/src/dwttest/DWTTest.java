package dwttest;

import java.util.Arrays;

import math.transform.jwave.Transform;
import math.transform.jwave.handlers.DiscreteWaveletTransform;
import math.transform.jwave.handlers.TransformInterface;
import math.transform.jwave.handlers.wavelets.Daub04;
import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings("serial")
public class DWTTest extends PApplet {

	int threshold = 5, depth = 1;
	Transform t = new Transform(
			(TransformInterface) new DiscreteWaveletTransform(new Daub04(),
					threshold));
	double[][] matHilb;
	boolean[] keys = new boolean[526];

	public void setup() {
		PImage img = loadImage("data/duc_006_2005_00_1024x768_ducati-monster-620.jpg");
		// PImage img = loadImage("data/2007_MV_Brutale_910_1r.jpg");

		size(512, 512, JAVA2D);
		background(0);

		img.resize(this.width, this.height);
		// img = getImage(getData(img));
		matHilb = getData(img);

		for (int i = 0; i < depth; i++)
			matHilb = t.forward(matHilb); // 2-D FWT Haar forward
		
		getMinMax(matHilb, 0,0,512,512);

		image(getImage(matHilb), 0, 0);
	}

	public void keyPressed() {
		double[][] data = dup(matHilb);
		boolean inverse = true, cumulative = true;

		keys[keyCode] = true;
		cumulative = !checkKey(CONTROL);

		switch (key) {
		case 'z':
			inverse = false;
			break;

		default:
			if (key > '0' && key <= '9')
				discard(data, key - '0', cumulative);
		}

		if (inverse)
			for (int i = 0; i < depth; i++)
				data = t.reverse(data); // 2-D FWT Haar reverse
		image(getImage(data), 0, 0);
	}

	public void keyReleased() {
		keys[keyCode] = false;
	}

	public void draw() {
	}

	double[][] getData(PImage img) {
		int wid = img.width, hei = img.height, i, j;
		double[][] ret = new double[hei][wid];
		double val;

		img.loadPixels();

		for (j = 0; j < hei; j++)
			for (i = 0; i < wid; i++) {
				// Extract luminance (the Y of YUV) from RBG
				val = 0.299 * red(img.pixels[j * wid + i]) + 0.587
						* green(img.pixels[j * wid + i]) + 0.114
						* blue(img.pixels[j * wid + i]);
				val /= 255.0;

				ret[j][i] = val;
			}

		return ret;
	}

	PImage getImage(double[][] data) {
		int hei = data.length, wid = data[0].length, i, j;
		PImage ret = createImage(wid, hei, RGB);

		ret.loadPixels();

		for (j = 0; j < hei; j++)
			for (i = 0; i < wid; i++) {
				ret.pixels[j * wid + i] = color(constrain(
						(int) (255 * Math.abs(data[j][i])), 0, 255));
			}

		ret.updatePixels();
		return ret;
	}

	PImage getImage(double[][] data, boolean scale) {
		int hei = data.length, wid = data[0].length, i, j, val;
		PImage ret = createImage(wid, hei, RGB);
		double max = 1, min = 0;

		ret.loadPixels();

		if (scale) {
			min = max = data[0][0];
			for (j = 0; j < hei; j++)
				for (i = 0; i < wid; i++) {
					min = (data[i][j] < min) ? data[i][j] : min;
					max = (data[i][j] > max) ? data[i][j] : max;
				}
		}

		for (j = 0; j < hei; j++)
			for (i = 0; i < wid; i++) {
				val = map(data[j][i], min, max, 0, 255);
				ret.pixels[j * wid + i] = color(val);
			}

		ret.updatePixels();
		return ret;
	}

	private int map(double d, double min, double max, int i, int j) {
		return (int) (i + (d - min) * (j - i) / (max - min));
	}

	private void discard(double[][] data, int level, boolean cumulative) {
		if (level > threshold || level < 1)
			return;

		int wid = (int) (this.width / Math.pow(2, level)), hei = (int) (this.height / Math
				.pow(2, level));

		if (cumulative) {
			nullify(data, wid, 0, this.width - wid, this.height);
			nullify(data, 0, hei, wid, this.height - hei);
		} else {
			nullify(data, wid, 0, wid, hei * 2);
			nullify(data, 0, hei, wid, hei);
		}
	}

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

	private void getMinMax(double[][] array, int x, int y, int wid, int hei) {
		int i, j, xx = x + wid;
		double max, min;
		min = max = array[x][y];
		
		for (j = y + hei - 1; j >= y; j--)
			for (i = x + wid - 1; i >= x; i--) {
				min = (array[i][j] < min) ? array[i][j] : min;
				max = (array[i][j] > max) ? array[i][j] : max;
			}
		
		println("(min, max) = ( " + min + ", " + max + " )");

	}

//	private double[] getSD(double[][] array) {
//		int i, j, xx = x + wid;
//		double max, min, mean;
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

	private double[][] dup(double[][] array) {
		int a = array.length, b = array[0].length, i;
		double[][] ret = new double[a][b];
		for (i = 0; i < a; i++) {
			System.arraycopy(array[i], 0, ret[i], 0, b);
		}
		return ret;
	}

	boolean checkKey(int k) {
		if (keys.length >= k) {
			return keys[k];
		}
		return false;
	}

	public static void main(String _args[]) {
		PApplet.main(new String[] { dwttest.DWTTest.class.getName() });
	}
}
