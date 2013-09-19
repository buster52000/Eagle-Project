package com.project.puzzle;

import java.awt.image.BufferedImage;

public class Puzzle {

	private BufferedImage img;
	private String description, name;
	
	public Puzzle(BufferedImage img, String description, String name) {
		this.img = img;
		this.description = description;
		this.name = name;
	}
	
	public BufferedImage getImage() {
		return img;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getName() {
		return name;
	}
	
}
