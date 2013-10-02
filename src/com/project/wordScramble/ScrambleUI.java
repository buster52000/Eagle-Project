package com.project.wordScramble;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.project.base.BaseUI;
import com.project.base.Main;

@SuppressWarnings("serial")
public abstract class ScrambleUI extends JFrame {

	private ArrayList<JLabel> answerLabels, scrambleLabels;
	private JPanel contentPane, titlePanel, mainLabelPanel, rightPanel, answerLabelPanel, scrambleLabelPanel, textPanel, picPanel, btnPanel;
	private JLabel lblSigalMuseum, lblText, lblPic;
	private boolean exit;
	private String currentText;
	private ImageIcon ico;
	private JButton btnRestartScramble, btnMainMenu;
	private Scramble scramble;

	public ScrambleUI() {

		getContentPane().setBackground(Color.BLACK);
		scramble = null;

		exit = false;
		answerLabels = new ArrayList<JLabel>();
		scrambleLabels = new ArrayList<JLabel>();

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		btnPanel = new JPanel();
		GridBagConstraints gbc_panel1 = new GridBagConstraints();
		gbc_panel1.gridwidth = 2;
		gbc_panel1.insets = new Insets(0, 0, 5, 5);
		gbc_panel1.fill = GridBagConstraints.BOTH;
		gbc_panel1.gridx = 0;
		gbc_panel1.gridy = 0;
		contentPane.add(btnPanel, gbc_panel1);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		btnPanel.setLayout(gbl_panel);
		btnPanel.setOpaque(false);

		btnMainMenu = new JButton("Main Menu");
		btnMainMenu.setPreferredSize(new Dimension(115, 22));
		btnMainMenu.setMinimumSize(new Dimension(83, 22));
		btnMainMenu.setMaximumSize(new Dimension(83, 22));
		GridBagConstraints gbc_btnMainMenu = new GridBagConstraints();
		gbc_btnMainMenu.anchor = GridBagConstraints.WEST;
		gbc_btnMainMenu.insets = new Insets(0, 0, 0, 5);
		gbc_btnMainMenu.gridx = 0;
		gbc_btnMainMenu.gridy = 0;
		btnMainMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				gameOver();
			}
		});
		btnPanel.add(btnMainMenu, gbc_btnMainMenu);

		btnRestartScramble = new JButton("Restart Scramble");
		btnRestartScramble.setOpaque(false);
		btnRestartScramble.setPreferredSize(new Dimension(150, 22));
		btnRestartScramble.setMaximumSize(new Dimension(150, 22));
		btnRestartScramble.setMinimumSize(new Dimension(150, 22));
		GridBagConstraints gbc_btnRestartScramble = new GridBagConstraints();
		gbc_btnRestartScramble.anchor = GridBagConstraints.WEST;
		gbc_btnRestartScramble.gridx = 1;
		gbc_btnRestartScramble.gridy = 0;
		btnRestartScramble.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newScramble(scramble);
			}
		});
		btnPanel.add(btnRestartScramble, gbc_btnRestartScramble);

		titlePanel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		contentPane.add(titlePanel, gbc_panel);

		lblSigalMuseum = new JLabel("Sigal Museum");
		lblSigalMuseum.setFont(new Font("Serif", Font.BOLD, 48));
		titlePanel.add(lblSigalMuseum);

		mainLabelPanel = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		contentPane.add(mainLabelPanel, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		mainLabelPanel.setLayout(gbl_panel_1);

		answerLabelPanel = new JPanel();
		answerLabelPanel.setLayout(new BoxLayout(answerLabelPanel, BoxLayout.X_AXIS));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		mainLabelPanel.add(answerLabelPanel, gbc_panel_3);

		scrambleLabelPanel = new JPanel();
		scrambleLabelPanel.setLayout(new BoxLayout(scrambleLabelPanel, BoxLayout.X_AXIS));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		mainLabelPanel.add(scrambleLabelPanel, gbc_panel_4);

		rightPanel = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 1;
		gbc_panel_2.gridy = 2;
		contentPane.add(rightPanel, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		rightPanel.setLayout(gbl_panel_2);

		textPanel = new JPanel();
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.anchor = GridBagConstraints.NORTH;
		gbc_panel_5.insets = new Insets(0, 0, 5, 0);
		gbc_panel_5.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 0;
		rightPanel.add(textPanel, gbc_panel_5);

		lblText = new JLabel();
		lblText.setFont(new Font("Serif", Font.PLAIN, 24));
		textPanel.add(lblText);

		picPanel = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 1;
		rightPanel.add(picPanel, gbc_panel_6);

		lblPic = new JLabel();
		lblPic.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
		lblPic.setAlignmentX(Component.CENTER_ALIGNMENT);
		picPanel.add(lblPic);

		contentPane.setOpaque(false);
		titlePanel.setOpaque(false);
		mainLabelPanel.setOpaque(false);
		rightPanel.setOpaque(false);
		answerLabelPanel.setOpaque(false);
		scrambleLabelPanel.setOpaque(false);
		textPanel.setOpaque(false);
		picPanel.setOpaque(false);
		lblSigalMuseum.setForeground(Color.WHITE);
		lblText.setForeground(Color.WHITE);

		setExtendedState(MAXIMIZED_BOTH);
		setUndecorated(true);
	}

	public abstract void complete();

	public boolean exit() {
		return exit;
	}

	public void newScramble(Scramble scramble) {
		exit = false;
		this.scramble = scramble;
		answerLabelPanel.removeAll();
		scrambleLabelPanel.removeAll();
		lblText.setText(scramble.getText());
		lblText.setHorizontalAlignment(SwingConstants.CENTER);
		BufferedImage pic = (BufferedImage) scramble.getPicture();
		int width;
		int height;
		if (pic.getWidth() > pic.getHeight()) {
			width = BaseUI.PIC_WIDTH;
			height = pic.getHeight();
		} else {
			width = pic.getWidth();
			height = BaseUI.PIC_WIDTH;
		}
		height = (width * height) / pic.getWidth();
		Image img = pic.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		ico = new ImageIcon(img);
		lblPic.setIcon(ico);
		String word = scramble.getWord().toUpperCase();
		ArrayList<Character> chars = new ArrayList<Character>();
		for (char c : word.toCharArray()) {
			chars.add(c);
		}
		Collections.shuffle(chars);
		answerLabels.removeAll(answerLabels);
		scrambleLabels.removeAll(scrambleLabels);
		currentText = "";
		for (char c : chars) {
			String temp = Character.toString(c);
			if (temp.equals(" "))
				temp = "•";
			JLabel s = new JLabel(temp);
			s.setForeground(Color.WHITE);
			s.setFont(new Font("Serif", Font.BOLD, 24));
			s.setBorder(BorderFactory.createLoweredBevelBorder());
			int size = 30;
			s.setPreferredSize(new Dimension(size, size));
			s.setMaximumSize(new Dimension(size, size));
			s.setMinimumSize(new Dimension(size, size));
			s.setHorizontalAlignment(SwingConstants.CENTER);
			JLabel a = new JLabel("");
			a.setForeground(Color.WHITE);
			a.setHorizontalAlignment(SwingConstants.CENTER);
			a.setFont(new Font("Serif", Font.BOLD, 24));
			a.setBorder(BorderFactory.createLoweredBevelBorder());
			a.setPreferredSize(new Dimension(size, size));
			a.setMaximumSize(new Dimension(size, size));
			a.setMinimumSize(new Dimension(size, size));
			s.addMouseListener(new MouseListener() {

				public void mouseReleased(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseClicked(MouseEvent e) {
					for (JLabel l : answerLabels) {
						if (l.getText() == null || l.getText().equals("")) {
							l.setText(((JLabel) e.getComponent()).getText());
							((JLabel) e.getComponent()).setText("");
						}
					}
					refreshCurrentText();
					inputCaptured();
					repaint();
				}
			});
			a.addMouseListener(new MouseListener() {

				public void mouseReleased(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseClicked(MouseEvent e) {
					for (JLabel l : scrambleLabels) {
						if (l.getText() == null || l.getText().equals("")) {
							l.setText(((JLabel) e.getComponent()).getText());
							((JLabel) e.getComponent()).setText("");
						}
					}
					refreshCurrentText();
					inputCaptured();
					repaint();
				}
			});
			scrambleLabels.add(s);
			answerLabels.add(a);
		}
		for (JLabel l : answerLabels) {
			answerLabelPanel.add(l);
		}
		for (JLabel l : scrambleLabels) {
			scrambleLabelPanel.add(l);
		}
		repaint();
		setVisible(true);
	}

	public void gameOver() {
		synchronized (this) {
			exit = true;
			notifyAll();
			setVisible(false);
		}
	}

	private void inputCaptured() {
		if (scramble.getText().equals(getCurrentText())) {
			complete();
		} else if (scramble.getText().length() == getCurrentText().length())
			displayWrong();
	}

	public String getCurrentText() {
		return currentText;
	}

	public void displayCorrect() {
		lblPic.setIcon(new ImageIcon("gameFiles/pics/checkMark.png"));
		AudioInputStream as = null;
		Clip clip = null;
		try {
			as = AudioSystem.getAudioInputStream(new File("gameFiles/sounds/correct.wav"));
			clip = AudioSystem.getClip();
			clip.open(as);
		} catch (UnsupportedAudioFileException e1) {
			Main.errMsg("gameFiles/sounds/correct.wav is not supported", false);
			Main.saveStackTrace(e1);
		} catch (IOException e1) {
			Main.errMsg("IOExcaption with gameFiles/sounds/correct.wav", false);
			Main.saveStackTrace(e1);
		} catch (LineUnavailableException e) {
			Main.errMsg("LineUnavailableException for gameFiles/sounds/correct.wav", false);
			Main.saveStackTrace(e);
		}
		if (Main.sound)
			clip.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Main.errMsg("Thread sleep InterruptedExcaption", false);
			Main.saveStackTrace(e);
		}
		clip.close();
		try {
			as.close();
		} catch (IOException e) {
			Main.saveStackTrace(e);
		}
	}

	public void displayWrong() {
		lblPic.setIcon(new ImageIcon("gameFiles/pics/xMark.png"));
		AudioInputStream as = null;
		Clip clip = null;
		try {
			as = AudioSystem.getAudioInputStream(new File("gameFiles/sounds/wrong.wav"));
			clip = AudioSystem.getClip();
			clip.open(as);
		} catch (UnsupportedAudioFileException e1) {
			Main.errMsg("gameFiles/sounds/wrong.wav is not supported", false);
			Main.saveStackTrace(e1);
		} catch (IOException e1) {
			Main.errMsg("IOExcaption with gameFiles/sounds/wrong.wav", false);
			Main.saveStackTrace(e1);
		} catch (LineUnavailableException e) {
			Main.errMsg("LineUnavailableException for gameFiles/sounds/wrong.wav", false);
			Main.saveStackTrace(e);
		}
		if (Main.sound)
			clip.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Main.errMsg("Thread sleep InterruptedExcaption", false);
			Main.saveStackTrace(e);
		}
		clip.close();
		try {
			as.close();
		} catch (IOException e) {
			Main.saveStackTrace(e);
		}
		lblPic.setIcon(ico);
		for (int i = 0; i < answerLabels.size(); i++) {
			scrambleLabels.get(i).setText(answerLabels.get(i).getText());
			answerLabels.get(i).setText("");
		}
		refreshCurrentText();
	}

	private void refreshCurrentText() {
		currentText = "";
		for (JLabel l : answerLabels) {
			if (l == null) {
				throw new NullPointerException("Scramble JLabel is null");
			}
			if (l.getText() == null) {
				l.setText("");
			}
			String temp = l.getText();
			if (temp.equals("•"))
				temp = " ";
			currentText = currentText + temp;
		}
	}

}