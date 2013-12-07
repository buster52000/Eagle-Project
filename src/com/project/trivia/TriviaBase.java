package com.project.trivia;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import com.project.base.BaseUI;
import com.project.base.BaseUtils;
import com.project.base.FutureAction;
import com.project.base.GameController;
import com.project.base.Main;

public class TriviaBase {

	private List<Trivia> trivia;
	private Trivia currentTrivia, nextTrivia;
	private BufferedImage currentImage, nextImage;
	private boolean triviaLoaded, nextTriviaPreped, noMoreTrivia;
	private TriviaUI ui;
	private Timer timer;
	private FutureAction endGameTimer;

	public TriviaBase() {
		trivia = loadTrivia("/gameFiles/trivia/list.txt");
		currentTrivia = null;
		currentImage = null;
		triviaLoaded = false;
		nextTriviaPreped = false;
		noMoreTrivia = false;
		ui = new TriviaUI();
		timer = new Timer();

		endGameTimer = new FutureAction("TriviaInactivity") {

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
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									BaseUtils.showDescriptionDialog(currentTrivia.getDescription(), currentImage, currentTrivia.getAnswers()[3]);
									nextTrivia();
								}
							});
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
				currentImage = null;
				ui.gameOver();
			}
		});
	}

	public void preloadFirst() {
		triviaLoaded = false;
		nextTriviaPreped = false;
		noMoreTrivia = false;
		prepTrivia();
	}

	public void playGame() {
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

	private void prepTrivia() {
		if (nextTriviaPreped)
			Main.infoMsg("prepTriva called when trivia is already preped");
		if (trivia.size() > 0) {
			Random rand = new Random();
			int i = rand.nextInt(trivia.size());
			nextTrivia = trivia.get(i);
			nextImage = BaseUtils.loadImage(nextTrivia.getPicUrl(), BaseUI.PIC_WIDTH);
			trivia.remove(i);
			ui.prepNextTrivia(nextTrivia, nextImage);
			nextTriviaPreped = true;
		} else
			noMoreTrivia = true;
	}

	private void nextTrivia() {

		timer.cancel();
		timer = new Timer();
		if (!nextTriviaPreped && !noMoreTrivia)
			prepTrivia();
		if (!noMoreTrivia) {
			if (triviaLoaded)
				Main.errMsg("nextTrivia Called when triviaLoaded", false);
			currentTrivia = nextTrivia;
			currentImage = nextImage;
			// Random rand = new Random();
			// int i = rand.nextInt(trivia.size());
			// currentTrivia = trivia.get(i);
			// currentImage = BaseUtils.loadImage(currentTrivia.getPicUrl(),
			// BaseUI.PIC_WIDTH);
			// trivia.remove(i);
			// ui.nextTrivia(currentTrivia, currentImage);
			ui.nextTrivia();
			prepTrivia();
			triviaLoaded = true;
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
					BaseUtils.showDescriptionDialog(currentTrivia.getDescription(), currentImage, currentTrivia.getAnswers()[3]);
					nextTrivia();
				}
			}, 50000);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ui.gameOver();
				}
			});
		}
	}

	public static List<Trivia> loadTrivia(String triviaListResource) {
		List<Trivia> trivia = new ArrayList<Trivia>();

		// load any that are part of the resources
		InputStream triviaList = TriviaBase.class.getResourceAsStream(triviaListResource);
		if (triviaList == null) {
			Main.errMsg("Couldn't open the trivia list as a resource", false);
			return trivia; // even though it's empty!
		}

		BufferedReader breader = new BufferedReader(new InputStreamReader(triviaList));
		String line;
		try {
			while ((line = breader.readLine()) != null) {
				String triviaName = line;
				boolean stat = loadTriviaDescriptor(trivia, TriviaBase.class.getResourceAsStream(triviaName));
				if (!stat) {
					Main.errMsg("Failed to load trivia descriptor: " + triviaName, false);
				}
			}
		} catch (IOException e) {
			// report problem, but continue
			Main.errMsg("Error while loading the trivia list.  The set of available trivia may not be complete", false);
		} finally {
			try {
				breader.close();
				triviaList.close();
			} catch (IOException e) {
				// not going to worry about this case
			}
		}

		// load any extras at user.home
		File dir = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "gameFiles/trivia/");
		if (!dir.exists()) {
			dir.mkdirs();
			return trivia;
		}
		File[] files = dir.listFiles();
		for (File f : files) {
			boolean stat = loadTriviaDescriptor(trivia, f);
			if (!stat) {
				Main.errMsg("Failed to load trivia descriptor: " + f.getAbsolutePath(), false);
			}
		}

		return trivia;
	}

	private static boolean loadTriviaDescriptor(List<Trivia> trivia, InputStream triviaMetadataStream) {
		BufferedReader read = null;
		try {
			read = new BufferedReader(new InputStreamReader(triviaMetadataStream));
			String question = read.readLine();
			String picLoc = read.readLine();
			String[] answers = new String[4];
			answers[0] = read.readLine();
			answers[1] = read.readLine();
			answers[2] = read.readLine();
			answers[3] = read.readLine();
			String description = read.readLine();
			URL url = TriviaBase.class.getResource(picLoc);
			Main.infoMsg("Registering trivia: " + picLoc);
			trivia.add(new Trivia(url, question, answers, description));
			return true;
		} catch (FileNotFoundException e) {
			Main.errMsg("File named in trivia list not found", false);
			Main.saveStackTrace(e);
			return false;
		} catch (IOException e) {
			Main.errMsg("IOException unable to read trivia descriptor file", false);
			Main.saveStackTrace(e);
			return false;
		} finally {
			try {
				if (read != null)
					read.close();
			} catch (IOException e) {
				// not much to do in this case, but probably doesn't matter
				// either
				e.printStackTrace();
			}
		}
	}

	private static boolean loadTriviaDescriptor(List<Trivia> trivia, File f) {
		BufferedReader read = null;
		try {
			read = new BufferedReader(new FileReader(f));
			String question = read.readLine();
			String picLoc = read.readLine();
			String[] answers = new String[4];
			answers[0] = read.readLine();
			answers[1] = read.readLine();
			answers[2] = read.readLine();
			answers[3] = read.readLine();
			String description = read.readLine();

			File f2 = new File(picLoc);
			if (f2.exists()) {
				Main.infoMsg("Registering trivia: " + f2.getAbsolutePath());
				trivia.add(new Trivia(f2.toURI().toURL(), question, answers, description));
				return true;
			} else {
				Main.errMsg("Trivia File not found: " + f2.getAbsolutePath(), false);
				return false;
			}
		} catch (FileNotFoundException e) {
			Main.errMsg("Unable to load trivia file not found", false);
			Main.saveStackTrace(e);
			return false;
		} catch (IOException e) {
			Main.errMsg("File with improper format found when loading trivia files", false);
			Main.saveStackTrace(e);
			return false;
		} finally {
			try {
				if (read != null)
					read.close();
			} catch (IOException e) {
				// not much to do in this case, but probably doesn't matter
				// either
				e.printStackTrace();
			}
		}
	}

}
