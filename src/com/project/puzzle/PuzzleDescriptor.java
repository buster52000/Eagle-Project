package com.project.puzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

import javax.imageio.ImageIO;

import com.project.base.Main;

class PuzzleDescriptor {
	//private member data
	private int[] xRedLoc, yRedLoc, xIntervals, yIntervals; 
	private BufferedImage[][] pieces;
	
	private int xPieces, yPieces;
	private Point[][] centers;
	
	private boolean prepared;

	// constructor
	PuzzleDescriptor() {}
	
	//public getters
	public int getXPieces() {return xPieces;}
	public int getYPieces() {return yPieces;}
	public BufferedImage getPiece(int x, int y) {return pieces[x][y];}
	public Point getCenter(int x, int y) {return centers[x][y];}
	public int getXInterval(int x) {return xIntervals[x];}
	public int getYInterval(int x) {return yIntervals[x];}

	private void findRedLocations(final BufferedImage template, ArrayList<Integer> xRedLoc, ArrayList<Integer> yRedLoc) {
		int a = 0;
		for (int y = 0, h = template.getHeight(); (xRedLoc.size() == 0) && (y < h); y++) {
			for (int x = 0, w = template.getWidth(); x < w; x++) {
				if (template.getRGB(x, y) == Color.RED.getRGB()) {
					if (a == 0 && yRedLoc.size() == 0) 
						yRedLoc.add(y);
					xRedLoc.add(x);
				}
			}
		}
		a = 1;
		for (int y = yRedLoc.get(0)+1, h = template.getHeight(), x = xRedLoc.get(0); y < h; y++) {
			if (template.getRGB(x, y) == Color.RED.getRGB()) {
				yRedLoc.add(y);
			}
		}

		for (Integer y : yRedLoc) 
			for (Integer x : xRedLoc)
				template.setRGB(x, y, 0);
	}
	
	// all the work happens here -- after this function finishes, the class is immutable
	public void preparePuzzle(BufferedImage img, BufferedImage unscaledTemplate) {
		if (prepared) 
			throw new IllegalStateException("PuzzleDescriptor.preparePuzzle should only be called once");

		prepared = true; // let's be optimistic
		
		int iw = img.getWidth();
		int ih = img.getHeight();
		
		BufferedImage prescaledCurrentTemplate = copyBufferedImage(unscaledTemplate);
		if (ih > iw) {
			unscaledTemplate = createRotatedCopy(unscaledTemplate, Math.PI / 2);
		}

		ArrayList<Integer> unscaledTemplateXLoc = new ArrayList<Integer>();
		ArrayList<Integer> unscaledTemplateYLoc = new ArrayList<Integer>();
		findRedLocations(unscaledTemplate, unscaledTemplateXLoc, unscaledTemplateYLoc);
		xPieces = unscaledTemplateXLoc.size();
		yPieces = unscaledTemplateYLoc.size();
		
		int w = unscaledTemplate.getWidth();
		int h = unscaledTemplate.getHeight();

		double sW = iw / (double) w;
		double sH = ih / (double) h;
		xRedLoc = new int[xPieces];
		yRedLoc = new int[yPieces];
		for (int yI = 0; yI < unscaledTemplateYLoc.size(); yI++)
			yRedLoc[yI] = (int) (unscaledTemplateYLoc.get(yI) * sH);
		for (int xI = 0; xI < unscaledTemplateXLoc.size(); xI++)
			xRedLoc[xI] = (int) (unscaledTemplateXLoc.get(xI) * sW);

		xIntervals = new int[xPieces - 1];
		yIntervals = new int[yPieces - 1];

		for (int c = 0; c < xIntervals.length; c++) {
			xIntervals[c] = xRedLoc[c + 1] - xRedLoc[c];
		}
		for (int d = 0; d < yIntervals.length; d++) {
			yIntervals[d] = yRedLoc[d + 1] - yRedLoc[d];
		}

		pieces = new BufferedImage[xPieces][yPieces];

		double mX = (double) prescaledCurrentTemplate.getWidth() / (double) iw;
		double mY = (double) prescaledCurrentTemplate.getHeight() / (double) ih;
		centers = new Point[xPieces][yPieces];

		Image scaledTemplateImage = unscaledTemplate.getScaledInstance(iw, ih, Image.SCALE_SMOOTH);
		BufferedImage bufferedScaledTemplateImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
		bufferedScaledTemplateImage.getGraphics().drawImage(scaledTemplateImage, 0, 0, null);

		int xStep = iw / xPieces;
		int yStep = ih / yPieces;
		for (int j = 0, cY = yStep / 2; j < yPieces; j++, cY += yStep) {
			for (int k = 0, cX = xStep / 2; k < xPieces; k++, cX += xStep) {
				pieces[k][j] = getPiece(cX, cY, bufferedScaledTemplateImage, img, null);
			}
		}

		for (int j = 0; j < unscaledTemplateYLoc.size(); j++) {
			for (int i = 0; i < unscaledTemplateXLoc.size(); i++) {
				int unscaledCenter[] = new int[2];
				findCenter(unscaledTemplateXLoc.get(i), unscaledTemplateYLoc.get(j), prescaledCurrentTemplate, prescaledCurrentTemplate, unscaledCenter);
				centers[i][j] = new Point((int) (unscaledCenter[0] / mX), (int) (unscaledCenter[1] / mY));
			}
		}
		
	}
	
