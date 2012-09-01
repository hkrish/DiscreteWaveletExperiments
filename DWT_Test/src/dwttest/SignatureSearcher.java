package dwttest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import sun.net.www.content.audio.wav;

public class SignatureSearcher extends Thread {
	boolean								running, runnable = false, canUpdateQuery = true;
	String								id;

	private WaveletSignature			Query;
	private ArrayList<String>			knownSignatures;
	private ArrayList<WaveletSignature>	possibleMatches;

	private long						ST;

	public SignatureSearcher(String id, WaveletSignature query, List<String> knownSigs) {
		if (query != null) {
			this.id = id;
			this.Query = query;
			knownSignatures = new ArrayList<String>(knownSigs);
			possibleMatches = new ArrayList<WaveletSignature>();
			runnable = true;
		}
	}

	@Override
	public void start() {
		if (!runnable) {
			System.err.println("Cannot Search. Stopping.");
			return;
		}

		running = true;
		System.out.println("Searching... ");

		ST = System.nanoTime();
		super.start();
	}

	@Override
	public void run() {
		ListIterator<String> itr = knownSignatures.listIterator();
		ListIterator<WaveletSignature> wavItr = null;
		int checkLevel = 1;
		RasterImageSignature item;

		double max, min, val;
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;

		while (running && checkLevel <= 3) {
			if (checkLevel == 1 && !itr.hasNext()) {
				checkLevel++;
				wavItr = possibleMatches.listIterator();
			} else {
				try {
					// Do a 3 level checking on crawled images
					if (checkLevel == 1 && itr.hasNext()) {
						FileInputStream fis = new FileInputStream(itr.next());
						ObjectInputStream ois = new ObjectInputStream(fis);

						item = (RasterImageSignature) ois.readObject();
						if (!FeatureVectorUtil.checkSigmaWith(Query, item))
							itr.remove();
						else
							possibleMatches.add(item);

						ois.close();

					} else if (checkLevel == 2) {
						if (!wavItr.hasNext()) {
							checkLevel++;
							wavItr = possibleMatches.listIterator();
							min = Double.MAX_VALUE;
							max = Double.MIN_VALUE;
							System.out.println("objects - " + possibleMatches.size());
						} else {
							item = (RasterImageSignature) wavItr.next();
							val = FeatureVectorUtil.getFirstMatrixDistanceWith(Query, item);

							if (val > 25000)
								wavItr.remove();

							if (val < min)
								min = val;
							else if (val > max)
								max = val;
						}
					} else if (checkLevel == 3) {
						if (!wavItr.hasNext()) {
							checkLevel++;
							System.out.println("objects - " + possibleMatches.size());
						} else {
							item = (RasterImageSignature) wavItr.next();
							val = FeatureVectorUtil.getDistanceWith(Query, item);

							if (val > 20000)
								wavItr.remove();

							if (val < min)
								min = val;
							else if (val > max)
								max = val;
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}

		long et = System.nanoTime();
		float milli = (et - ST) / 1000000;
		System.out.println("Elapsed - " + milli + " ms");
		System.out.println("( " + min + " , " + max + " )");

		System.out.println(possibleMatches.get(0).getFileName());

		quit();
	}

	public int getMatchesCount() {
		return possibleMatches.size();
	}

	public void quit() {
		System.out.println("Stopping.");
		running = false;
		interrupt();
	}

}
