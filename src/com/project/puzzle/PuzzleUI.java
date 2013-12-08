package com.project.puzzle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.project.base.BaseUtils;
import com.project.base.Main;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JButton;

import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("serial")
public abstract class PuzzleUI extends JFrame {

	private static int PROXIMITY = Main.test ? 100 : 10;
	
	private JPanel contentPane, puzzlePanel, buttonPanel;
	private PicturePanel[][] piecePanels;
	private JLabel lblSigalMuseum, lblItemName;
	private JButton btnMenu, btnRestart;
	private int puzzlePanelWidth, puzzlePanelHeight;
	private MouseMotionListener mouseMotionListener;
	private MouseListener mouseListener;
	private boolean exit, firstEntry=true;
	private PuzzleModel puzzle;

	public PuzzleUI() {

		setExtendedState(Frame.MAXIMIZED_BOTH);
		setUndecorated(true);
		setBackground(Color.BLACK);
		setForeground(Color.WHITE);

		exit = false;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setForeground(Color.WHITE);
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		buttonPanel = new JPanel();
		buttonPanel.setForeground(Color.WHITE);
		buttonPanel.setBackground(Color.BLACK);
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.gridwidth = 2;
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 0);
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 0;
		contentPane.add(buttonPanel, gbc_buttonPanel);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_buttonPanel.rowHeights = new int[] { 0, 0 };
		gbl_buttonPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_buttonPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		buttonPanel.setLayout(gbl_buttonPanel);

		btnMenu = new JButton("Main Menu");
		GridBagConstraints gbc_btnMenu = new GridBagConstraints();
		gbc_btnMenu.insets = new Insets(0, 0, 0, 5);
		gbc_btnMenu.anchor = GridBagConstraints.WEST;
		gbc_btnMenu.gridx = 0;
		gbc_btnMenu.gridy = 0;
		buttonPanel.add(btnMenu, gbc_btnMenu);
		btnMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// disable exit buttons; they'll either not be needed anymore or be re-enabled after the next puzzle is setup
				btnMenu.setEnabled(false);
				btnRestart.setEnabled(false);
				
