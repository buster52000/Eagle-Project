package com.project.wordScramble;

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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import com.project.base.BaseUI;
import com.project.base.BaseUtils;
import com.project.base.FutureAction;
import com.project.base.GameController;
import com.project.base.Main;

public class WordScrambleBase {

	private ScrambleUI ui;
	private List<Scramble> scrambles;
	private FutureAction endGameTimer, hintTimer;
	private Random rand;
	private Scramble currentScramble, nextScramble;
	private BufferedImage currentImage, nextImage;
	private boolean scrambleLoaded, nextScramblePreped, noMoreScrambles;

	private static final int HINT_WAIT_TIME = 15000;

	@SuppressWarnings("serial")
	public WordScrambleBase() {
		scrambles = loadScrambles("/gameFiles/scrambles/list.txt");
		ui = new ScrambleUI() {

			@Override
			public void complete() {
				scrambleLoaded = false;
				ui.displayCorrect();

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						BaseUtils.showDescriptionDialog(currentScramble.getDescription(), currentImage, currentScramble.getWord());
						nextScramble();
					}
				});
			}

			@Override
			public void clickCaptured() {
				hintTimer.startOrRestartCountdown(HINT_WAIT_TIME);
			}

		};

		hintTimer = new FutureAction("ScrambleHint") {

			@Override
			public void performAction() {
				ui.hint();
				hintTimer.startOrRestartCountdown(HINT_WAIT_TIME);
			}

			@Override
			public void actionCancelled() {

			}
		};

		rand = new Random();
		currentScramble = null;
		currentImage = null;

		endGameTimer = new FutureAction("ScrambleInactivity") {

			@Override
			public void performAction() {
				ui.gameOver();
				Main.infoMsg("Word scramble game timed out");
			}

			@Override
			public void actionCancelled() {

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
	}

	public void preloadFirst() {
		scrambleLoaded = false;
		noMoreScrambles = false;
		nextScramblePreped = false;
		prepNextScramble();
	}
	
	public void playGame() {
		if(!ui.isPreped())
			preloadFirst();
		nextScramble();
		endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
		hintTimer.startOrRestartCountdown(HINT_WAIT_TIME);
		synchronized (ui) {
			while (!ui.exit()) {
				try {
					ui.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		endGameTimer.cancel();
		hintTimer.cancel();
	}

	private void prepNextScramble() {
		if (scrambles.size() > 0) {
			if (nextScramblePreped)
				Main.errMsg("Scramble already preped proceeding anyway", false);
			int i = rand.nextInt(scrambles.size());
			nextScramble = scrambles.get(i);
			nextImage = BaseUtils.loadImage(nextScramble.getPictureUrl(), BaseUI.PIC_WIDTH);
			scrambles.remove(i);
			ui.prepScramble(nextScramble, nextImage);
			nextScramblePreped = true;
		} else {
			noMoreScrambles = true;
		}
	}

	private void nextScramble() {
		if(!nextScramblePreped && !noMoreScrambles)
			prepNextScramble();
		if(noMoreScrambles) {
			ui.gameOver();
			return;
		}
		if (scrambleLoaded)
			Main.errMsg("Scramble already loaded proceeding anyway", false);
		currentScramble = nextScramble;
		currentImage = nextImage;
		scrambleLoaded = true;
		if (!ui.isPreped())
			ui.prepScramble(currentScramble, currentImage);
		ui.nextScramble();
		prepNextScramble();
	}

	public static List<Scramble> loadScrambles(String scrambleListResource) {
		List<Scramble> scrambles = new ArrayList<Scramble>();

		// load any that are part of the resources
		InputStream scrambleList = WordScrambleBase.class.getResourceAsStream(scrambleListResource);
		if (scrambleList == null) {
			Main.errMsg("Couldn't open the scramble list as a resource", false);
			return scrambles; // even though it's empty!
		}
		BufferedReader breader = new BufferedReader(new InputStreamReader(scrambleList));
		String line;
		try {
			while ((line = breader.readLine()) != null) {
				String scrambleName = line;
				boolean stat = loadScrambleDescriptor(scrambles, WordScrambleBase.class.getResourceAsStream(scrambleName));
				if (!stat) {
					Main.errMsg("Failed to load scramble descriptor: " + scrambleName, false);
				}
			}
		} catch (IOException e) {
			// report problem, but continue
			Main.errMsg("Error while loading the scramble list.  The set of available scrambles may not be complete", false);
		} finally {
			try {
				breader.close();
				scrambleList.close();
			} catch (IOException e) {
				// not going to worry about this case
			}
		}

		// load any extras at user.home
		File dir = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "gameFiles/scrambles/");
		if (!dir.exists()) {
			dir.mkdirs();
			return scrambles;
		}
		File[] files = dir.listFiles();
		for (File f : files) {
			boolean stat = loadScrambleDescriptor(scrambles, f);
			if (!stat) {
				Main.errMsg("Failed to load scramble descriptor: " + f.getAbsolutePath(), false);
			}
		}

		return scrambles;
	}

	private static boolean loadScrambleDescriptor(List<Scramble> scrambles, InputStream scrambleMetadataStream) {
		BufferedReader read = null;
		try {
			read = new BufferedReader(new InputStreamReader(scrambleMetadataStream));
			String word = read.readLine();
			String text = read.readLine();
			String picLoc = read.readLine();
			String description = read.readLine();

			if (word.equals("") || text.equals("") || picLoc.equals("") || description.equals("")) {
				Main.errMsg("Scramble File not read properly", false);
				return false;
			} else {
				URL url = WordScrambleBase.class.getResource(picLoc);
				Main.infoMsg("Registering scramble: " + picLoc);
				scrambles.add(new Scramble(word, text, url, description));
				return true;
			}
		} catch (FileNotFoundException e) {
			Main.errMsg("File named in scramble file list not found", false);
			Main.saveStackTrace(e);
			return false;
		} catch (IOException e) {
			Main.errMsg("IOException unable to read scramble descriptor file", false);
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

	private static boolean loadScrambleDescriptor(List<Scramble> scrambles, File f) {
		BufferedReader read = null;
		try {
			read = new BufferedReader(new FileReader(f));
			String word = read.readLine();
			String text = read.readLine();
			String picLoc = read.readLine();
			String description = read.readLine();

			if (word.equals("") || text.equals("") || picLoc.equals("") || description.equals("")) {
				Main.errMsg("Scramble File not read properly", false);
				return false;
			} else {
				File f2 = new File(picLoc);
				if (f2.exists()) {
					Main.infoMsg("Registering scramble: " + f2.getAbsolutePath());
					scrambles.add(new Scramble(word, text, f2.toURI().toURL(), description));
					return true;
				} else {
					Main.errMsg("Scramble File not found: " + f2.getAbsolutePath(), false);
					return false;
				}
			}
		} catch (FileNotFoundException e) {
			Main.errMsg("File named in scramble file list not found", false);
			Main.saveStackTrace(e);
			return false;
		} catch (IOException e) {
			Main.errMsg("IOException unable to read scramble descriptor file", false);
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

	public static String scrambleWord(String word) {
		Main.infoMsg("scrambleWord called with word: " + word);
		char[] arr = word.toCharArray();
		ArrayList<Character> list = new ArrayList<Character>();
		for (char c : arr)
			list.add(c);
		Collections.shuffle(list);
		word = "";
		for (char c : list)
			word = word + c;
		Main.infoMsg("scrambleWord complete with scramble: " + word);
		return word;
	}

}
