package dwttest;

import java.io.File;

import processing.core.PApplet;
import processing.core.PImage;

public class RasterImageSignature extends WaveletSignature {
	private static final long		serialVersionUID	= -1195866416435093809L;

	// Filetypes supported by this RasterImageSignature
	// Add more? - tiff, bmp
	public static final String[]	FileTypes			= new String[] { ".gif", ".jpg", ".jpeg", ".tga", ".png" };
	private final transient PApplet	parent;

	public RasterImageSignature(String fileName, PApplet p) {
		super();

		this.parent = p;
		this.setFileName(fileName);

		PImage img = parent.loadImage(fileName);

		if (img != null) {
			this.Init(img);
		}
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
