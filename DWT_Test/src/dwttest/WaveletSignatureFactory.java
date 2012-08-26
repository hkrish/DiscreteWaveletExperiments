package dwttest;

import processing.core.PImage;
import math.transform.jwave.Transform;
import math.transform.jwave.handlers.DiscreteWaveletTransform;
import math.transform.jwave.handlers.TransformInterface;
import math.transform.jwave.handlers.wavelets.Daub04;

public class WaveletSignatureFactory {
	private static int DEFAULT_THRESHOLD = 3, DEFAULT_DETAIL_LEVEL = 1,
			DEFAULT_SAMPLE_SIZE = 256;

	int size, sampleSize, threshold, level;

	Transform t;

	public WaveletSignatureFactory() {
		this(DEFAULT_SAMPLE_SIZE, DEFAULT_THRESHOLD, DEFAULT_DETAIL_LEVEL);
	}

	public WaveletSignatureFactory(int sampleSize, int threshold, int level) {
		this.threshold = threshold;
		this.sampleSize = sampleSize;
//		this.size = sampleSize;
		this.size = (int) (sampleSize / Math.pow(2, level));
		this.level = level;

		t = new Transform((TransformInterface) new DiscreteWaveletTransform(
				new Daub04(), threshold));
		
		System.out.println(size);
	}

	public WaveletSignature getWaveletSignature(PImage img) {
		return new WaveletSignature(this, img);
	}
}
