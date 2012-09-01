package dwttest;

import java.io.File;

import processing.core.PApplet;
import processing.core.PImage;

public class RasterImageSignature extends WaveletSignature implements Comparable<WaveletSignature> {
	private static final long		serialVersionUID	= -8859049232985113197L;

	// Filetypes supported by this RasterImageSignature
	// Add more? - tiff, bmp
	public static final String[]	FileTypes			= new String[] { ".gif", ".jpg", ".jpeg", ".tga", ".png" };
	public final transient PApplet	parent;

	public final String				fileName;

	public RasterImageSignature(String fileName, PApplet p) {
		super();

		this.parent = p;
		this.fileName = fileName;

		PImage img = parent.loadImage(fileName);

		if (img != null) {
			this.Init(img);
		}
	}

	@Override
	public int compareTo(WaveletSignature o) {
		if (this.rank < o.rank)
			return -1;
		else if (this.rank > o.rank)
			return 1;
		return 0;
	}
	
	public static boolean accept(File file) {
		if (file.isDirectory())
			return true;

		for (String type : FileTypes) {
			if (file.getName().toLowerCase().endsWith(type)) {
				return true;
			}
		}
		return false;
	}

}
