package dwttest;

abstract class FeatureVectorUtil {

	public static boolean checkSigmaWith(WaveletSignature query, WaveletSignature with) {
		return (query.sigYb < with.sigY && with.sigY < query.sigYdivb) || ((query.sigUb < with.sigU && with.sigU < query.sigUdivb) && (query.sigVb < with.sigV && with.sigV < query.sigVdivb));
	}

	public static double getFirstMatrixDistanceWith(WaveletSignature query, WaveletSignature with) {
		int S1 = WaveletSignature.SIZE >> (WaveletSignature.LEVEL2 - 1);
		return euclidianDist(query.Ys, with.Ys, S1) + euclidianDist(query.Us, with.Us, S1) + euclidianDist(query.Vs, with.Vs, S1);
	}

	public static double getDistanceWith(WaveletSignature query, WaveletSignature with) {
		int S1 = WaveletSignature.SIZE >> (WaveletSignature.LEVEL1 - 1);
		return euclidianDist(query.Y, with.Y, S1) + euclidianDist(query.U, with.U, S1) + euclidianDist(query.V, with.V, S1);

		// int S1 = (WaveletSignature.SIZE >> (WaveletSignature.LEVEL1 - 1)) >>
		// 1;
		// double q1, q2, q3, q4;
		//
		// q1 = euclidianDist(query.Y, with.Y, S1, 0, 0) +
		// euclidianDist(query.Us, with.Us, S1, 0, 0) + euclidianDist(query.Vs,
		// with.Vs, S1, 0, 0);
		// q2 = euclidianDist(query.Y, with.Y, S1, 0, S1) +
		// euclidianDist(query.Us, with.Us, S1, 0, S1) + euclidianDist(query.Vs,
		// with.Vs, S1, 0, S1);
		// q3 = euclidianDist(query.Y, with.Y, S1, S1, 0) +
		// euclidianDist(query.Us, with.Us, S1, S1, 0) + euclidianDist(query.Vs,
		// with.Vs, S1, S1, 0);
		// q4 = euclidianDist(query.Y, with.Y, S1, S1, S1) +
		// euclidianDist(query.Us, with.Us, S1, S1, S1) +
		// euclidianDist(query.Vs, with.Vs, S1, S1, S1);
		//
		// return q1 + q2 + q3 + q4;
	}

	/**
	 * @param to
	 * @return The computer Euclidean Distance between two matrices
	 */
	private static double euclidianDist(int[] m1, int[] m2, int stride) {
		double ret = 0.0;
		int i, SQ = stride * stride;

		for (i = 0; i < SQ; i++) {
			ret += (double) (m1[i] - m2[i]) * (double) (m1[i] - m2[i]);
		}

		return Math.sqrt(ret);
	}

	// /**
	// * @param to
	// * @return The computer Euclidean Distance between two SUB matrices
	// */
	// private static double euclidianDist(int[] m1, int[] m2, int stride, int
	// x, int y) {
	// double ret = 0.0;
	// // int i, j, SQ = stride * stride;
	//
	// for (i = 0; i < SQ; i++) {
	// ret += (double) (m1[i] - m2[i]) * (double) (m1[i] - m2[i]);
	// }
	//
	// int i ,j, sp = y * SIZE + x, ep = (y + hei - 1) * SIZE + x;
	//
	// for (j = sp; j < ep; j+=SIZE) {
	// Arrays.fill(data, j, j + wid, 0);
	// }
	//
	// return Math.sqrt(ret);
	// }
}
