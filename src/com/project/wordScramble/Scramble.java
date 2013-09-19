package com.project.wordScramble;

import java.awt.Image;

public class Scramble {

	private String word, text, description;
	private Image pic;
	
	public Scramble(String word, String text, Image pic, String description) {
		this.word = word;
		this.text = text;
		this.pic = pic;
		this.description = description;
	}
	
	public Image getPicture() {
		return pic;
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
