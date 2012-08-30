package dwttest;

import processing.core.PImage;
import math.transform.jwave.Transform;
import math.transform.jwave.handlers.DiscreteWaveletTransform;
import math.transform.jwave.handlers.TransformInterface;
import math.transform.jwave.handlers.wavelets.Daub04;

public class WaveletSignatureFactory {
	private static int	DEFAULT_THRESHOLD	= 4, DEFAULT_DETAIL_LEVEL = 3, DEFAULT_SAMPLE_SIZE = 256;
	int					size, sampleSize, threshold, level;
	Transform			tf;

	public WaveletSignatureFactory() {
		this(DEFAULT_SAMPLE_SIZE, DEFAULT_THRESHOLD, DEFAULT_DETAIL_LEVEL);
	}

	public WaveletSignatureFactory(int sampleSize, int threshold, int level) {
		this.threshold = threshold;
		this.sampleSize = sampleSize;
		// this.size = sampleSize;
		this.size = (int) (sampleSize / Math.pow(2, level));
		System.out.println(size + " , " + sampleSize);
		this.level = level;

		tf = new Transform((TransformInterface) new DiscreteWaveletTransform(new Daub04(), threshold));

	}

	public WaveletSignatureI getWaveletSignature(PImage img) {
		return new WaveletSignatureI(this, img);
	}
	
	public double euclidianDist(WaveletSignatureI a, WaveletSignatureI b){
		return euclidianDist(a.Ys, b.Ys) + euclidianDist(a.Us, b.Us) + euclidianDist(a.Vs, b.Vs);
	}

	//
	private double euclidianDist(int[][] m1, int[][] m2) {
		if (m1.length != m1[0].length || m2.length != m2[0].length || m2.length != m1.length) {
			System.err.println("euclidianDist() - input should be 2 square matrices of equal size");
			return 0;
		}

		return euclidianDist(m1, 0, 0, m2, 0, 0, m1.length);
	}

	private double euclidianDist(int[][] m1, int x1, int y1, int[][] m2, int x2, int y2, int size) {
		double ret = 0.0;
		int i, j;

		for (i = 0; i < size; i++) {
			for (j = 0; j < size; j++) {
				ret += (m1[i][j] - m2[i][j]) * (m1[i][j] - m2[i][j]);
			}

		}

		return Math.sqrt(ret);
	}
}
