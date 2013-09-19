package com.project.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JButton;

import com.project.puzzle.PuzzleBase;
import com.project.trivia.TriviaBase;
import com.project.wordScramble.WordScrambleBase;

public class OldGameController implements MouseMotionListener {

	private BaseUI baseUI;
	private boolean screenSaverActivated;
	private DepriciatedTimer ssTimer;
	private boolean inGame;
	private boolean noFatalErr;
	public static final int ACTIVATE_SS_After = 3;

	public OldGameController() {
		baseUI = new BaseUI();
		screenSaverActivated = false;
		inGame = false;
		noFatalErr = true;
		ssTimer = new DepriciatedTimer();
		ArrayList<JButton> buttons = baseUI.getJButtons();
		baseUI.getJFrame().addMouseMotionListener(this);
		buttons.get(0).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!screenSaverActivated && !inGame) {
					startGame(1);
				} else if (inGame)
					Main.errMsg("Game select buttons pressed while in game", false);
			}
		});
		buttons.get(1).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!screenSaverActivated && !inGame) {
					startGame(2);
				} else if (inGame)
					Main.errMsg("Game select buttons pressed while in game", false);
			}
		});
		buttons.get(2).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!screenSaverActivated && !inGame) {
					startGame(3);
				} else if (inGame)
					Main.errMsg("Game select buttons pressed while in game", false);
			}
		});
	}

	public boolean start() {
		baseUI.showMenu();
		(new Thread(baseUI)).start();
		ssTimer.startTimer();
		String pass = "";
		while (!pass.equals("stop")) {
			if (!screenSaverActivated && !inGame) {
				if (ssTimer.getTime() >= ACTIVATE_SS_After) {
					baseUI.showScreenSaver();
					ssTimer.stopTimer();
					screenSaverActivated = true;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Main.errMsg("Thread sleep interrupted", true);
				Main.saveStackTrace(e);
				return false;
			}
			if (!noFatalErr) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (screenSaverActivated) {
			baseUI.showMenu();
			screenSaverActivated = false;
		}
		if (!inGame)
			ssTimer.startTimer();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (screenSaverActivated) {
			baseUI.showMenu();
			screenSaverActivated = false;
		}
		if (!inGame)
			ssTimer.startTimer();
	}

	private void startGame(int i) {
		inGame = true;
		ssTimer.stopTimer();
		switch (i) {
		case 1:
			Main.infoMsg("Started new Word Scramble Game");
			Thread game1 = new Thread() {
				public void run() {
					try {
						WordScrambleBase word = new WordScrambleBase();
						word.playGame();
						inGame = false;
						baseUI.requestFocus();
						Main.infoMsg("Word Scramble Game Completed");
						ssTimer.startTimer();
					} catch (Throwable e) {
						Main.saveStackTrace(e);
					}
				}
			};
			game1.start();
			break;
		case 2:
			Main.infoMsg("Started new Trivia Game");
			Thread game2 = new Thread() {
				public void run() {
					try {
						TriviaBase trivia = new TriviaBase();
						trivia.playGame();
						inGame = false;
						baseUI.requestFocus();
						Main.infoMsg("Trivia Game Completed");
						ssTimer.startTimer();
					} catch (Throwable e) {
						Main.saveStackTrace(e);
					}
				}
			};
			game2.start();
			break;
		case 3:
			Main.infoMsg("Started new Puzzle Game");
			Thread game3 = new Thread() {
				public void run() {
					try {
						PuzzleBase puzzle = new PuzzleBase();
						puzzle.playGame();
						inGame = false;
						baseUI.requestFocus();
						Main.infoMsg("Puzzle Game Completed");
						ssTimer.startTimer();
					} catch (Throwable e) {
						Main.saveStackTrace(e);
					}
				}
			};
			game3.start();
			break;
		default:
			Main.errMsg("Unknown game type selected.", false);
			inGame = false;
		}
		ssTimer.startTimer();
	}
}
