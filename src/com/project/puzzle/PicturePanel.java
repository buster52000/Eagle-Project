package com.project.puzzle;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PicturePanel extends JPanel {

	private Image img;
	private int pMouseX, pMouseY, pieceX, pieceY;
	private Set<PicturePanel> neighbors;
	private Point center;

	public PicturePanel(BufferedImage image, int pieceX, int pieceY) {
		this.pieceX = pieceX;
		this.pieceY = pieceY;
		center = new Point(0, 0);
		img = image;
		neighbors = new HashSet<PicturePanel>();
		setOpaque(false);
	}

	public void setLocationWithCenter(int x, int y) {
		setLoc((int) (x - center.getX()), (int) (y - center.getY()), new ArrayList<PicturePanel>());
	}

	public void setCenter(Point p) {
		center = p;
	}

	public Point getCenter() {
		return center;
	}

	public void setLoc(int x, int y, ArrayList<PicturePanel> list) {
		setLoc(new Point(x, y), list);
	}
	
	public void setLoc(Point p, ArrayList<PicturePanel> used) {
		if (p.equals(getLocation()))
			return;
		boolean contained = false;
		for (PicturePanel pic : used) {
			if (this == pic) {
				contained = true;
			}
		}
		if (contained)
			return;
		else
			used.add(this);
		for (PicturePanel panel : neighbors) {
			int pX = panel.getX();
			int pY = panel.getY();
			int rX = pX - getX();
			int rY = pY - getY();
			int x = (int) p.getX() + rX;
			int y = (int) p.getY() + rY;
			panel.setLoc(new Point(x, y), used);
		}
		super.setLocation(p);
	}

	public boolean addNeighbor(PicturePanel p) {
		return neighbors.add(p);
	}

	public boolean isNeighbor(PicturePanel p) {
		return neighbors.contains(p);
	}
	
	public Set<PicturePanel> getExtendedNeighbors(Set<PicturePanel> extended) {
		if(extended.contains(this)) {
			return extended;
		}
		extended.add(this);
		for(PicturePanel p : neighbors) {
			p.getExtendedNeighbors(extended);
		}
		return extended;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, this);
	}

	public void setPicMouse(int x, int y) {
		pMouseX = x;
		pMouseY = y;
	}

	public int getPicMouseX() {
		return pMouseX;
	}

	public int getPicMouseY() {
		return pMouseY;
	}

	public int getPieceX() {return pieceX;}
	public int getPieceY() {return pieceY;}
	
	public String toString() {
		return Integer.toString(pieceX)+"|"+Integer.toString(pieceY);
	}
}
