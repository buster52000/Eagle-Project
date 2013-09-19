package com.project.puzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

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

	// all the work happens here -- after this function finishes, the class is immutable
	public void preparePuzzle(BufferedImage img, BufferedImage template) {
		if (prepared) 
			throw new IllegalStateException("PuzzleDescriptor.preparePuzzle should only be called once");

		prepared = true; // let's be optimistic
		
		BufferedImage currentTemplate = copyBufferedImage(template);
		if (img.getHeight() > img.getWidth()) {
			template = createRotatedCopy(template, Math.PI / 2);
			xPieces = 4;
			yPieces = 6;
		} else {
			xPieces = 6;
			yPieces = 4;
		}

		xRedLoc = new int[xPieces];
		yRedLoc = new int[yPieces];

		int y = 0;
		boolean red = false;
		while (!red) {
			for (int x = 0; x < template.getWidth(); x++) {
				if (template.getRGB(x, y) == Color.RED.getRGB()) {
					red = true;
				}
			}
			if (!red)
				y++;
		}
		int a = 0;
		int x = 0;
		while (x < template.getWidth()) {
			if (template.getRGB(x, y) == Color.RED.getRGB()) {
				xRedLoc[a] = x;
				a++;
			}
			x++;
		}

		x = 0;
		red = false;
		while (!red) {
			for (y = 0; y < template.getHeight(); y++) {
				if (template.getRGB(x, y) == Color.RED.getRGB()) {
					red = true;
				}
			}
			if (!red)
				x++;
		}
		a = 0;
		y = 0;
		while (y < template.getHeight()) {
			if (template.getRGB(x, y) == Color.RED.getRGB()) {
				yRedLoc[a] = y;
				a++;
			}
			y++;
		}

		for (x = 0; x < template.getWidth(); x++)
			for (y = 0; y < template.getHeight(); y++)
				if (template.getRGB(x, y) == Color.RED.getRGB())
					template.setRGB(x, y, 0);

		int w = template.getWidth();
		int h = template.getHeight();

		Image t = template.getScaledInstance(img.getWidth(), img.getHeight(), Image.SCALE_SMOOTH);
		template = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		template.getGraphics().drawImage(t, 0, 0, null);

		double sW = template.getWidth() / (double) w;
		double sH = template.getHeight() / (double) h;
		for (int yI = 0; yI < yRedLoc.length; yI++)
			yRedLoc[yI] = (int) (yRedLoc[yI] * sH);
		for (int xI = 0; xI < xRedLoc.length; xI++)
			xRedLoc[xI] = (int) (xRedLoc[xI] * sW);

		xIntervals = new int[xPieces - 1];
		yIntervals = new int[yPieces - 1];

		for (int c = 0; c < xIntervals.length; c++) {
			xIntervals[c] = xRedLoc[c + 1] - xRedLoc[c];
		}
		for (int d = 0; d < yIntervals.length; d++) {
			yIntervals[d] = yRedLoc[d + 1] - yRedLoc[d];
		}

		int[] xCoords = new int[xPieces];
		int[] yCoords = new int[yPieces];
		int width = template.getWidth();
		int height = template.getHeight();
		int xStep = width / xPieces;
		int yStep = height / yPieces;
		int cX = xStep / 2;
		for (int i = 0; i < xPieces; i++) {
			xCoords[i] = cX;
			cX += xStep;
		}
		int cY = yStep / 2;
		for (int i = 0; i < yPieces; i++) {
			yCoords[i] = cY;
			cY += yStep;
		}

		pieces = new BufferedImage[xPieces][yPieces];

		double mX = (double) currentTemplate.getWidth() / (double) template.getWidth();
		double mY = (double) currentTemplate.getHeight() / (double) template.getHeight();
		centers = new Point[xPieces][yPieces];

		int pX = 0;
		int pY = 0;
		BufferedImage cTemplateNoRed = copyBufferedImage(currentTemplate);
		for (int b = 0; b < cTemplateNoRed.getWidth(); b++) {
			for (int c = 0; c < cTemplateNoRed.getHeight(); c++) {
				if (cTemplateNoRed.getRGB(b, c) == Color.RED.getRGB()) {
					cTemplateNoRed.setRGB(b, c, 0);
				}
			}
		}
		for (int j : yCoords) {
			for (int k : xCoords) {
				pieces[pX][pY] = getPiece(k, j, template, img);
				pX++;
			}
			pY++;
			pX = 0;
		}

		pX = 0;
		pY = 0;
		for (int j = 0; j < currentTemplate.getHeight(); j++) {
			for (int i = 0; i < currentTemplate.getWidth(); i++) {
				if (currentTemplate.getRGB(i, j) == Color.RED.getRGB()) {
					BufferedImage p = getPiece(i, j, cTemplateNoRed, currentTemplate);
					for (int b = 0; b < p.getWidth(); b++)
						for (int c = 0; c < p.getHeight(); c++)
							if (p.getRGB(b, c) == Color.RED.getRGB()) {
								centers[pX][pY] = new Point((int) (b / mX), (int) (c / mY));
							}
					pX++;
					if (pX == xPieces) {
						pX = 0;
						pY++;
					}
				}
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
	
	private ArrayList<Integer> floodIt(int[] pixels, int x, int y, int width, int height, int oldColor, int fillColor) {

		int[] point = new int[] { x, y };
		LinkedList<int[]> points = new LinkedList<int[]>();
		ArrayList<Integer> points1 = new ArrayList<Integer>();
		points.addFirst(point);
		while (!points.isEmpty()) {
			point = points.remove();

			x = point[0];
			y = point[1];
			int xr = x;

			int yp = y * width;
			int ypp = yp + width;
			int ypm = yp - width;

			do {
				pixels[xr + yp] = fillColor;
				points1.add(xr + yp);
				xr++;
			} while (xr < width && (pixels[xr + y * width] == oldColor || pixels[xr + y * width] == Color.RED.getRGB()));

			int xl = x;
			do {
				pixels[xl + yp] = fillColor;
				points1.add(xl + yp);
				xl--;
			} while (xl >= 0 && (pixels[xl + y * width] == oldColor || pixels[xl + y * width] == Color.RED.getRGB()));

			xr--;
			xl++;

			boolean upLine = false;
			boolean downLine = false;

			for (int xi = xl; xi <= xr; xi++) {
				if (y > 0 && (pixels[xi + ypm] == oldColor || pixels[xi + ypm] == Color.RED.getRGB()) && !upLine) {
					points.addFirst(new int[] { xi, y - 1 });
					upLine = true;
				} else {
					upLine = false;
				}
				if (y < height - 1 && (pixels[xi + ypp] == oldColor || pixels[xi + ypp] == Color.RED.getRGB()) && !downLine) {
					points.addFirst(new int[] { xi, y + 1 });
					downLine = true;
				} else {
					downLine = false;
				}
			}
		}
		return points1;
	}
	
	private ArrayList<Integer> fill(Image img, int xSeed, int ySeed, Color col) {
		BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(img, 0, 0, null);
		int x = xSeed;
		int y = ySeed;
		int width = bi.getWidth();
		int height = bi.getHeight();

		DataBufferInt data = (DataBufferInt) (bi.getRaster().getDataBuffer());
		int[] pixels = data.getData();

		if (x >= 0 && x < width && y >= 0 && y < height) {

			int oldColor = pixels[y * width + x];
			int fillColor = col.getRGB();

			if (oldColor != fillColor) {
				return floodIt(pixels, x, y, width, height, oldColor, fillColor);
			}
		}
		return null;
	}
	
	private BufferedImage getPiece(int x, int y, BufferedImage template, BufferedImage origImg) {
		int width = template.getWidth();
		ArrayList<Integer> pixles = fill(template, x, y, Color.GREEN);
		ArrayList<int[]> points = new ArrayList<int[]>();
		for (int i : pixles) {
			int[] j = new int[2];
			j[0] = i % width;
			j[1] = (i - j[0]) / width;
			points.add(j);
		}
		int maxH = 1;
		int maxW = 1;
		int minW = template.getWidth();
		int minH = template.getHeight();
		for (int[] i : points) {
			if (i[0] > maxW)
				maxW = i[0];
			if (i[1] > maxH)
				maxH = i[1];
			if (i[0] < minW)
				minW = i[0];
			if (i[1] < minH)
				minH = i[1];
		}
		BufferedImage image = new BufferedImage(maxW - minW + 1, maxH - minH + 1, BufferedImage.TYPE_INT_ARGB);
		for (int[] i : points) {
			image.setRGB(i[0] - minW, i[1] - minH, origImg.getRGB(i[0], i[1]));
		}
		return image;
	}

	private static BufferedImage copyBufferedImage(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
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
