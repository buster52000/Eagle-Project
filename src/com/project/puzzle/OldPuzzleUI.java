package com.project.puzzle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.project.base.Main;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Frame;

@SuppressWarnings("serial")
public class OldPuzzleUI extends JFrame implements MouseListener {

	private JPanel contentPane, puzzlePanel, buttonPanel;
	private PicturePanel[][] piecePanels;
	private JLabel lblSigalMuseum;
	private JButton btnMenu, btnRestart;
	private boolean exit;//, complete;
	private BufferedImage[][] pieces;
	private BufferedImage currentImage, currentTemplate;
	private int[] xRedLoc, yRedLoc, xIntervals, yIntervals;

	public OldPuzzleUI() {

		setExtendedState(Frame.MAXIMIZED_BOTH);
		setUndecorated(true);
		setBackground(Color.BLACK);
		setForeground(Color.WHITE);

		exit = false;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setForeground(Color.WHITE);
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		buttonPanel = new JPanel();
		buttonPanel.setForeground(Color.WHITE);
		buttonPanel.setBackground(Color.BLACK);
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.gridwidth = 2;
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 5);
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 0;
		contentPane.add(buttonPanel, gbc_buttonPanel);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_buttonPanel.rowHeights = new int[] { 0, 0 };
		gbl_buttonPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_buttonPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		buttonPanel.setLayout(gbl_buttonPanel);

