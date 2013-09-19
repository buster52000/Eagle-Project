package com.project.trivia;

import java.awt.Image;

public class Trivia {

	private String question, description;
	private String [] answers;
	private Image pic;
	
	public Trivia(Image pic, String question, String [] answers, String description) {
		this.question = question;
		this.answers = answers;
		this.pic = pic;
		this.description = description;
	}
	
	public String [] getAnswers() {
		return answers;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public Image getPic() {
		return pic;
	}
	
	public String getDescription() {
		return description;
	}
	
}
