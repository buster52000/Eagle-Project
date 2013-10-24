package com.project.puzzle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
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

	private JPanel contentPane, puzzlePanel, buttonPanel;
	private PicturePanel[][] piecePanels;
	private JLabel lblSigalMuseum, lblItemName;
	private JButton btnMenu, btnRestart;
	private final int puzzlePanelWidth, puzzlePanelHeight;
	private MouseMotionListener mouseMotionListener;
	private MouseListener mouseListener;
	private boolean exit;
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
				gameOver();
			}
		});

		btnRestart = new JButton("Restart Puzzle");
		GridBagConstraints gbc_btnRestart = new GridBagConstraints();
		gbc_btnRestart.gridx = 1;
		gbc_btnRestart.gridy = 0;
		buttonPanel.add(btnRestart, gbc_btnRestart);
		btnRestart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				newPuzzle();
			}
		});

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
					int x = (int) puzzlePanel.getMousePosition().getX();
					int y = (int) puzzlePanel.getMousePosition().getY();
					PicturePanel panel = (PicturePanel) e.getComponent();
					panel.setLoc(x - panel.getPicMouseX(), y - panel.getPicMouseY(), new ArrayList<PicturePanel>());
				} catch (NullPointerException e1) {
					Main.errMsg("Unable to get mouse position", false);
					Main.saveStackTrace(e1);
				}
			}
		};
		
		mouseListener = new MouseListener() {

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
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				PicturePanel p = (PicturePanel) e.getSource();
				Set<PicturePanel> extendedNeighbors = new HashSet<PicturePanel>();
				for(PicturePanel panel : p.getExtendedNeighbors(extendedNeighbors)) {
					checkNear(panel, puzzle);
				}
				if (extendedNeighbors.size() == puzzle.getXPieces() * puzzle.getYPieces()) {
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
				PicturePanel p = new PicturePanel(b);
				p.setCenter(puzzle.getCenter(picX, picY));
				p.setSize(b.getWidth(), b.getHeight());
				p.setMinimumSize(new Dimension(b.getWidth(), b.getHeight()));
				p.setMaximumSize(new Dimension(b.getWidth(), b.getHeight()));
				p.addMouseMotionListener(mouseMotionListener);
				p.addMouseListener(mouseListener);
				piecePanels[picX][picY] = p;
			}
		}

		Random rand = Main.test ? new Random(3) : new Random();
		
		if (!Main.test) {
			for (int i = 0; i < puzzle.getXPieces(); i++) {
				for (int j = 0; j < puzzle.getYPieces(); j++) {
					int randX = rand.nextInt(puzzlePanelWidth - piecePanels[i][j].getWidth());
					int randY = rand.nextInt(puzzlePanelHeight - piecePanels[i][j].getHeight());
					piecePanels[i][j].setLocation(randX, randY);
					puzzlePanel.add(piecePanels[i][j]);
				}
			}

		} else {
			int currentX = (int) piecePanels[0][0].getCenter().getX();
			int currentY = (int) piecePanels[0][0].getCenter().getY();
			for (int j = 0; j < puzzle.getXPieces(); j++) {
				for (int k = 0; k < puzzle.getYPieces(); k++) {
					PicturePanel p = piecePanels[j][k];
					p.setLocationWithCenter(currentX, currentY);
					if (k < puzzle.getYPieces()-1)
						currentY += puzzle.getYInterval(k);
					puzzlePanel.add(p);
				}
				currentY = (int) piecePanels[j][0].getCenter().getY();
				if (j < puzzle.getXPieces()-1)
					currentX += puzzle.getXInterval(j);
			}
		}

		if (Main.test) {
			// in order to make sure the puzzle area is as expected, let's put a big red background there for testing!
			// there will be a 10pixel black border around it...
			JPanel p = new JPanel();
			p.setBounds(10, 10, puzzlePanelWidth-20, puzzlePanelHeight-20);
			p.setSize(puzzlePanelWidth-20, puzzlePanelHeight-20);
			p.setBackground(Color.RED);
			puzzlePanel.add(p);
			
			//TODO: this makes it easy to see that there's still an occasional piece that's partially off-screen
			// ... it doesn't seem to hurt anything, but I'd rather it didn't happen -- as long as it keeps happening, I'm a
			// little worried that a piece may start completely off the panel (making it impossible to complete the puzzle!)
		}
		
		repaint();
		setVisible(true);
		requestFocus();

		Main.infoMsg("done all puzzle prep work");
	}

	public void displayCorrect() {
		BaseUtils.displayResult(null, "doesn't/matter/here", "/gameFiles/sounds/correct.wav");
	}

	
	private int PROXIMITY = Main.test ? 100 : 10;
	
	public void checkNear(PicturePanel p, PuzzleModel puzzle) {
		int x = 0;
		int y = 0;
		for (int i = 0; i < piecePanels.length; i++)
			for (int j = 0; j < piecePanels[i].length; j++)
				if (p == piecePanels[i][j]) {
					x = i;
					y = j;
				}
		if (x > 0) {
			PicturePanel min1 = piecePanels[x - 1][y];
			if (min1.getCenter().getY() + min1.getY() < p.getCenter().getY() + p.getY() + PROXIMITY && min1.getCenter().getY() + min1.getY() > p.getCenter().getY() + p.getY() - PROXIMITY && min1.getCenter().getX() + min1.getX() < p.getCenter().getX() + p.getX() - puzzle.getXInterval(x - 1) + PROXIMITY && min1.getCenter().getX() + min1.getX() > p.getCenter().getX() + p.getX() - puzzle.getXInterval(x - 1) - PROXIMITY) {
				p.setLocationWithCenter((int) (min1.getX() + min1.getCenter().getX() + puzzle.getXInterval(x - 1)), (int) (min1.getY() + min1.getCenter().y));
				if (!p.isNeighbor(min1)) {
					p.addNeighbor(min1);
					min1.addNeighbor(p);
				}
			}
		}
		if (x < piecePanels.length - 1) {
			PicturePanel plus1 = piecePanels[x + 1][y];
			if (plus1.getCenter().y + plus1.getY() < p.getCenter().y + p.getY() + PROXIMITY && plus1.getCenter().y + plus1.getY() > p.getCenter().y + p.getY() - PROXIMITY && plus1.getCenter().x + plus1.getX() < p.getCenter().x + p.getX() + puzzle.getXInterval(x) + PROXIMITY && plus1.getCenter().x + plus1.getX() > p.getCenter().x + p.getX() + puzzle.getXInterval(x) - PROXIMITY) {
				p.setLocationWithCenter((int) (plus1.getX() + plus1.getCenter().x - puzzle.getXInterval(x)), (int) (plus1.getY() + plus1.getCenter().y));
				if (!p.isNeighbor(plus1)) {
					p.addNeighbor(plus1);
					plus1.addNeighbor(p);
				}
			}
		}
		if (y > 0) {
			PicturePanel min1 = piecePanels[x][y - 1];
			if (min1.getCenter().x + min1.getX() < p.getCenter().x + p.getX() + PROXIMITY && min1.getCenter().x + min1.getX() > p.getCenter().x + p.getX() - PROXIMITY && min1.getCenter().y + min1.getY() < p.getCenter().y + p.getY() - puzzle.getYInterval(y - 1) + PROXIMITY && min1.getCenter().y + min1.getY() > p.getCenter().getY() - puzzle.getYInterval(y - 1) - PROXIMITY) {
				p.setLocationWithCenter((int) (min1.getX() + min1.getCenter().x), (int) (min1.getY() + min1.getCenter().y + puzzle.getYInterval(y - 1)));
				if (!p.isNeighbor(min1)) {
					p.addNeighbor(min1);
					min1.addNeighbor(p);
				}
			}
		}
		if (y < piecePanels[0].length - 1) {
			PicturePanel plus1 = piecePanels[x][y + 1];
			if (plus1.getCenter().x + plus1.getX() < p.getCenter().x + p.getX() + PROXIMITY && plus1.getCenter().x + plus1.getX() > p.getCenter().x + p.getX() - PROXIMITY && plus1.getCenter().y + plus1.getY() < p.getCenter().y + p.getY() + puzzle.getYInterval(y) + PROXIMITY && plus1.getCenter().y + plus1.getY() > p.getCenter().y + p.getY() + puzzle.getYInterval(y) - PROXIMITY) {
				p.setLocationWithCenter((int) (plus1.getX() + plus1.getCenter().x), (int) (plus1.getY() + plus1.getCenter().y - puzzle.getYInterval(y)));
				if (!p.isNeighbor(plus1)) {
					p.addNeighbor(plus1);
					plus1.addNeighbor(p);
				}
			}
		}
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
