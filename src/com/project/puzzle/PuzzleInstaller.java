package com.project.puzzle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import com.project.base.BaseUtils;
import com.project.base.Main;

public class PuzzleInstaller {
	private int xPieces, yPieces;
	private BufferedImage pieces[][];
	private Point centers[][];
	private double xIntervals[], yIntervals[];
	private BufferedImage img;
	
	public PuzzleModel createAndInstallFromUrls(URL imageUrl, URL templateUrl, String name) {
		String filenameRoot = getRootDir(imageUrl, templateUrl, name);
		PuzzleModel model = createFromImageUrls(imageUrl, templateUrl);
		PuzzleModelSerializer serializer = new PuzzleModelSerializer(model);
		boolean stat = serializer.write(filenameRoot);
		if (stat) {
			Main.infoMsg("Succeeded to write puzzle descriptor: "+name+" template "+templateUrl);
		} else {
			Main.errMsg("Failed to write puzzle descriptor: "+name+" template "+templateUrl, false);
		}
		return model;
	}
	
	// all the heavy lifting happens here -- after this function finishes, it returns an immutable instance of PuzzleModel
	public PuzzleModel createFromImageUrls(URL imageUrl, URL templateUrl) {
		// java - get screen size using the Toolkit class
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int puzzleHeight = screenSize.height - 125; // 125 ~ the height of the stuff above the puzzle panel
		puzzleHeight = (int) (puzzleHeight * 0.6); // leave some space so that the pieces can be spread out a bit
		img = BaseUtils.loadImage(imageUrl, puzzleHeight);
		BufferedImage unscaledTemplate = BaseUtils.loadImage(templateUrl, Integer.MAX_VALUE);
		int iw = img.getWidth(null);
		int ih = img.getHeight(null);
		
		BufferedImage prescaledCurrentTemplate = copyBufferedImage(unscaledTemplate);
		if (ih > iw) {
			unscaledTemplate = createRotatedCopy(unscaledTemplate, Math.PI / 2);
		}
		
		if (Thread.currentThread().isInterrupted())
			return null;

		List<Integer> unscaledTemplateXLoc = new ArrayList<Integer>();
		List<Integer> unscaledTemplateYLoc = new ArrayList<Integer>();
		findRedLocations(unscaledTemplate, unscaledTemplateXLoc, unscaledTemplateYLoc);
		xPieces = unscaledTemplateXLoc.size();
		yPieces = unscaledTemplateYLoc.size();
		
		int w = unscaledTemplate.getWidth();
		int h = unscaledTemplate.getHeight();

		double sW = iw / (double) w;
		double sH = ih / (double) h;
		double [] xRedLoc = new double[xPieces];
		double [] yRedLoc = new double[yPieces];
		for (int yI = 0; yI < unscaledTemplateYLoc.size(); yI++)
			yRedLoc[yI] = unscaledTemplateYLoc.get(yI) * sH;
		for (int xI = 0; xI < unscaledTemplateXLoc.size(); xI++)
			xRedLoc[xI] = unscaledTemplateXLoc.get(xI) * sW;

		xIntervals = new double[xPieces - 1];
		yIntervals = new double[yPieces - 1];

		for (int c = 0; c < xIntervals.length; c++) 
			xIntervals[c] = xRedLoc[c + 1] - xRedLoc[c];
		for (int d = 0; d < yIntervals.length; d++) 
			yIntervals[d] = yRedLoc[d + 1] - yRedLoc[d];

		pieces = new BufferedImage[xPieces][yPieces];

		double mX = (double) prescaledCurrentTemplate.getWidth() / (double) iw;
		double mY = (double) prescaledCurrentTemplate.getHeight() / (double) ih;
		centers = new Point[xPieces][yPieces];

		Image scaledTemplateImage = unscaledTemplate.getScaledInstance(iw, ih, Image.SCALE_SMOOTH);
		BufferedImage bufferedScaledTemplateImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
		bufferedScaledTemplateImage.getGraphics().drawImage(scaledTemplateImage, 0, 0, null);

		if (Thread.currentThread().isInterrupted())
			return null;
		
		int xStep = iw / xPieces;
		int yStep = ih / yPieces;
		for (int j = 0, cY = yStep / 2; j < yPieces; j++, cY += yStep) {
			for (int k = 0, cX = xStep / 2; k < xPieces; k++, cX += xStep) {
				if (Thread.currentThread().isInterrupted())
					return null;
				pieces[k][j] = getPiece(cX, cY, bufferedScaledTemplateImage, img, null);
			}
		}

		for (int j = 0; j < unscaledTemplateYLoc.size(); j++) {
			for (int i = 0; i < unscaledTemplateXLoc.size(); i++) {
				if (Thread.currentThread().isInterrupted())
					return null;
				int unscaledCenter[] = new int[2];
				findCenter(unscaledTemplateXLoc.get(i), unscaledTemplateYLoc.get(j), prescaledCurrentTemplate, prescaledCurrentTemplate, unscaledCenter);
				centers[i][j] = new Point((int) (unscaledCenter[0] / mX  + 0.5), (int) (unscaledCenter[1] / mY + 0.5));
			}
		}
		
		return new PuzzleModel(xPieces, yPieces, pieces, centers, xIntervals, yIntervals, img);
	}


	//
	// Utility functions for managing file locations 
	//
	public static String getRootDir(URL imageUrl, URL templateUrl, String name) {
		UUID uuid = generateUUID(imageUrl, templateUrl);
		String rootDir = System.getProperty("user.home")+"/gamecache/"+name;
		File dir = new File(rootDir);
		if (!dir.exists()) 
			dir.mkdirs();
		return rootDir+"/"+uuid;
	}

	private static UUID generateUUID(URL imageUrl, URL templateUrl) {
		String concatenated = imageUrl.toString() + templateUrl.toString();
		UUID uuid = UUID.nameUUIDFromBytes(concatenated.getBytes());
		return uuid;
	}


	//
	// puzzle preparation implementations
	//
	private void findRedLocations(final BufferedImage template, List<Integer> xRedLoc, List<Integer> yRedLoc) {
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

	
	public static void main(String[] args) {
		List<Puzzle> puzzles = PuzzleBase.loadPuzzles("/gameFiles/puzzles/list.txt");
		List<URL> templateFilenames = PuzzleBase.loadTemplateNames("/gameFiles/puzzleTemplates/list.txt");

		for (Puzzle p : puzzles) {
			int templateCnt = 0;
			for (URL templateUrl : templateFilenames) {
				String filenameRoot = getRootDir(p.getImageUrl(), templateUrl, p.getName());
				
				PuzzleInstaller installer = new PuzzleInstaller();
				PuzzleModel model = installer.createFromImageUrls(p.getImageUrl(), templateUrl);
				PuzzleModelSerializer serializer = new PuzzleModelSerializer(model);
				boolean stat = serializer.write(filenameRoot);
				if (stat) {
					Main.infoMsg("Succeeded to write puzzle descriptor: "+p.getName()+" template "+templateCnt);
				} else {
					Main.errMsg("Failed to write puzzle descriptor: "+p.getName()+" template "+templateCnt, false);
				}
				templateCnt++;
				
				if (stat) {
					// deserialize and compare as confirmation
					PuzzleModelDeserializer testDeserializer = new PuzzleModelDeserializer();
					PuzzleModel testDesc = testDeserializer.read(filenameRoot);
					
					if (testDesc == null || !testDesc.equals(model)) {
						Main.errMsg("Verification failed for puzzle descriptor: "+p.getName(), false);
					}
				}
			}
		}
		Main.infoMsg("Done installing all puzzle/template combinations");
	}

}