				gameOver();
			}
		});
		btnMenu.setEnabled(false);
		
		btnRestart = new JButton("Restart Puzzle");
		GridBagConstraints gbc_btnRestart = new GridBagConstraints();
		gbc_btnRestart.gridx = 1;
		gbc_btnRestart.gridy = 0;
		buttonPanel.add(btnRestart, gbc_btnRestart);
		btnRestart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// disable exit buttons; they'll either not be needed anymore or be re-enabled after the next puzzle is setup
				btnMenu.setEnabled(false);
				btnRestart.setEnabled(false);
				
				newPuzzle();
			}
		});
		btnRestart.setEnabled(false);

		lblSigalMuseum = new JLabel("Sigal Museum");
		lblSigalMuseum.setForeground(Color.WHITE);
		lblSigalMuseum.setBackground(Color.BLACK);
		lblSigalMuseum.setFont(new Font("Serif", Font.BOLD, 48));
		GridBagConstraints gbc_lblSigalMuseum = new GridBagConstraints();
		gbc_lblSigalMuseum.gridwidth = 2;
		gbc_lblSigalMuseum.insets = new Insets(0, 0, 5, 0);
		gbc_lblSigalMuseum.gridx = 0;
		gbc_lblSigalMuseum.gridy = 1;
		contentPane.add(lblSigalMuseum, gbc_lblSigalMuseum);

		lblItemName = new JLabel();
		lblItemName.setForeground(Color.WHITE);
		lblItemName.setBackground(Color.BLACK);
		lblItemName.setFont(new Font("Serif", Font.BOLD, 36));
		GridBagConstraints gbc_lblItemName = new GridBagConstraints();
		gbc_lblItemName.anchor = GridBagConstraints.EAST;
		gbc_lblItemName.insets = new Insets(0, 0, 5, 5);
		gbc_lblItemName.gridx = 0;
		gbc_lblItemName.gridy = 2;
		contentPane.add(lblItemName, gbc_lblItemName);

		puzzlePanel = new JPanel();
		puzzlePanel.setForeground(Color.WHITE);
		puzzlePanel.setBackground(Color.BLACK);
		GridBagConstraints gbc_puzzlePanel = new GridBagConstraints();
		gbc_puzzlePanel.gridwidth = 2;
		gbc_puzzlePanel.fill = GridBagConstraints.BOTH;
		gbc_puzzlePanel.gridx = 0;
		gbc_puzzlePanel.gridy = 3;
		contentPane.add(puzzlePanel, gbc_puzzlePanel);
		puzzlePanel.setLayout(null);
		pack();
		
		// java - get screen size using the Toolkit class
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		puzzlePanelWidth = screenSize.width;
		puzzlePanelHeight = screenSize.height - contentPane.getHeight();

		mouseMotionListener = new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent arg0) {

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				try {
					Point p = puzzlePanel.getMousePosition();
					if (p != null) {
						int x = (int) p.getX();
						int y = (int) p.getY();
						PicturePanel panel = (PicturePanel) e.getComponent();
						panel.setLoc(x - panel.getPicMouseX(), y - panel.getPicMouseY(), new ArrayList<PicturePanel>());
					}
				} catch (NullPointerException e1) {
					Main.errMsg("Unable to get mouse position", false);
					Main.saveStackTrace(e1);
				}
			}
		};
		
		mouseListener = new MouseListener() {
			private int numNeighborsBeforeMove;
			
			@Override
			public void mouseClicked(MouseEvent arg0) {

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {

			}

			@Override
			public void mouseExited(MouseEvent arg0) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				PicturePanel p = (PicturePanel) e.getComponent();
				p.setPicMouse(e.getX(), e.getY());
				synchronized (this) {
					numNeighborsBeforeMove = getNeighborhood(p).size();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int numAfter = -1, numBefore = -1;
				synchronized (this) {
					numBefore = numNeighborsBeforeMove;
					numAfter = calcNewNeighborhood((PicturePanel) e.getSource()).size();
				}
				if (numAfter > numBefore) {
					BaseUtils.playClick();
					if (numAfter == puzzle.getXPieces() * puzzle.getYPieces()) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								// disable exit buttons; they'll either not be needed anymore or be re-enabled after the next puzzle is setup
								btnMenu.setEnabled(false);
								btnRestart.setEnabled(false);
								
								complete();
							}
						});
					}
				} else if (numAfter == puzzle.getXPieces() * puzzle.getYPieces()) {
					// there's still a case I haven't found yet where we get here after the puzzle has already
					// been completed.  So, let's make sure we notice completion in this case too...
					Main.infoMsg("Found puzzle done by the unexpected route");

					// disable exit buttons; they'll either not be needed anymore or be re-enabled after the next puzzle is setup
					btnMenu.setEnabled(false);
					btnRestart.setEnabled(false);
					
					complete();
				}
			}
		};
		
	}

	public abstract void complete();

	public void setModel(PuzzleModel m, String name) {
		this.puzzle = m;
		lblItemName.setText(name);
	}
	
	public void newPuzzle() {
		puzzlePanel.removeAll();

		Main.infoMsg("Preparing all the piece UIs");

		piecePanels = new PicturePanel[puzzle.getXPieces()][puzzle.getYPieces()];
		for (int picX = 0; picX < puzzle.getXPieces(); picX++) {
			for (int picY = 0; picY < puzzle.getYPieces(); picY++) {
				BufferedImage b = puzzle.getPiece(picX, picY);
				PicturePanel p = new PicturePanel(b, picX, picY);
				p.setCenter(puzzle.getCenter(picX, picY));
				p.setSize(b.getWidth(), b.getHeight());
				p.setMinimumSize(new Dimension(b.getWidth(), b.getHeight()));
				p.setMaximumSize(new Dimension(b.getWidth(), b.getHeight()));
				p.addMouseMotionListener(mouseMotionListener);
				p.addMouseListener(mouseListener);
				piecePanels[picX][picY] = p;
			}
		}

		// we're doing some ugly stuff here!  The following sequence was found to work cross-platform only after a lot of trial and error, so be careful about changes!
		// I'm not sure exaclty which parts are platform-specific, but to cleanly grab the correct puzzlePanelHeight, this was the combination that 
		// was found to work.
		repaint();
		setVisible(true);
		if (firstEntry) {
			int contentHeight = contentPane.getHeight();
			int puzzleUIHeight = getHeight();
			// for some strange reason (at least on Mac), this line returns a different value (0) than the following line!?!?
			// puzzlePanelHeight = getHeight() - contentPane.getHeight();
			puzzlePanelHeight = puzzleUIHeight - contentHeight;
			Main.infoMsg("Recalculated panel height: "+puzzlePanelHeight);
			firstEntry = false;
		}

		setupPuzzlePanel();
	}

	public void setupPuzzlePanel(final int i, final int j, final Random rand) {
		int xRange = puzzlePanelWidth - piecePanels[i][j].getWidth();
		int yRange = puzzlePanelHeight - piecePanels[i][j].getHeight();
		int randX = rand.nextInt(xRange);
		int randY = rand.nextInt(yRange);
		piecePanels[i][j].setLocation(randX, randY);
		puzzlePanel.add(piecePanels[i][j]);
		
		try {
			repaint();
			BaseUtils.playClick();
			Thread.sleep(60);
		} catch (InterruptedException ie) {
			// TODO Auto-generated catch block
			ie.printStackTrace();
		}
		
		final int nextJ = j+1 == puzzle.getYPieces() ? 0 : j+1;
		final int nextI = nextJ == 0 ? (i+1 == puzzle.getXPieces() ? 0 : i+1) : i;
		if (nextJ == 0 && nextI == 0) {
			// all done the pieces -- finish up

			// call repaint once more to make sure everything's displayed
			repaint();
			setVisible(true);

			Main.infoMsg("done all puzzle prep work");
			btnMenu.setEnabled(true);
			btnRestart.setEnabled(true);
			return;
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setupPuzzlePanel(nextI, nextJ, rand);
				}
			});
		}
	}
	
	public void setupTestPuzzlePanel(final int i, final int j, final Random rand) {
		int currentX = (int) piecePanels[0][0].getCenter().getX();
		int currentY = (int) piecePanels[0][0].getCenter().getY();
		boolean done = false;
		for (int ii = 0; !done && ii < puzzle.getXPieces(); ii++) {
			for (int jj = 0; !done && jj < puzzle.getYPieces(); jj++) {
				PicturePanel p = piecePanels[ii][jj];
				p.setLocationWithCenter(currentX, currentY);
				if (jj < puzzle.getYPieces()-1)
					currentY += puzzle.getYInterval(jj);
				if (ii == i && jj == j) {
					puzzlePanel.add(p);
					done = true;
				}
			}
			currentY = (int) piecePanels[ii][0].getCenter().getY();
			if (ii < puzzle.getXPieces()-1)
				currentX += puzzle.getXInterval(ii);
		}
		
		try {
			repaint();
			BaseUtils.playClick();
			Thread.sleep(60);
		} catch (InterruptedException ie) {
			// TODO Auto-generated catch block
			ie.printStackTrace();
		}
		
		final int nextJ = j+1 == puzzle.getYPieces() ? 0 : j+1;
		final int nextI = nextJ == 0 ? (i+1 == puzzle.getXPieces() ? 0 : i+1) : i;
		if (nextJ == 0 && nextI == 0) {
			// all done the pieces -- finish up

			// in order to make sure the puzzle area is as expected, let's put a big red background there for testing!
			// there will be a 10pixel yellow border around it...
			JPanel p = new JPanel();
			p.setBounds(10, 10, puzzlePanelWidth-20, puzzlePanelHeight-20);
			p.setSize(puzzlePanelWidth-20, puzzlePanelHeight-20);
			p.setBackground(Color.RED);
			puzzlePanel.add(p);
			p = new JPanel();
			p.setBounds(0, 0, puzzlePanelWidth, puzzlePanelHeight);
			p.setSize(puzzlePanelWidth, puzzlePanelHeight);
			p.setBackground(Color.YELLOW);
			puzzlePanel.add(p);
			
			// call repaint once more to make sure everything's displayed
			repaint();
			setVisible(true);

			Main.infoMsg("done all puzzle prep work");
			btnMenu.setEnabled(true);
			btnRestart.setEnabled(true);
			return;
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setupTestPuzzlePanel(nextI, nextJ, rand);
				}
			});
		}
	}
	
	public void setupPuzzlePanel() {
		Main.infoMsg("Setting up puzzlePanel");
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (Main.test) setupTestPuzzlePanel(0, 0, new Random(3));
				else setupPuzzlePanel(0, 0, new Random());
			}
		});
		
	}
	
	public void displayCorrect() {
		BaseUtils.displayResult(null, "doesn't/matter/here", "/gameFiles/sounds/correct.wav", null);
	}

	
	public Set<PicturePanel> getNeighborhood(PicturePanel p) {
		return p.getExtendedNeighbors(new HashSet<PicturePanel>());
	}
	
	public Set<PicturePanel> calcNewNeighborhood(PicturePanel p) {
		Set<PicturePanel> neighbors = null;
		checkNear(p, puzzle);
		boolean foundSomething = true;
		while (foundSomething) {
			foundSomething = false;
			neighbors = new HashSet<PicturePanel>();
			for(PicturePanel panel : p.getExtendedNeighbors(neighbors)) {
				foundSomething |= checkNear(panel, puzzle);
			}
		}
		return neighbors;
	}
	
	private boolean checkNear(PicturePanel p, PuzzleModel puzzle) {
		boolean foundNewNeighbor = false;
		int x = p.getPieceX();
		int y = p.getPieceY();
		double px = p.getCenter().getX() + p.getX();
		double py = p.getCenter().getY() + p.getY();
		if (x > 0) {
			PicturePanel neighbor = piecePanels[x - 1][y];
			double interval = puzzle.getXInterval(x - 1);
			double nx = neighbor.getCenter().getX() + neighbor.getX();
			double ny = neighbor.getCenter().getY() + neighbor.getY();
			if (ny < py + PROXIMITY && ny > py - PROXIMITY && nx < px - interval + PROXIMITY && nx > px - interval - PROXIMITY) {
				p.setLocationWithCenter((int) (nx + interval + 0.5), (int) ny);
				if (p.addNeighbor(neighbor)) {
					neighbor.addNeighbor(p);
					foundNewNeighbor = true;
				}
			}
		}
		if (x < piecePanels.length - 1) {
			PicturePanel neighbor = piecePanels[x + 1][y];
			double interval = puzzle.getXInterval(x);
			double nx = neighbor.getCenter().getX() + neighbor.getX();
			double ny = neighbor.getCenter().getY() + neighbor.getY();
			if (ny < py + PROXIMITY && ny > py - PROXIMITY && nx < px + interval + PROXIMITY && nx > px + interval - PROXIMITY) {
				p.setLocationWithCenter((int) (nx - interval + 0.5), (int) ny);
				if (p.addNeighbor(neighbor)) {
					neighbor.addNeighbor(p);
					foundNewNeighbor = true;
				}
			}
		}
		if (y > 0) {
			PicturePanel neighbor = piecePanels[x][y - 1];
			double interval = puzzle.getYInterval(y - 1);
			double nx = neighbor.getCenter().getX() + neighbor.getX();
			double ny = neighbor.getCenter().getY() + neighbor.getY();
			if (nx < px + PROXIMITY && nx > px - PROXIMITY && ny < py - interval + PROXIMITY && ny > py - interval - PROXIMITY) {
				p.setLocationWithCenter((int) nx, (int) (ny + interval + 0.5));
				if (p.addNeighbor(neighbor)) {
					neighbor.addNeighbor(p);
					foundNewNeighbor = true;
				}
			}
		}
		if (y < piecePanels[0].length - 1) {
			PicturePanel neighbor = piecePanels[x][y + 1];
			double interval = puzzle.getYInterval(y);
			double nx = neighbor.getCenter().getX() + neighbor.getX();
			double ny = neighbor.getCenter().getY() + neighbor.getY();
			if (nx < px + PROXIMITY && nx > px - PROXIMITY && ny < py + interval + PROXIMITY && ny > py + interval - PROXIMITY) {
				p.setLocationWithCenter((int) nx, (int) (ny - interval + 0.5));
				if (p.addNeighbor(neighbor)) {
					neighbor.addNeighbor(p);
					foundNewNeighbor = true;
				}
			}
		}
		return foundNewNeighbor;
	}

	public boolean exit() {
		return exit;
	}

	public void restartGame() {
		exit = false;
	}
	
	public void gameOver() {
		synchronized (this) {
			puzzlePanel.removeAll();
			repaint();
			setVisible(false);
			exit = true;
			notifyAll();
		}
	}

}
