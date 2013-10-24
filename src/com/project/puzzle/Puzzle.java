package com.project.puzzle;

import java.net.URL;

public class Puzzle {

	private URL imageUrl;
	private String description, name;
	
	public Puzzle(URL imageUrl, String description, String name) {
		this.imageUrl = imageUrl;
		this.description = description;
		this.name = name;
	}
	
	public URL getImageUrl() {
		return imageUrl;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getName() {
		return name;
	}
	
}
