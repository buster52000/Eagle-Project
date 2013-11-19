package com.project.puzzle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.project.base.BaseUtils;
import com.project.base.FutureAction;
import com.project.base.GameController;
import com.project.base.Main;

public class PuzzleBase {

	private PuzzleUI ui;
	private List<Puzzle> puzzles, allPuzzles;
	private Puzzle currentPuzzle;
	private PuzzleModel currentPuzzleModel;
	private List<URL> templateFilenames, allTemplateFilenames;
	private FutureAction endGameTimer;

	// be very careful with these! They are being accessed from 2 threads and
	// careful synchronization is needed to avoid deadlocks or race conditions
	private PuzzleModel nextPuzzleModel;
	private Puzzle nextPuzzle;
	private AtomicBoolean isLoading = new AtomicBoolean(); // this is used for
															// synchronization
															// -- don't use the
															// others while this
															// is true
	private Thread loadingThread = null;

	public PuzzleBase() {
		reset();
	}

	@SuppressWarnings("serial")
	public void reset() {
		if (ui == null) {
			ui = new PuzzleUI() {

				@Override
				public void complete() {
					displayCorrect();
					BaseUtils.showDescriptionDialog(currentPuzzle.getDescription(), currentPuzzleModel.getOriginalImage(), currentPuzzle.getName());
					boolean isNextAvailable = getPuzzleAndStartPreloadingNext();
					if (!isNextAvailable)
						gameOver();
				}

			};

			endGameTimer = new FutureAction() {

				@Override
				public void performAction() {
					ui.gameOver();
					Main.infoMsg("Puzzle Game Timed out");
				}

				@Override
				public void actionCancelled() {
					ui.gameOver();
					Main.errMsg("Puzzle end game timer canceled", false);
				}
			};

			ui.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseDragged(MouseEvent arg0) {
					endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
				}

				@Override
				public void mouseMoved(MouseEvent arg0) {
					endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
				}
			});

