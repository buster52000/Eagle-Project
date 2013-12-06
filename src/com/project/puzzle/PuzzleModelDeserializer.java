package com.project.puzzle;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import com.project.base.BaseUtils;
import com.project.base.Main;

public class PuzzleModelDeserializer {
	private static final int SCHEMA_VERSION = 1;
	
	private int xsize, ysize;
	private Point centers[][];
	private double[] xIntervals, yIntervals; 
	private BufferedImage[][] pieces;
	private BufferedImage originalImage;

	public PuzzleModelDeserializer() {
	}
	
	public PuzzleModel read(String filenameRoot) {
		if (readDatafile(filenameRoot)) {
			if (readPieces(filenameRoot)) {
				if (readOriginalImage(filenameRoot)) {
					return new PuzzleModel(xsize, ysize, pieces, centers, xIntervals, yIntervals, originalImage);
				}
			}
		}
		return null;
	}

	
	
	// 
	// input implementation stuff
	//
	private boolean readDatafile(String filenameRoot) {
		String datafile = filenameRoot+"_puzzle.dat";
		InputStream is = null;
		DataInputStream dis = null;
		try {
			is = new BufferedInputStream(new FileInputStream(new File(datafile)));
			dis = new DataInputStream(is);
			int schema = dis.readInt();
			if (schema > SCHEMA_VERSION) {
				// this code base cannot understand the file's schema...
				Main.errMsg("Schema error while reading a puzzle descriptor cache; skipping this one; performance will suffer", false);
				return false;
			}

			xsize = dis.readInt();
			ysize = dis.readInt();
			centers = new Point[xsize][ysize];
			xIntervals = new double[xsize - 1];
			yIntervals = new double[ysize - 1];
			pieces = new BufferedImage[xsize][ysize];

			for (int y = 0; y < ysize; y++) {
				for (int x = 0; x < xsize; x++) {
					if (Thread.currentThread().isInterrupted())
						return false;
					int px = dis.readInt();
					int py = dis.readInt();
					centers[x][y] = new Point(px, py);
				}
			}
			
			for (int y = 0; y < ysize-1; y++) {
				yIntervals[y] = dis.readDouble();
			}
			for (int x = 0; x < xsize-1; x++) {
				xIntervals[x] = dis.readDouble();
			}

		} catch (FileNotFoundException e) {
			Main.errMsg("Error while reading a puzzle descriptor cache; skipping this one; performance will suffer", false);
			return false;
		} catch (IOException e) {
			Main.errMsg("Error while reading a puzzle descriptor cache; skipping this one; performance will suffer", false);
			return false;
		} finally {
			try {
				if (dis != null) 
					dis.close();
				if (is != null) 
					is.close();
			} catch (IOException e) {
				// don't care if this fails
			}
		}
		
		return true;
	}
	
	private boolean readPieces(String filenameRoot) {
		for (int y = 0; y < ysize; y++) {
			for (int x = 0; x < xsize; x++) {
				if (Thread.currentThread().isInterrupted())
					return false;
				String pieceFilename = filenameRoot + "_piece_" + y + "_" + x + ".png";
				BufferedImage pieceImg = null;
				try {
					pieceImg = BaseUtils.loadImage(new File(pieceFilename).toURI().toURL(), Integer.MAX_VALUE);
				} catch (MalformedURLException e) {
					// shouldn't happen 'case it's always a filename
					e.printStackTrace();
				}
				if (pieceImg == null) 
					return false;
				pieces[x][y] = pieceImg;
			}
		}
		return true;
	}
	
	private boolean readOriginalImage(String filenameRoot) {
		String filename = filenameRoot + "_img.png";
		try {
			originalImage = BaseUtils.loadImage(new File(filename).toURI().toURL(), Integer.MAX_VALUE);
		} catch (MalformedURLException e) {
			// shouldn't happen 'case it's always a filename
			e.printStackTrace();
		}
		return (originalImage != null);
	}

}
