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

public class GameController implements MouseMotionListener {

	public static final int ACTIVATE_SS_AfterMilliseconds = 180000;
	public static final int END_GAME_AFTER_MILLI = 120000;
	
	private BaseUI baseUI;
	private FutureAction ssController;
	private boolean screensaverActivated;
	private boolean inGame;
	private boolean noFatalErr;

	public GameController() {
		baseUI = new BaseUI();
		inGame = false;
		noFatalErr = true;
		ssController = new FutureAction() {
			@Override
			public void performAction() {
				// will be called when the timeout triggers the screensaver
				screensaverActivated = true;
				baseUI.showScreenSaver();
			}

			@Override
			public void actionCancelled() {
				// not much to do...  something else will be replacing the screensaver, so no need to do anything to remove it
				screensaverActivated = false;
			}
		};
		ArrayList<JButton> buttons = baseUI.getJButtons();
		baseUI.getJFrame().addMouseMotionListener(this);
		buttons.get(0).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!screensaverActivated && !inGame) {
					startGame(1);
				} else if (inGame)
					Main.errMsg("Game select buttons pressed while in game", false);
			}
		});
		buttons.get(1).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!screensaverActivated && !inGame) {
					startGame(2);
				} else if (inGame)
					Main.errMsg("Game select buttons pressed while in game", false);
			}
		});
		buttons.get(2).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!screensaverActivated && !inGame) {
					startGame(3);
				} else if (inGame)
					Main.errMsg("Game select buttons pressed while in game", false);
			}
		});
	}

	public boolean start() {
		baseUI.showMenu();
		(new Thread(baseUI)).start();
		ssController.startOrRestartCountdown(ACTIVATE_SS_AfterMilliseconds);
		
		while (true) {
			// this loop will only do something interesting if the following sleep is ever interrupted.  Therefore, 
			// it doesn't matter how long I make it, so, since I don't want the processor doing much to support this loop, make it long...
			try {
				Thread.sleep(1000000); // sleep for a long time... 
			} catch (InterruptedException e) {
				Main.errMsg("Thread sleep interrupted", true);
				Main.saveStackTrace(e);
				return false;
			}
			if (!noFatalErr) {
				return false;
			}
		}
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (screensaverActivated) {
			ssController.cancel();
			baseUI.showMenu();
		}
		if (!inGame)
			ssController.startOrRestartCountdown(ACTIVATE_SS_AfterMilliseconds);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (screensaverActivated) {
			ssController.cancel();
			baseUI.showMenu();
		}
		if (!inGame)
			ssController.startOrRestartCountdown(ACTIVATE_SS_AfterMilliseconds);
	}

	private void startGame(int i) {
		inGame = true;
		ssController.cancel();
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
						ssController.startOrRestartCountdown(ACTIVATE_SS_AfterMilliseconds);
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
						ssController.startOrRestartCountdown(ACTIVATE_SS_AfterMilliseconds);
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
						ssController.startOrRestartCountdown(ACTIVATE_SS_AfterMilliseconds);
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
		ssController.startOrRestartCountdown(ACTIVATE_SS_AfterMilliseconds);
	}
}
