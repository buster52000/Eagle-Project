package com.project.puzzle;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

class PuzzleModel {
	//private member data
	private double[] xIntervals, yIntervals; 
	private BufferedImage[][] pieces;
	private BufferedImage image;
	
	private int xPieces, yPieces;
	private Point[][] centers;
	
	public PuzzleModel(int xSize, int ySize, BufferedImage pieces[][], Point centers[][], double xIntervals[], double yIntervals[], BufferedImage image) {
		this.xPieces = xSize;
		this.yPieces = ySize;
		this.pieces = pieces;
		this.centers = centers;
		this.xIntervals = xIntervals;
		this.yIntervals = yIntervals;
		this.image = image;
	}
	
	//public getters
	public int getXPieces() {return xPieces;}
	public int getYPieces() {return yPieces;}
	public BufferedImage getPiece(int x, int y) {return pieces[x][y];}
	public Point getCenter(int x, int y) {return centers[x][y];}
	public double getXInterval(int x) {return xIntervals[x];}
	public double getYInterval(int x) {return yIntervals[x];}
	public BufferedImage getOriginalImage() {return image;}


	private boolean imagesEqual(BufferedImage i1, BufferedImage i2) {
		int w = i1.getWidth();
		int h = i1.getHeight();
		if (w != i2.getWidth())
			return false;
		if (h != i2.getHeight())
			return false;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int p1 = i1.getRGB(x, y);
				int p2 = i2.getRGB(x, y);
				if (p1 != p2) 
					return false;
			}
		}
		
		return true;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof PuzzleModel))
			return false;
		
		PuzzleModel o = (PuzzleModel) other;
		if (xPieces != o.xPieces)
			return false;
		if (yPieces != o.yPieces)
			return false;
		if (xIntervals.length != o.xIntervals.length)
			return false;
		if (yIntervals.length != o.yIntervals.length)
			return false;
		if (pieces.length != o.pieces.length) 
			return false;
		if (centers.length != o.centers.length)
			return false;
		if (!imagesEqual(image, o.image))
			return false;
		
		for (int i = 0; i < xPieces-1; i++)
			if (xIntervals[i] != o.xIntervals[i])
				return false;
		
		for (int i = 0; i < yPieces-1; i++)
			if (yIntervals[i] != o.yIntervals[i])
				return false;

		for (int x = 0; x < xPieces; x++) {
			if (pieces[x].length != o.pieces[x].length)
				return false;
			if (centers[x].length != o.centers[x].length)
				return false;
			for (int y = 0; y < yPieces; y++) {
				if (!imagesEqual(pieces[x][y], o.pieces[x][y]))
					return false;
				if (centers[x][y].x != o.centers[x][y].x)
					return false;
				if (centers[x][y].y != o.centers[x][y].y)
					return false;
			}
		}
		
		return true;
	}
	

	private static URL loadFirstTemplateForTesting() {
		List<URL> templateUrls= PuzzleBase.loadTemplateNames("/gameFiles/puzzleTemplates/test.txt");
		if (templateUrls.size() < 1) 
			return null;

		return templateUrls.get(0);
	}
	
	private static Puzzle loadFirstPuzzleForTesting() {
		List<Puzzle> puzzles = PuzzleBase.loadPuzzles("/gameFiles/puzzles/test.txt");
		if (puzzles.size() < 1) 
			return null;
		else
			return puzzles.get(0);
	}
	
	public static void main(String[] args) {
		Puzzle p = PuzzleModel.loadFirstPuzzleForTesting();
		if (p != null) System.out.println("Puzzle image loaded");
		URL templateUrl = PuzzleModel.loadFirstTemplateForTesting();
		
		int loops = 1;
		if (args.length < 1 || !"-r".equals(args[0])) {
			System.out.println("No loop counter supplied; preparing the puzzle once (to repeat, invoke with \"-r #\")");
		} else {
			try {
				loops = Integer.valueOf(args[1]);
			} catch (NumberFormatException nfe) {
				System.out.println("Syntax error for loop counter, so we'll prepare the puzzle just once; to repeat, invoke with \"-r #\")");
			}
		}
		if (p != null && templateUrl != null) {

			long startNanos = System.nanoTime();
			for (int i = 0; i < loops; i++) {
				long sNano = System.nanoTime();
				PuzzleInstaller installer = new PuzzleInstaller();
				PuzzleModel d = installer.createFromImageUrls(p.getImageUrl(), templateUrl);
				if (d == null) {
					System.err.println("Error creating the PuzzleModel");
				}
				if (loops > 1) {
					long eNano = System.nanoTime();
					System.out.printf("Finished pass %d through the loop in %f milliseconds\n", i, (eNano - sNano)/1000000.0);
				}
			}
			long endNanos = System.nanoTime();
			
			System.out.printf("Test completed; took %f milliseconds for %d passes through puzzle preparation\n", (endNanos - startNanos)/1000000.0, loops);
		} else {
			System.err.println("Test failed");
		}
	}
}
