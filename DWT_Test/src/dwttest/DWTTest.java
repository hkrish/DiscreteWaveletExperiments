package dwttest;

import java.util.Arrays;
import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings("serial")
public class DWTTest extends PApplet {

	static final int	SIZE		= 512;
	int					threshold	= 5;
	DWT_CDF_9_7			t			= new DWT_CDF_9_7(SIZE, SIZE, threshold);
	int[]				matHilb;
	boolean[]			keys		= new boolean[526];

	public void setup() {
		PImage img = loadImage("data/duc_006_2005_00_1024x768_ducati-monster-620.jpg");
		// PImage img = loadImage("data/2007_MV_Brutale_910_1r.jpg");

		size(512, 512, JAVA2D);
		background(0);

		img.resize(SIZE, SIZE);
		// img = getImage(getData(img));
		matHilb = getData(img);

		matHilb = t.forward(matHilb, 0); // 2-D FWT Haar forward

		image(getImage(matHilb), 0, 0);
	}

	public void keyPressed() {
		int[] data = new int[matHilb.length];
		System.arraycopy(matHilb, 0, data, 0, matHilb.length);
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
			data = t.inverse(data, 0); // 2-D FWT Haar reverse
		image(getImage(data), 0, 0);
	}

	public void keyReleased() {
		keys[keyCode] = false;
	}

	public void draw() {
	}

	int[] getData(PImage img) {
		int i, j, val;
		int[] ret = new int[SIZE * SIZE];

		img.loadPixels();

		for (j = 0; j < SIZE; j++)
			for (i = 0; i < SIZE; i++) {
				// Extract luminance (the Y of YUV) from RBG
				val = (int) (0.299 * red(img.pixels[j * SIZE + i]) + 0.587 * green(img.pixels[j * SIZE + i]) + 0.114 * blue(img.pixels[j * SIZE + i]));

				ret[j * SIZE + i] = val;
			}

		return ret;
	}

	PImage getImage(int[] data) {
		int i, j;
		PImage ret = createImage(SIZE, SIZE, RGB);

		ret.loadPixels();

		for (j = 0; j < SIZE; j++)
			for (i = 0; i < SIZE; i++) {
				ret.pixels[j * SIZE + i] = color(constrain(Math.abs(data[j * SIZE + i]), 0, 255));
			}

		ret.updatePixels();
		return ret;
	}

	private void discard(int[] data, int level, boolean cumulative) {
		if (level > threshold || level < 1)
			return;

		int wid = (int) (SIZE / Math.pow(2, level)); // DEBUG (SIZE >> level) ??

		if (cumulative) {
			nullify(data, wid, 0, SIZE - wid, SIZE);
			nullify(data, 0, wid, wid, SIZE - wid);
		} else {
			nullify(data, wid, 0, wid, wid * 2);
			nullify(data, 0, wid, wid, wid);
		}
	}

	private void nullify(int[] data, int x, int y, int wid, int hei) {
		int j, sp = y * SIZE + x, ep = (y + hei - 1) * SIZE + x;

		for (j = sp; j < ep; j+=SIZE) {
			Arrays.fill(data, j, j + wid, 0);
		}
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
