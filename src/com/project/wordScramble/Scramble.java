package com.project.wordScramble;

import java.net.URL;

public class Scramble {

	private String word, text, description;
	private URL picLoc;
	
	public Scramble(String word, String text, URL picLoc, String description) {
		this.word = word;
		this.text = text;
		this.picLoc = picLoc;
		this.description = description;
	}
	
	public URL getPictureUrl() {
		return picLoc;
	}
	
	public String getText() {
		return text;
	}
	
	public String getWord() {
		return word;
	}
	
	public String getDescription() {
		return description;
	}
	
}
