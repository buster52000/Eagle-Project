package com.project.puzzle;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.project.base.Main;

public class PuzzleModelSerializer {
	private static final int SCHEMA_VERSION = 1;
	
	private PuzzleModel d; 

	public PuzzleModelSerializer(PuzzleModel d) {
		this.d = d;
	}
	
	public boolean write(String filenameRoot) {
		if (writeDatafile(filenameRoot)) {
			if (writePieces(filenameRoot)) {
				if (writeOriginalImage(filenameRoot)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	// 
	// output implementation stuff
	//
	private boolean writeImage(BufferedImage bi, String filename) {
		try {
		    File outputfile = new File(filename);
		    ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {
			Main.errMsg("Cannot write cache file for image: "+filename, false);
			return false;
		}
		
		return true;
	}
	
	private boolean writeOriginalImage(String filenameRoot) {
		String filename = filenameRoot + "_img.png";
		return writeImage(d.getOriginalImage(), filename);
	}

	private boolean writePieces(String filenameRoot) {
		for (int y = 0; y < d.getYPieces(); y++) {
			for (int x = 0; x < d.getXPieces(); x++) {
				String pieceFilename = filenameRoot + "_piece_" + y + "_" + x + ".png";
				boolean status = writeImage(d.getPiece(x, y), pieceFilename);
				if (!status) 
					return false;
			}
		}
		
		return true;
	}

	private boolean writeDatafile(String filenameRoot) {
		String datafile = filenameRoot+"_puzzle.dat";
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		DataOutputStream dos = null;
		try {
			fos = new FileOutputStream(new File(datafile));
			bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			
			dos.writeInt(SCHEMA_VERSION);
			dos.writeInt(d.getXPieces());
			dos.writeInt(d.getYPieces());
			
			for (int y = 0; y < d.getYPieces(); y++) {
				for (int x = 0; x < d.getXPieces(); x++) {
					Point p = d.getCenter(x,  y);
					dos.writeInt(p.x);
					dos.writeInt(p.y);
				}
			}
			
			for (int y = 0; y < d.getYPieces()-1; y++) {
				dos.writeDouble(d.getYInterval(y));
			}
			for (int x = 0; x < d.getXPieces()-1; x++) {
				dos.writeDouble(d.getXInterval(x));
			}
				
		} catch (FileNotFoundException e) {
			Main.errMsg("Error while writing a puzzle descriptor cache; skipping this one and performance may suffer in the future", false);
			return false;
		} catch (IOException e) {
			Main.errMsg("Error while writing a puzzle descriptor cache; skipping this one and performance may suffer in the future", false);
			return false;
		} finally {
			try {
				if (dos != null) 
					dos.close();
				if (bos != null)
					bos.close();
				if (fos != null) 
					fos.close();
			} catch (IOException e) {
				// don't care if it cannot close...
			}
		}
		
		return true;
	}

}
