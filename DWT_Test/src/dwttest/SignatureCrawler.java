package dwttest;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.PriorityQueue;

import processing.core.PApplet;

public class SignatureCrawler extends Thread {

	public final String				signatureDirectory;
	public final String				rootDirectory;

	boolean							running, runnable = false;
	String							id;
	private int						count, directoryCount;

	private String					currentDir;
	private PriorityQueue<String>	directoriesToCrawl, filesCrawled;

	SignatureFileFilter				fileFilter	= new SignatureFileFilter();

	public final PApplet			parent;
	WaveletSignature				sig;

	SignatureCrawler(String signatureDestination, String directoryRoot, PApplet p) {
		File sdir = new File(signatureDestination), dir = new File(directoryRoot);

		if (!(runnable = sdir.exists() && sdir.isDirectory()))
			runnable = sdir.mkdirs();

		if (dir.exists() && runnable) {
			this.parent = p;
			this.signatureDirectory = sdir.getAbsolutePath();
			this.rootDirectory = dir.getAbsolutePath();

			running = false;
			id = this.rootDirectory;
			directoryCount = count = 0;

			this.currentDir = this.rootDirectory;
			directoriesToCrawl = new PriorityQueue<String>();
			directoriesToCrawl.add(currentDir);

			filesCrawled = new PriorityQueue<String>();
			runnable = true;
		} else {
			this.signatureDirectory = null;
			this.rootDirectory = null;
			this.parent = null;
			runnable = false;
		}
	}

	public void RegisterSignatureTypes(String[] s) {
		this.fileFilter.addFileTypes(s);
	}

	public int getCount() {
		return count;
	}

	public int getDirectoryCount() {
		return directoryCount;
	}

	@Override
	public void start() {
		if (!runnable) {
			System.err.println("Cannot crawl. Stopping.");
			return;
		}

		running = true;
		System.out.println("Crawling " + rootDirectory + "\n Signatures will be saved to " + signatureDirectory);

		super.start();
	}

	@Override
	public void run() {
		while (running && (!directoriesToCrawl.isEmpty() || !filesCrawled.isEmpty())) {
			if (filesCrawled.isEmpty()) {
				// Go to one of the directories to be crawled
				File dir = new File(directoriesToCrawl.poll());
				try {
					validateDirectory(dir);

					++directoryCount;
					File[] files = dir.listFiles(fileFilter);
					for (File f : files) {
						if (f.isDirectory())
							directoriesToCrawl.add(f.getAbsolutePath());
						else
							filesCrawled.add(f.getAbsolutePath());
					}

				} catch (FileNotFoundException e) {
				}

			} else {
				File file = new File(filesCrawled.poll());

				if (RasterImageSignature.accept(file) && shouldCrowl(file)) {

					// Save the signature
					File sigfile = new File(this.signatureDirectory + File.separator + file.hashCode() + ".sig");

					FileOutputStream fos;
					try {
						fos = new FileOutputStream(sigfile);
						ObjectOutputStream oos = new ObjectOutputStream(fos);

						sig = new RasterImageSignature(file.getAbsolutePath(), this.parent);
						sig.setLastModified(file.lastModified());

						oos.writeObject(sig);
						sig = null;

						oos.close();
						// System.out.println("Saved  " + sigfile);
						++count;
					} catch (FileNotFoundException e1) {
						System.err.println("Error saving signature to  " + sigfile);
						e1.printStackTrace();
					} catch (IOException e1) {
						System.err.println("Error saving signature to  " + sigfile);
						e1.printStackTrace();
					}
				}
			}
		}

		if (directoriesToCrawl.isEmpty()) {
			System.out.println("Nothing to crawl.");
			quit();
		}
	}

	public void quit() {
		System.out.println("Stopping.");
		running = false;
		interrupt();
	}

	static private void validateDirectory(File aDirectory) throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
		}
	}

	private boolean shouldCrowl(File file) {
		// TODO implement this for continuous crawling.
		return true;
	}

	// Create a FileFilter object according to the registered file types
	public class SignatureFileFilter implements FileFilter {
		private final ArrayList<String>	okFileExtensions	= new ArrayList<String>();

		public void addFileTypes(String[] types) {
			for (String type : types)
				okFileExtensions.add(type);
		}

		public boolean accept(File file) {
			if (file.isDirectory())
				return true;

			for (String type : okFileExtensions) {
				if (file.getName().toLowerCase().endsWith(type)) {
					return true;
				}
			}
			return false;
		}
	}

}