		btnMenu = new JButton("Main Menu");
		GridBagConstraints gbc_btnMenu = new GridBagConstraints();
		gbc_btnMenu.insets = new Insets(0, 0, 0, 5);
		gbc_btnMenu.anchor = GridBagConstraints.WEST;
		gbc_btnMenu.gridx = 0;
		gbc_btnMenu.gridy = 0;
		buttonPanel.add(btnMenu, gbc_btnMenu);
		btnMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameOver();
			}
		});

		btnRestart = new JButton("Restart Puzzle");
		GridBagConstraints gbc_btnRestart = new GridBagConstraints();
		gbc_btnRestart.gridx = 1;
		gbc_btnRestart.gridy = 0;
		buttonPanel.add(btnRestart, gbc_btnRestart);
		btnRestart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				nextPuzzle(currentImage, currentTemplate);
			}
		});

		lblSigalMuseum = new JLabel("Sigal Museum");
		lblSigalMuseum.setForeground(Color.WHITE);
		lblSigalMuseum.setBackground(Color.BLACK);
		lblSigalMuseum.setFont(new Font("Serif", Font.BOLD, 48));
		GridBagConstraints gbc_lblSigalMuseum = new GridBagConstraints();
		gbc_lblSigalMuseum.gridwidth = 2;
		gbc_lblSigalMuseum.insets = new Insets(0, 0, 5, 0);
		gbc_lblSigalMuseum.gridx = 0;
		gbc_lblSigalMuseum.gridy = 1;
		contentPane.add(lblSigalMuseum, gbc_lblSigalMuseum);

		puzzlePanel = new JPanel();
		puzzlePanel.setForeground(Color.WHITE);
		puzzlePanel.setBackground(Color.BLACK);
		GridBagConstraints gbc_puzzlePanel = new GridBagConstraints();
		gbc_puzzlePanel.gridwidth = 2;
		gbc_puzzlePanel.fill = GridBagConstraints.BOTH;
		gbc_puzzlePanel.gridx = 0;
		gbc_puzzlePanel.gridy = 2;
		contentPane.add(puzzlePanel, gbc_puzzlePanel);
		puzzlePanel.setLayout(null);
		setVisible(true);
	}

	public void nextPuzzle(BufferedImage img, BufferedImage template) {
		currentImage = img;
		currentTemplate = copyBufferedImage(template);
		puzzlePanel.removeAll();
		while (img.getHeight() > puzzlePanel.getHeight() || img.getWidth() > puzzlePanel.getWidth()) {
			if (img.getHeight() > puzzlePanel.getHeight()) {
				int h = puzzlePanel.getHeight();
				int w = h * img.getWidth() / img.getHeight();
				Image image = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
				img = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				img.getGraphics().drawImage(image, 0, 0, null);
			} else {
				int w = puzzlePanel.getWidth();
				int h = w * img.getHeight() / img.getWidth();
				Image image = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
				img = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
				img.getGraphics().drawImage(image, 0, 0, null);
			}
		}
		int xPieces, yPieces;
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
		Point[][] centers = new Point[xPieces][yPieces];

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

		piecePanels = new PicturePanel[xPieces][yPieces];
		for (int picX = 0; picX < pieces.length; picX++) {
			for (int picY = 0; picY < pieces[picX].length; picY++) {
				BufferedImage b = pieces[picX][picY];
				PicturePanel p = new PicturePanel(b);
				p.setCenter(centers[picX][picY]);
				p.setSize(b.getWidth(), b.getHeight());
				p.setMinimumSize(new Dimension(b.getWidth(), b.getHeight()));
				p.setMaximumSize(new Dimension(b.getWidth(), b.getHeight()));
				p.addMouseMotionListener(new MouseMotionListener() {

					@Override
					public void mouseMoved(MouseEvent arg0) {

					}

					@Override
					public void mouseDragged(MouseEvent e) {
						try {
							int x = (int) puzzlePanel.getMousePosition().getX();
							int y = (int) puzzlePanel.getMousePosition().getY();
							PicturePanel panel = (PicturePanel) e.getComponent();
							panel.setLoc(x - panel.getPicMouseX(), y - panel.getPicMouseY(), new ArrayList<PicturePanel>());
						} catch (NullPointerException e1) {
							Main.errMsg("Unable to get mouse position", false);
							Main.saveStackTrace(e1);
						}
					}
				});
				p.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {

					}

					@Override
					public void mouseEntered(MouseEvent arg0) {

					}

					@Override
					public void mouseExited(MouseEvent arg0) {

					}

					@Override
					public void mousePressed(MouseEvent e) {
						PicturePanel p = (PicturePanel) e.getComponent();
						p.setPicMouse(e.getX(), e.getY());
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						PicturePanel p = (PicturePanel) e.getSource();
						for(PicturePanel panel : p.getExtendedNeighbors(new ArrayList<PicturePanel>())) {
							checkNear(panel);
						}
						
					}
				});
				piecePanels[picX][picY] = p;
			}
		}

		// Random rand = new Random(1);
		int currentX = (int) piecePanels[0][0].getCenter().getX();
		int currentY = (int) piecePanels[0][0].getCenter().getY();
		for (int j = 0; j < xPieces; j++) {
			for (int k = 0; k < yPieces; k++) {
				PicturePanel p = piecePanels[j][k];
				p.setLocationWithCenter(currentX, currentY);
				if (k < yIntervals.length)
					currentY += yIntervals[k];
				puzzlePanel.add(p);
				repaint();
			}
			currentY = (int) piecePanels[j][0].getCenter().getY();
			if (j < xIntervals.length)
				currentX += xIntervals[j];
		}
		repaint();

	}

	public void checkNear(PicturePanel p) {
		int x = 0;
		int y = 0;
		for (int i = 0; i < piecePanels.length; i++)
			for (int j = 0; j < piecePanels[i].length; j++)
				if (p == piecePanels[i][j]) {
					x = i;
					y = j;
				}
		if (x > 0) {
			PicturePanel min1 = piecePanels[x - 1][y];
			if (min1.getCenter().getY() + min1.getY() < p.getCenter().getY() + p.getY() + 100 && min1.getCenter().getY() + min1.getY() > p.getCenter().getY() + p.getY() - 100 && min1.getCenter().getX() + min1.getX() < p.getCenter().getX() + p.getX() - xIntervals[x - 1] + 100 && min1.getCenter().getX() + min1.getX() > p.getCenter().getX() + p.getX() - xIntervals[x - 1] - 100) {
				p.setLocationWithCenter((int) (min1.getX() + min1.getCenter().getX() + xIntervals[x - 1]), (int) (min1.getY() + min1.getCenter().y));
				if (!p.isNeighbor(min1)) {
//					ArrayList<PicturePanel> neighborList = p.getNeighborList();
					p.addNeighbor(min1);
					min1.addNeighbor(p);
				}
			}
		}
		if (x < piecePanels.length - 1) {
			PicturePanel plus1 = piecePanels[x + 1][y];
			if (plus1.getCenter().y + plus1.getY() < p.getCenter().y + p.getY() + 100 && plus1.getCenter().y + plus1.getY() > p.getCenter().y + p.getY() - 100 && plus1.getCenter().x + plus1.getX() < p.getCenter().x + p.getX() + xIntervals[x] + 100 && plus1.getCenter().x + plus1.getX() > p.getCenter().x + p.getX() + xIntervals[x] - 100) {
				p.setLocationWithCenter((int) (plus1.getX() + plus1.getCenter().x - xIntervals[x]), (int) (plus1.getY() + plus1.getCenter().y));
				if (!p.isNeighbor(plus1)) {
					p.addNeighbor(plus1);
					plus1.addNeighbor(p);
				}
			}
		}
		if (y > 0) {
			PicturePanel min1 = piecePanels[x][y - 1];
			if (min1.getCenter().x + min1.getX() < p.getCenter().x + p.getX() + 100 && min1.getCenter().x + min1.getX() > p.getCenter().x + p.getX() - 100 && min1.getCenter().y + min1.getY() < p.getCenter().y + p.getY() - yIntervals[y - 1] + 100 && min1.getCenter().y + min1.getY() > p.getCenter().getY() - yIntervals[y - 1] - 100) {
				p.setLocationWithCenter((int) (min1.getX() + min1.getCenter().x), (int) (min1.getY() + min1.getCenter().y + yIntervals[y - 1]));
				if (!p.isNeighbor(min1)) {
					p.addNeighbor(min1);
					min1.addNeighbor(p);
				}
			}
		}
		if (y < piecePanels[0].length - 1) {
			PicturePanel plus1 = piecePanels[x][y + 1];
			if (plus1.getCenter().x + plus1.getX() < p.getCenter().x + p.getX() + 100 && plus1.getCenter().x + plus1.getX() > p.getCenter().x + p.getX() - 100 && plus1.getCenter().y + plus1.getY() < p.getCenter().y + p.getY() + yIntervals[y] + 100 && plus1.getCenter().y + plus1.getY() > p.getCenter().y + p.getY() + yIntervals[y] - 100) {
				p.setLocationWithCenter((int) (plus1.getX() + plus1.getCenter().x), (int) (plus1.getY() + plus1.getCenter().y - yIntervals[y]));
				if (!p.isNeighbor(plus1)) {
					p.addNeighbor(plus1);
					plus1.addNeighbor(p);
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

	public static BufferedImage copyBufferedImage(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public BufferedImage getPiece(int x, int y, BufferedImage template, BufferedImage origImg) {
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

	public ArrayList<Integer> fill(Image img, int xSeed, int ySeed, Color col) {
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

	public boolean exit() {
		return exit;
	}

	public void gameOver() {
		setVisible(false);
		dispose();
		exit = true;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}
