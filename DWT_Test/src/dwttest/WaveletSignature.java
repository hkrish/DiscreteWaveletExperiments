package dwttest;

import java.io.Serializable;
import processing.core.PImage;

public class WaveletSignature implements Serializable {
	private static final long	serialVersionUID	= 6491384560082421303L;

	static final int			SIZE				= 256;
	static final int			LEVEL1				= 4;
	static final int			LEVEL2				= 5;

	int[]						Y;
	int[]						U;
	int[]						V;
	int[]						Ys;
	int[]						Us;
	int[]						Vs;
	double						sigY				= 0.0;
	double						sigU				= 0.0;
	double						sigV				= 0.0;

	public transient double		sigYb				= 0.0;
	public transient double		sigUb				= 0.0;
	public transient double		sigVb				= 0.0;
	public transient double		sigYdivb			= 0.0;
	public transient double		sigUdivb			= 0.0;
	public transient double		sigVdivb			= 0.0;
	public transient double		rank				= -1;

	public WaveletSignature() {
		// Do nothing
	}

	public WaveletSignature(PImage img) {
		this.Init(img);
	}

	public WaveletSignature(PImage img, int percent) {
		this.Init(img);
		this.InitSignature(percent);
	}

	public void Init(PImage img) {
		if (img.width != img.height || img.width != SIZE)
			img.resize(SIZE, SIZE);

		makeFeatureVector(img);
	}

	public void InitSignature(int percent) {
		double beta = 1.0 - percent / 100.0;
		sigYb = sigY * beta;
		sigUb = sigU * beta;
		sigVb = sigV * beta;
		sigYdivb = sigY / beta;
		sigUdivb = sigU / beta;
		sigVdivb = sigV / beta;
	}

	/**
	 * @param img
	 * 
	 *            Calculate the FeatureVector FeatureVector is a collection of
	 *            following 3 individual feature vectors
	 * 
	 *            <ol>
	 *            <li>1. [16 x 16] of Level4 FWT</li>
	 *            <li>2. [ 8 x 8] of Level5 FWT</li>
	 *            <li>3. SD of [1:8 x 1:8] (top-left quadrant) of <b>1.</b></li>
	 *            </ol>
	 */
	private void makeFeatureVector(PImage img) {
		int i, j, r, g, b, SQ = SIZE * SIZE;
		int S1 = SIZE >> (LEVEL1 - 1), S1Q = S1 * S1, ep1 = (S1 - 1) * SIZE;
		int S2 = SIZE >> (LEVEL2 - 1), S2Q = S2 * S2, ep2 = (S2 - 1) * SIZE;
		int[] A = new int[SQ];
		int[] B = new int[SQ];
		int[] C = new int[SQ];
		int[] ta, tb, tc;

		img.loadPixels();

		for (i = 0; i < SQ; i++) {
			// val = (int) (0.299 * red(img.pixels[j * SIZE + i]) + 0.587 *
			// green(img.pixels[j * SIZE + i]) + 0.114 * blue(img.pixels[j *
			// SIZE + i]));

			b = img.pixels[i]; // 32bits
			r = (b & 0x00FF0000) >> 16;
			g = (b & 0x0000FF00) >> 8;
			b = b & 0x000000FF;

			A[i] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
			B[i] = (int) (128 - 0.168736 * r - 0.331264 * g + 0.5 * b);
			C[i] = (int) (128 + 0.5 * r - 0.418688 * g - 0.081312 * b);
		}

		DWT_CDF_9_7 t = new DWT_CDF_9_7(SIZE, SIZE, LEVEL1);

		ta = t.forward(A, 0);
		tb = t.forward(B, 0);
		tc = t.forward(C, 0);

		Y = new int[S1Q];
		U = new int[S1Q];
		V = new int[S1Q];

		for (i = j = 0; i < ep1; i += SIZE, j += S1) {
			System.arraycopy(ta, i, Y, j, S1);
			System.arraycopy(tb, i, U, j, S1);
			System.arraycopy(tc, i, V, j, S1);
		}

		t = new DWT_CDF_9_7(SIZE, SIZE, LEVEL2);

		ta = t.forward(A, 0);
		tb = t.forward(B, 0);
		tc = t.forward(C, 0);

		Ys = new int[S2Q];
		Us = new int[S2Q];
		Vs = new int[S2Q];

		for (i = j = 0; i < ep2; i += SIZE, j += S2) {
			System.arraycopy(ta, i, Ys, j, S2);
			System.arraycopy(tb, i, Us, j, S2);
			System.arraycopy(tc, i, Vs, j, S2);
		}

		// TODO Calculate the Standard Deviation
		sigY = featureSD(Y, S1);
		sigU = featureSD(U, S1);
		sigV = featureSD(V, S1);

		// Feature vector is complete
	}

	private double featureSD(int[] y2, int stride) {
		int i, j, w = stride >> 2, ep = w * stride;
		double mean = 0.0, ret = 0.0;

		for (i = 0; i < ep; i += stride)
			for (j = 0; j < w; j++) {
				mean += y2[i + j];
			}
		
		/**
		 * Is this a <a href=
		 * "http://en.wikipedia.org/wiki/Standard_deviation#Definition_of_population_values"
		 * >Population Standard Deviation</a>? I think so!
		 */
		mean /= w * w;

		// FIXME Debug this code for calculating Standard Deviation, make sure
		// it is the right code
		for (i = 0; i < ep; i += stride)
			for (j = 0; j < w; j++) {
				ret += Math.pow((y2[i + j] - mean), 2);
			}

		return Math.sqrt(ret / (w * w));
	}
	
}