	private BufferedImage createRotatedCopy(BufferedImage img, double rotation) {
		int w = img.getWidth();
		int h = img.getHeight();

		BufferedImage rot = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);

		AffineTransform xform = new AffineTransform();
		xform.translate(0.5 * h, 0.5 * w);
		xform.rotate(rotation);
		xform.translate(-0.5 * w, -0.5 * h);
		Graphics2D g = (Graphics2D) rot.createGraphics();
		g.drawImage(img, xform, null);
		g.dispose();

		return rot;
	}
	
	private BitSet filled = new BitSet();
	private BitSet toVisit = new BitSet();
	
	// doesn't change pixels
	private BitSet floodIt(int[] pixels, int seedx, int seedy, int width, int height, int oldColor, int fillColor) {

		filled.clear(); toVisit.clear();
		
		int point = seedy*width+seedx;
		toVisit.set(point);
		
		int red = Color.RED.getRGB();
		while (point != -1) {
			toVisit.clear(point);
			int nextPointToVisit = -1;
			if (!filled.get(point)) {
				
				int y = point / width;
				int x = point - y * width;
				int yp = point - x;

				int xr = x;
				
				int tt = xr + yp;
				do {
					xr++;
					tt++;
				} while (xr < width && (pixels[tt] == oldColor || pixels[tt] == red) && !filled.get(tt));
				filled.set(x + yp, tt);
				
				int xl = x;
				tt = xl + yp;
				do {
					xl--;
					tt--;
				} while (xl >= 0 && (pixels[tt] == oldColor || pixels[tt] == red) && !filled.get(tt));
				filled.set(tt+1, x+yp);
				
				xr--;
				xl++;

				int ypp = yp + width;
				int ypm = yp - width;
				for (int xi = xl, tm = xl+ypm, tp = xl+ypp; xi <= xr; xi++, tm++, tp++) {
					if (y > 0 && (pixels[tm] == oldColor || pixels[tm] == red) && !filled.get(tm)) {
						if (nextPointToVisit == -1) nextPointToVisit = tm;
						else toVisit.set(tm);
					}
					if (y < height - 1 && (pixels[tp] == oldColor || pixels[tp] == red) && !filled.get(tp)) {
						if (nextPointToVisit == -1) nextPointToVisit = tp;
						else toVisit.set(tp);
					}
				}
				
				toVisit.andNot(filled);
			}
			
			point = (nextPointToVisit == -1) ? toVisit.nextSetBit(0) : nextPointToVisit;
		}
		return filled;
	}
	

	private BitSet fill(BufferedImage img, int xSeed, int ySeed, Color col) {
		int x = xSeed;
		int y = ySeed;
		int width = img.getWidth();
		int height = img.getHeight();

		int pixels [] = null;
		WritableRaster wr = img.getRaster();
		if (wr.getDataBuffer() instanceof DataBufferInt) {
			pixels = ((DataBufferInt) wr.getDataBuffer()).getData();
		}
		if (pixels == null) {
			Main.errMsg("Undesirable extra BufferedImage conversion being done...", false);
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			bi.getGraphics().drawImage(img, 0, 0, null);
			pixels = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
		}

		if (x >= 0 && x < width && y >= 0 && y < height) {

			// ignore any red marks (the marks were put there for reference, but shouldn't be used to find boundaries)
			int oldColor = pixels[y*width + x] == Color.RED.getRGB() ? 0 : pixels[y * width + x];
			int fillColor = col.getRGB();

			if (oldColor != fillColor) {
				return floodIt(pixels, x, y, width, height, oldColor, fillColor);
			}
		}
		return null;
	}
	
	private BufferedImage getPiece(int seedx, int seedy, BufferedImage template, BufferedImage origImg, int center[]) {
		int width = template.getWidth(null);
		BitSet pixles = fill(template, seedx, seedy, Color.GREEN);
		int maxH = 1;
		int maxW = 1;
		int minW = width;
		int minH = width;
		for (int i = pixles.nextSetBit(0); i >= 0; i = pixles.nextSetBit(i+1)) {
			int y = i / width;
			int x = i - y*width;
			if (x > maxW) maxW = x;
			if (y > maxH) maxH = y;
			if (x < minW) minW = x;
			if (y < minH) minH = y;
		}
		BufferedImage image = new BufferedImage(maxW - minW + 1, maxH - minH + 1, BufferedImage.TYPE_INT_ARGB);
		for (int i = pixles.nextSetBit(0); i >= 0; i = pixles.nextSetBit(i+1)) {
			int y = i / width;
			int x = i - y*width;
			int pix = origImg.getRGB(x, y);
			image.setRGB(x - minW, y - minH, pix);
			if (pix == Color.RED.getRGB() && center != null) {
				center[0] = x - minW;
				center[1] = y - minH;
			}
		}
		return image;
	}

	private void findCenter(int seedx, int seedy, BufferedImage template, BufferedImage image, int center[]) {
		int width = template.getWidth();
		BitSet pixles = fill(template, seedx, seedy, Color.GREEN);
		int minW = template.getWidth();
		int minH = template.getHeight();
		int centerx = -1, centery = -1;
		for (int i = pixles.nextSetBit(0); i >= 0; i = pixles.nextSetBit(i+1)) {
			int y = i / width;
			int x = i - y*width;
			if (x < minW) minW = x;
			if (y < minH) minH = y;
			if (image.getRGB(x,y) == Color.RED.getRGB()) {
				centerx = x;
				centery = y;
			}
		}
		center[0] = centerx - minW;
		center[1] = centery - minH;
	}

	private static BufferedImage copyBufferedImage(BufferedImage bi) {
		BufferedImage bi2 = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
		bi2.getGraphics().drawImage(bi, 0, 0, null);
		return bi2;
	}

	

	private static BufferedImage loadFirstTemplateForTesting() {
		File dir = new File("gameFiles/puzzleTemplates/");
		if (!dir.exists()) return null;
		File[] files = dir.listFiles();
		for (File f : files) {
			if (!f.getName().endsWith(".txt")) {
				BufferedImage img = null;
				try {
					img = ImageIO.read(f);
					return img;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		}
		return null;
	}
	
	private static Puzzle loadFirstPuzzleForTesting() {
		// get first puzzle
		File dir = new File("gameFiles/puzzles/");
		if (!dir.exists()) return null;
		File[] files = dir.listFiles();
		for (File f : files) {
			String path = null;
			String name = null;
			String description = null;
			try {
				BufferedReader read = new BufferedReader(new FileReader(f));
				path = read.readLine();
				description = read.readLine();
				name = read.readLine();
				read.close();
			} catch (FileNotFoundException e) {
				Main.saveStackTrace(e);
			} catch (IOException e) {
				Main.saveStackTrace(e);
			}
			if (path != null && name != null && description != null) {
				BufferedImage img = null;
				try {
					img = ImageIO.read(new File(path));
					return new Puzzle(img, description, name);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Main.errMsg("The puzzle file " + f.getName() + "did not load correctly", false);
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		Puzzle p = PuzzleDescriptor.loadFirstPuzzleForTesting();
		if (p != null) System.out.println("Puzzle image loaded");
		BufferedImage template = PuzzleDescriptor.loadFirstTemplateForTesting();
		if (template != null) System.out.println("Template image loaded");
		
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
		if (p != null && template != null) {

			long startNanos = System.nanoTime();
			for (int i = 0; i < loops; i++) {
				long sNano = System.nanoTime();
				PuzzleDescriptor d = new PuzzleDescriptor();
				d.preparePuzzle(p.getImage(), copyBufferedImage(template));
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
