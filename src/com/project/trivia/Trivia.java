package com.project.trivia;

import java.net.URL;

public class Trivia {

	private String question, description;
	private String [] answers;
	private URL picUrl;
	
	public Trivia(URL picUrl, String question, String [] answers, String description) {
		this.question = question;
		this.answers = answers;
		this.picUrl = picUrl;
		this.description = description;
	}
	
	public String [] getAnswers() {
		return answers;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public URL getPicUrl() {
		return picUrl;
	}
	
	public String getDescription() {
		return description;
	}
	
}
