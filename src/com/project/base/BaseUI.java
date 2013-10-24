package com.project.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class BaseUI extends JFrame implements Runnable, KeyListener {

	public static final int PIC_WIDTH = 500;
	
	private JButton game1, game2, game3;
	private JPanel ssPanel, menuPanel;
	private JLabel sigalMuseum, menuMuseum;

	public BaseUI() {
		addKeyListener(this);
		setForeground(Color.WHITE);
		setExtendedState(MAXIMIZED_BOTH);
		setUndecorated(true);
		game1 = new JButton("Word Scramble");
		game2 = new JButton("Trivia");
		game3 = new JButton("Puzzle");
		game1.setForeground(Color.BLACK);
		game2.setForeground(Color.BLACK);
		game3.setForeground(Color.BLACK);
		game1.setFont(new Font(Font.SERIF, Font.BOLD, 26));
		game2.setFont(new Font(Font.SERIF, Font.BOLD, 26));
		game3.setFont(new Font(Font.SERIF, Font.BOLD, 26));
		sigalMuseum = new JLabel("Sigal Museum");
		menuMuseum = new JLabel("Sigal Museum");
		menuMuseum.setFont(new Font(Font.SERIF, Font.BOLD, 72));
		menuMuseum.setHorizontalAlignment(SwingConstants.CENTER);
		menuMuseum.setForeground(Color.WHITE);
		menuMuseum.setVerticalAlignment(SwingConstants.BOTTOM);
		sigalMuseum.setFont(new Font(Font.SERIF, Font.BOLD, 72));
		sigalMuseum.setHorizontalAlignment(SwingConstants.CENTER);
		sigalMuseum.setVerticalAlignment(SwingConstants.CENTER);
		sigalMuseum.setForeground(Color.WHITE);
		ssPanel = new JPanel();
		menuPanel = new JPanel();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		ssPanel.setOpaque(false);
		menuPanel.setOpaque(false);
		getContentPane().setBackground(Color.BLACK);
		ssPanel.setLayout(new BorderLayout());
		menuPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		//General
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1;
		c.weighty = 0.25;
		c.insets = new Insets(0, 50, 200, 50);
		
		//Label
		c.anchor = GridBagConstraints.SOUTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 5;
		c.gridheight = 1;
		menuPanel.add(menuMuseum, c);
		
		//Buttons
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 2;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridwidth = 1;
		menuPanel.add(game1, c);
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 4;
		menuPanel.add(game2, c);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridx = 3;
		menuPanel.add(game3, c);
		ssPanel.add(sigalMuseum);
		setVisible(true);
	}

	public void changeSS() {
		Random rand = new Random();
		int x = rand.nextInt(9);
		switch (x) {
		case 0:
			sigalMuseum.setHorizontalAlignment(SwingConstants.LEFT);
			sigalMuseum.setVerticalAlignment(SwingConstants.TOP);
			break;
		case 1:
			sigalMuseum.setHorizontalAlignment(SwingConstants.CENTER);
			sigalMuseum.setVerticalAlignment(SwingConstants.TOP);
			break;
		case 2:
			sigalMuseum.setHorizontalAlignment(SwingConstants.RIGHT);
			sigalMuseum.setVerticalAlignment(SwingConstants.TOP);
			break;
		case 3:
			sigalMuseum.setHorizontalAlignment(SwingConstants.LEFT);
			sigalMuseum.setVerticalAlignment(SwingConstants.CENTER);
			break;
		case 4:
			sigalMuseum.setHorizontalAlignment(SwingConstants.CENTER);
			sigalMuseum.setVerticalAlignment(SwingConstants.CENTER);
			break;
		case 5:
			sigalMuseum.setHorizontalAlignment(SwingConstants.RIGHT);
			sigalMuseum.setVerticalAlignment(SwingConstants.CENTER);
			break;
		case 6:
			sigalMuseum.setHorizontalAlignment(SwingConstants.LEFT);
			sigalMuseum.setVerticalAlignment(SwingConstants.BOTTOM);
			break;
		case 7:
			sigalMuseum.setHorizontalAlignment(SwingConstants.CENTER);
			sigalMuseum.setVerticalAlignment(SwingConstants.BOTTOM);
			break;
		case 8:
			sigalMuseum.setHorizontalAlignment(SwingConstants.RIGHT);
			sigalMuseum.setVerticalAlignment(SwingConstants.BOTTOM);
			break;
		default:
			Main.errMsg("rand.nextInt() in project.base.baseUI.changeSS() returned invalid number", false);
			break;
		}
		sigalMuseum.repaint();
		sigalMuseum.revalidate();
	}

	public void showScreenSaver() {
		remove(menuPanel);
		add(ssPanel);
		repaint();
		revalidate();
	}

	public void showMenu() {
		remove(ssPanel);
		add(menuPanel);
		repaint();
		revalidate();
	}

	public JFrame getJFrame() {
		return this;
	}

	public ArrayList<JButton> getJButtons() {
		ArrayList<JButton> buttons = new ArrayList<JButton>();
		buttons.add(game1);
		buttons.add(game2);
		buttons.add(game3);
		return buttons;
	}

	@Override
	public void run() {
		while (true) {
			changeSS();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Main.saveStackTrace(e);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();
		if(c == 'x') {
			Main.infoMsg("Program Stopped with exit key");
			System.exit(0);
		}
	}
}