			allPuzzles = loadPuzzles("/gameFiles/puzzles/list.txt");
			allTemplateFilenames = loadTemplateNames("/gameFiles/puzzleTemplates/list.txt");
		} else {
			ui.restartGame();
		}
		puzzles = new ArrayList<Puzzle>(allPuzzles);
		templateFilenames = new ArrayList<URL>(allTemplateFilenames);
		preloadNextPuzzle();
	}

	private boolean getPuzzleAndStartPreloadingNext() {
		currentPuzzle = null;
		currentPuzzleModel = null;
		synchronized (isLoading) {
			if (isLoading.get())
				try {
					isLoading.wait();
				} catch (InterruptedException e) {
					// shouldn't happen
				}
			currentPuzzle = nextPuzzle;
			currentPuzzleModel = nextPuzzleModel;
		}
		if (currentPuzzle != null) {
			puzzles.remove(currentPuzzle);
			ui.setModel(currentPuzzleModel, currentPuzzle.getName());
			ui.newPuzzle();
			preloadNextPuzzle();
			return true;
		} else {
			return false;
		}
	}

	public void playGame() {
		endGameTimer.startOrRestartCountdown(GameController.END_GAME_AFTER_MILLI);
		getPuzzleAndStartPreloadingNext();
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
		ui.gameOver();
	}

	private void preloadNextPuzzle() {
		if (puzzles.size() > 0) {
			// it's possible (though unlikely) that we're already loading
			// something...
			// to avoid any confusion, let's wait for that to finish, then
			// ignore it's results, before proceeding
			synchronized (isLoading) {
				if (loadingThread != null && loadingThread.isAlive())
					if (isLoading.get()) {
						loadingThread.setPriority(Thread.MAX_PRIORITY); // raise
																		// it
																		// 'cause
																		// we're
																		// waiting
																		// for
																		// it
						loadingThread.interrupt(); // try killing it to make
													// sure we don't get stuck
													// in case it's stuck --
													// either way we're going to
													// ignore it's results
						try {
							isLoading.wait();
						} catch (InterruptedException e) {/* shouldn't happen */
						}
					}
			}

			Random rand = Main.test ? new Random(1) : new Random(); // for
																	// testing,
																	// it's much
																	// easier to
																	// always
																	// see the
																	// same
																	// sequence

			final Puzzle nextPuzzle = puzzles.get(rand.nextInt(puzzles.size()));
			final URL nextImageUrl = nextPuzzle.getImageUrl();
			final URL nextTemplateUrl = templateFilenames.get(rand.nextInt(templateFilenames.size()));
			final String nextPuzzleName = nextPuzzle.getName();
			loadingThread = new Thread() {
				public void run() {
					Main.infoMsg("Preloading next puzzle model: " + nextPuzzleName);
					PuzzleModel nextPuzzleModel = loadPuzzle(nextImageUrl, nextTemplateUrl, nextPuzzleName);
					setNextPuzzle(nextPuzzle, nextPuzzleModel);
				}
			};
			synchronized (isLoading) {
				isLoading.set(true);
				isLoading.notify();
			}
			loadingThread.setDaemon(true); // don't want to prevent the program
											// from closing if this thread is
											// still running
			loadingThread.setPriority(Thread.MIN_PRIORITY); // let it go slowly;
															// raise it if we
															// end up waiting
															// for it
			loadingThread.start();
		} else {
			setNextPuzzle(null, null);
		}
	}

	private void setNextPuzzle(Puzzle nextPuzzle, PuzzleModel nextPuzzleModel) {
		synchronized (isLoading) {
			this.nextPuzzle = nextPuzzle;
			this.nextPuzzleModel = nextPuzzleModel;
			this.isLoading.set(false);
			this.isLoading.notify();
			Main.infoMsg("next puzzle model is waiting...");
		}
	}

	private PuzzleModel loadPuzzle(URL imageUrl, URL templateUrl, String name) {
		Main.infoMsg("Preparing puzzle model " + name);

		PuzzleModelDeserializer deserializer = new PuzzleModelDeserializer();
		String filenameRoot = PuzzleInstaller.getRootDir(imageUrl, templateUrl, name);
		PuzzleModel puzzle = deserializer.read(filenameRoot);

		// if puzzle is not available now, it might just mean that the
		// puzzle/template combo has never run through PuzzleInstall.
		// Doing so now will be **slow**, but there's really no other choice.
		// So, let the user know, run through install, and try again
		if (puzzle == null) {
			Main.errMsg("Couldn't load puzzle model " + name + " with template " + templateUrl + " ; will try to install that combo now; please wait...", false);
			PuzzleInstaller installer = new PuzzleInstaller();
			puzzle = installer.createAndInstallFromUrls(imageUrl, templateUrl, name);
			if (puzzle == null) {
				Main.errMsg("Couldn't create puzzle model " + name + " from the raw source data; giving up on this puzzle", false);
				return null;
			}
		}
		Main.infoMsg("Done loading puzzle model " + name);

		return puzzle;
	}

	public static List<URL> loadTemplateNames(String templateListResource) {
		List<URL> templates = new ArrayList<URL>();

		// load any that are part of the resources
		InputStream templateList = PuzzleBase.class.getResourceAsStream(templateListResource);
		BufferedReader breader = new BufferedReader(new InputStreamReader(templateList));
		String line;
		try {
			while ((line = breader.readLine()) != null) {
				String templateName = line;
				Main.infoMsg("Registering template: " + templateName);
				URL url = PuzzleBase.class.getResource(templateName);
				templates.add(url);
			}
		} catch (IOException e) {
			// report problem, but continue
			Main.errMsg("Error while loading the template list resource.  The set of available templates may not be complete", false);
		} finally {
			try {
				breader.close();
				templateList.close();
			} catch (IOException e) {
				// not going to worry about it if this happens...
			}
		}

		// load any extras at user.home
		File dir = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "gameFiles/puzzleTemplates/");
		if (!dir.exists()) {
			dir.mkdirs();
			return templates;
		}
		File[] files = dir.listFiles();
		for (File f : files) {
			if (!f.getName().endsWith(".txt")) {
				try {
					Main.infoMsg("Registering template: " + f.getAbsolutePath());
					templates.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					// report problem, but continue
					Main.errMsg("Error while loading user-supplied templates.  The set of available templates may not be complete", false);
				}
			}
		}

		return templates;
	}

	public static List<Puzzle> loadPuzzles(String puzzleListResource) {
		List<Puzzle> puzzles = new ArrayList<Puzzle>();

		// load any that are part of the resources
		InputStream puzzleList = PuzzleBase.class.getResourceAsStream(puzzleListResource);
		BufferedReader breader = new BufferedReader(new InputStreamReader(puzzleList));
		String line;
		try {
			while ((line = breader.readLine()) != null) {
				String puzzleName = line;
				loadPuzzleDescriptor(puzzles, PuzzleBase.class.getResourceAsStream(puzzleName));
			}
		} catch (IOException e) {
			// report problem, but continue
			Main.errMsg("Error while loading the puzzle list resource.  The set of available puzzles may not be complete", false);
		} finally {
			try {
				breader.close();
				puzzleList.close();
			} catch (IOException e) {
				// not going to worry about this case
			}
		}

		// load any extras at user.home
		File dir = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "gameFiles/puzzles/");
		if (!dir.exists()) {
			dir.mkdirs();
			return puzzles;
		}
		File[] files = dir.listFiles();
		for (File f : files) {
			loadPuzzleDescriptor(puzzles, f);
		}

		return puzzles;
	}

	private static void loadPuzzleDescriptor(List<Puzzle> puzzles, InputStream puzzleMetadataStream) {
		String path = null;
		String name = null;
		String description = null;
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(puzzleMetadataStream));
			path = read.readLine();
			description = read.readLine();
			name = read.readLine();
			read.close();
		} catch (FileNotFoundException e) {
			Main.saveStackTrace(e);
		} catch (IOException e) {
			Main.saveStackTrace(e);
		}
		if (path != null && name != null && description != null) {
			// supplied path can only be a resource in this case
			URL url = PuzzleBase.class.getResource(path);
			Main.infoMsg("Registering puzzle: " + path);
			puzzles.add(new Puzzle(url, description, name));
			return;
		}

		Main.errMsg("The puzzle metadata file doesn't appear to exist", false);
	}

	private static void loadPuzzleDescriptor(List<Puzzle> puzzles, File f) {
		// for this function, we're working outside the jar -- maybe a puzzle
		// has been added without updating the jar
		String path = null;
		String name = null;
		String description = null;
		try {
			BufferedReader read = new BufferedReader(new FileReader(f));
			path = read.readLine();
			description = read.readLine();
			name = read.readLine();
			read.close();
		} catch (FileNotFoundException e) {
			Main.saveStackTrace(e);
		} catch (IOException e) {
			Main.saveStackTrace(e);
		}
		if (path != null && name != null && description != null) {
			// supplied path should be an absolute path in this case
			File f2 = new File(path);
			if (f2.exists()) {
				Main.infoMsg("Registering puzzle: " + f2.getAbsolutePath());
				try {
					puzzles.add(new Puzzle(f2.toURI().toURL(), description, name));
				} catch (MalformedURLException e) {
					// report problem, but continue
					Main.errMsg("Error while loading a puzzle descriptor.  The set of available puzzles may not be complete", false);
				}
				return;
			}

		}

		Main.errMsg("The puzzle metadata file doesn't appear to exist", false);
	}

}
