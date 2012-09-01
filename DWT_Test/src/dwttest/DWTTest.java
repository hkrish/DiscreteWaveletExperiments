package dwttest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import math.transform.jwave.Transform;
import math.transform.jwave.handlers.DiscreteWaveletTransform;
import math.transform.jwave.handlers.TransformInterface;
import math.transform.jwave.handlers.wavelets.Daub04;
import processing.core.PApplet;
import processing.core.PImage;

@SuppressWarnings("serial")
public class DWTTest extends PApplet {

	WaveletSignatureFactory factory = new WaveletSignatureFactory();
	WaveletSignatureI sig, sig2;
	
	boolean[] keys = new boolean[526];

	public void setup() {
//		PImage img2 = loadImage("data/Barns_grand_tetons_YCbCr_separation.jpg");
		 PImage img = loadImage("data/2007_MV_Brutale_910_1r.jpg");

		size(512, 512, JAVA2D);
		background(0);

		sig = factory.getWaveletSignature(img);
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File("/Users/hari/Desktop/1.wsig"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(sig);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FileInputStream fis;
		try {
			fis = new FileInputStream("/Users/hari/Desktop/1.wsig");
			ObjectInputStream ois = new ObjectInputStream(fis);
			sig2 = (WaveletSignatureI) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		image(img, 0, 0, 512, 512);
		sig2.factory = sig.factory;
		sig2.loadValues();
		image(sig2.getImage(), 0, 0, this.width, this.height);
		
//		println(factory.euclideanDist(sig, sig2));
		
		img.resize(256, 256);
		img.save("/Users/hari/Desktop/1.jpg");
	}

	public void keyPressed() {
		image(sig.getImage(), 0, 0, this.width, this.height);
		sig.getImage().save("/Users/hari/Desktop/2.jpg");
//		double[][] data = dup(matHilb);
//		boolean inverse = true, cumulative = true;
//
//		keys[keyCode] = true;
//		cumulative = !checkKey(CONTROL);
//
//		switch (key) {
//		case 'z':
//			inverse = false;
//			break;
//
//		default:
//			if (key > '0' && key <= '9')
//				discard(data, key - '0', cumulative);
//		}
//
//		if (inverse)
//			for (int i = 0; i < depth; i++)
//				data = t.reverse(data); // 2-D FWT Haar reverse
//		image(getImage(data), 0, 0);
	}

	public void keyReleased() {
		keys[keyCode] = false;
	}

	public void draw() {
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
