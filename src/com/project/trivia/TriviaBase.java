package com.project.trivia;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.project.base.FutureAction;
import com.project.base.GameController;
import com.project.base.Main;

public class TriviaBase {

	private ArrayList<Trivia> trivia = new ArrayList<Trivia>();
	private Trivia currentTrivia;
	private boolean triviaLoaded;
	private TriviaUI ui;
	private Timer timer;
	private FutureAction endGameTimer;

	public TriviaBase() {
		currentTrivia = null;
		triviaLoaded = false;
		ui = new TriviaUI();
		timer = new Timer();

		endGameTimer = new FutureAction() {

			@Override
			public void performAction() {
				ui.gameOver();
				Main.infoMsg("Trivia Game timed out");
			}

			@Override
			public void actionCancelled() {
				ui.gameOver();
				Main.errMsg("Trivia end game timer canceled", false);
			}
		};

		ui.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
			}
		});

		ui.getSubmitButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (triviaLoaded) {
					if (currentTrivia != null) {
						if (ui.getSelected().equals(currentTrivia.getAnswers()[3])) {
							timer.cancel();
							ui.displayCorrect();
							triviaLoaded = false;
							ImageIcon ico = new ImageIcon();
							ico.setImage(currentTrivia.getPic());
							showDescriptionDialog(currentTrivia.getDescription(), ico, currentTrivia.getAnswers()[3]);
							nextTrivia();
						} else {
							ui.displayWrong();
						}
					} else {
						Main.errMsg("triviaLoaded is true but currentTrivia == null", false);
						timer.cancel();
						ui.gameOver();
					}
				} else {
					Main.errMsg("Submit Button Pressed when !triviaLoaded", false);
				}
			}
		});
		ui.getMenuButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				timer.cancel();
				triviaLoaded = false;
				currentTrivia = null;
				ui.gameOver();
			}
		});
	}

	public void playGame(ArrayList<Trivia> trivia) {
		this.trivia = trivia;
		endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
		nextTrivia();
		synchronized (ui) {
			while (!ui.exit()) {
				try {
					ui.wait();
				} catch (InterruptedException e) {
					Main.saveStackTrace(e);
				}
			}
		}
		endGameTimer.cancel();
	}

	private void showDescriptionDialog(String str, ImageIcon ico, String title) {
		if (ico.getIconHeight() > 500 || ico.getIconWidth() > 500) {
			int h = 1;
			int w = 1;
			if (ico.getIconHeight() < ico.getIconWidth()) {
				h = 500;
				w = (h * ico.getIconWidth()) / ico.getIconWidth();
			} else {
				w = 500;
				h = (w * ico.getIconHeight()) / ico.getIconWidth();
			}
			if (h == 1 || w == 1) {
				Main.errMsg("Unable to resize image - com.project.trivia.TriviaBase.showDescriptionDialog()", false);
			} else {
				Image img = ico.getImage().getScaledInstance(h, w, Image.SCALE_FAST);
				ico = new ImageIcon(img);
			}
		}
		ArrayList<String> descriptionLines = new ArrayList<String>();
		int currentCharNum = 0;
		int lastCharNum = 0;
		while (currentCharNum < str.length()) {
			lastCharNum = currentCharNum;
			currentCharNum += 90;
			if (currentCharNum > str.length()) {
				descriptionLines.add(str.substring(lastCharNum));
			} else {
				while (str.charAt(currentCharNum) != ' ')
					currentCharNum -= 1;
				descriptionLines.add(str.substring(lastCharNum, currentCharNum));
			}
		}
		String formatted = " ";
		for (int i = 0; i < descriptionLines.size(); i++) {
			formatted += descriptionLines.get(i) + (i + 1 != descriptionLines.size() ? "\n" : "");
		}
		UIManager.put("OptionPane.background", Color.BLACK);
		UIManager.put("OptionPane.messageForeground", Color.WHITE);
		UIManager.put("Panel.background", Color.BLACK);
		UIManager.put("OptionPane.messageFont", new Font("Serif", Font.PLAIN, 30));
		JOptionPane.showOptionDialog(null, formatted, title, JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, ico, new String[] { "Next" }, "Next");
	}

	private void nextTrivia() {
		timer.cancel();
		timer = new Timer();
		if (trivia.size() > 0) {
			if (triviaLoaded) {
				Main.errMsg("nextTrivia Called when triviaLoaded", false);
				ui.gameOver();
			} else {
				Random rand = new Random();
				int i = rand.nextInt(trivia.size());
				currentTrivia = trivia.get(i);
				trivia.remove(i);
				triviaLoaded = true;
				ui.nextTrivia(currentTrivia);
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						ui.hideNext();
					}
				}, 15000);
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						ui.hideNext();
					}
				}, 25000);
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						ui.hideNext();
						ui.displayWrong();
					}
				}, 45000);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						triviaLoaded = false;
						showDescriptionDialog(currentTrivia.getDescription(), new ImageIcon(currentTrivia.getPic()), currentTrivia.getAnswers()[3]);
						nextTrivia();
					}
				}, 50000);
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ui.gameOver();
				}
			});
		}
	}

	public static ArrayList<Trivia> loadTrivia() {
		File dir = new File("gameFiles/trivia/");
		ArrayList<Trivia> triv = new ArrayList<Trivia>();
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (File f : files) {
				String question = "";
				String picLoc = "";
				String description = "";
				String[] answers = new String[4];
				Image img = null;
				try {
					BufferedReader read = new BufferedReader(new FileReader(f));
					question = read.readLine();
					picLoc = read.readLine();
					answers[0] = read.readLine();
					answers[1] = read.readLine();
					answers[2] = read.readLine();
					answers[3] = read.readLine();
					description = read.readLine();
					read.close();
					img = ImageIO.read(new File(picLoc));
				} catch (FileNotFoundException e) {
					Main.errMsg("Unable to load trivia file not found", false);
					Main.saveStackTrace(e);
				} catch (IOException e) {
					Main.errMsg("File with improper format found when loading trivia files", false);
					Main.saveStackTrace(e);
				}
				triv.add(new Trivia(img, question, answers, description));
			}
		} else {
			dir.mkdirs();
		}
		return triv;
	}

}
