package com.project.trivia;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;

import com.project.base.BaseUtils;
import com.project.base.Main;

@SuppressWarnings("serial")
public class TriviaUI extends JFrame implements ActionListener {

	private JPanel contentPane, topPanel, leftPanel, panelA, panelB, panelC, panelD, questPanel, picPanel, rightPanel, panelSubmit;
	private JLabel lblSigalMuseum, lblA, lblB, lblC, lblD, lblQuest, lblPic;
	private JButton btnSubmit, btnMainMenu;
	private JRadioButton rdbtnA, rdbtnB, rdbtnC, rdbtnD;
	private ImageIcon nextIco;
	private Trivia currentTrivia, nextTrivia;
	private ArrayList<String> nextTriviaAns;
	private int numHidden;
	private boolean exit, nextTriviaLoaded;
	private ButtonGroup group;

	private String selectedTxt;

	public TriviaUI() {

		exit = false;
		numHidden = 0;
		selectedTxt = "";
		nextIco = null;
		currentTrivia = null;
		nextTriviaAns = new ArrayList<String>();
		nextTriviaLoaded = false;

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		btnMainMenu = new JButton("Main Menu");
		btnMainMenu.setMinimumSize(new Dimension(83, 22));
		btnMainMenu.setMaximumSize(new Dimension(83, 22));
		GridBagConstraints gbc_btnMainMenu = new GridBagConstraints();
		gbc_btnMainMenu.insets = new Insets(0, 0, 5, 5);
		gbc_btnMainMenu.anchor = GridBagConstraints.WEST;
		gbc_btnMainMenu.gridx = 0;
		gbc_btnMainMenu.gridy = 0;
		contentPane.add(btnMainMenu, gbc_btnMainMenu);
		btnMainMenu.setFocusPainted(false);

		topPanel = new JPanel();
		GridBagConstraints gbc_topPanel = new GridBagConstraints();
		gbc_topPanel.gridwidth = 2;
		gbc_topPanel.insets = new Insets(0, 0, 5, 0);
		gbc_topPanel.anchor = GridBagConstraints.NORTH;
		gbc_topPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_topPanel.gridx = 0;
		gbc_topPanel.gridy = 0;
		contentPane.add(topPanel, gbc_topPanel);

		lblSigalMuseum = new JLabel("Sigal Museum");
		topPanel.add(lblSigalMuseum);
		
				questPanel = new JPanel();
				GridBagConstraints gbc_questPanel = new GridBagConstraints();
				gbc_questPanel.gridwidth = 2;
				gbc_questPanel.insets = new Insets(0, 0, 5, 5);
				gbc_questPanel.gridx = 0;
				gbc_questPanel.gridy = 1;
				contentPane.add(questPanel, gbc_questPanel);
				
						lblQuest = new JLabel("");
						questPanel.add(lblQuest);
						lblQuest.setForeground(Color.WHITE);
						lblQuest.setFont(new Font("Serif", Font.PLAIN, 36));
						questPanel.setOpaque(false);

		leftPanel = new JPanel();
		GridBagConstraints gbc_leftPanel = new GridBagConstraints();
		gbc_leftPanel.insets = new Insets(0, 0, 0, 5);
		gbc_leftPanel.fill = GridBagConstraints.BOTH;
		gbc_leftPanel.gridx = 0;
		gbc_leftPanel.gridy = 2;
		contentPane.add(leftPanel, gbc_leftPanel);
		GridBagLayout gbl_leftPanel = new GridBagLayout();
		gbl_leftPanel.columnWidths = new int[] { 0, 0 };
		gbl_leftPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_leftPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_leftPanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		leftPanel.setLayout(gbl_leftPanel);

		picPanel = new JPanel();
		GridBagConstraints gbc_picPanel = new GridBagConstraints();
		gbc_picPanel.gridheight = 2;
		gbc_picPanel.fill = GridBagConstraints.BOTH;
		gbc_picPanel.gridx = 0;
		gbc_picPanel.gridy = 0;
		leftPanel.add(picPanel, gbc_picPanel);
		GridBagLayout gbl_picPanel = new GridBagLayout();
		gbl_picPanel.columnWidths = new int[] { 0, 0 };
		gbl_picPanel.rowHeights = new int[] { 0, 0 };
		gbl_picPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_picPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		picPanel.setLayout(gbl_picPanel);

		lblPic = new JLabel("");
		GridBagConstraints gbc_lblPic = new GridBagConstraints();
		gbc_lblPic.gridx = 0;
		gbc_lblPic.gridy = 0;
		picPanel.add(lblPic, gbc_lblPic);

		rightPanel = new JPanel();
		GridBagConstraints gbc_rightPanel = new GridBagConstraints();
		gbc_rightPanel.fill = GridBagConstraints.BOTH;
		gbc_rightPanel.gridx = 1;
		gbc_rightPanel.gridy = 2;
		contentPane.add(rightPanel, gbc_rightPanel);
		GridBagLayout gbl_rightPanel = new GridBagLayout();
		gbl_rightPanel.columnWidths = new int[] { 0, 0 };
		gbl_rightPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_rightPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_rightPanel.rowWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		rightPanel.setLayout(gbl_rightPanel);

		panelA = new JPanel();
		GridBagConstraints gbc_panelA = new GridBagConstraints();
		gbc_panelA.insets = new Insets(0, 0, 5, 0);
		gbc_panelA.fill = GridBagConstraints.BOTH;
		gbc_panelA.gridx = 0;
		gbc_panelA.gridy = 0;
		rightPanel.add(panelA, gbc_panelA);
		GridBagLayout gbl_panelA = new GridBagLayout();
		gbl_panelA.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelA.rowHeights = new int[] { 0, 0 };
		gbl_panelA.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelA.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelA.setLayout(gbl_panelA);

		rdbtnA = new JRadioButton("A.");
		GridBagConstraints gbc_rdbtnA = new GridBagConstraints();
		gbc_rdbtnA.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnA.fill = GridBagConstraints.BOTH;
		gbc_rdbtnA.gridx = 0;
		gbc_rdbtnA.gridy = 0;
		panelA.add(rdbtnA, gbc_rdbtnA);

		lblA = new JLabel("");
		GridBagConstraints gbc_lblA = new GridBagConstraints();
		gbc_lblA.fill = GridBagConstraints.BOTH;
		gbc_lblA.gridx = 1;
		gbc_lblA.gridy = 0;
		panelA.add(lblA, gbc_lblA);

		panelB = new JPanel();
		GridBagConstraints gbc_panelB = new GridBagConstraints();
		gbc_panelB.insets = new Insets(0, 0, 5, 0);
		gbc_panelB.fill = GridBagConstraints.BOTH;
		gbc_panelB.gridx = 0;
		gbc_panelB.gridy = 1;
		rightPanel.add(panelB, gbc_panelB);
		GridBagLayout gbl_panelB = new GridBagLayout();
		gbl_panelB.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelB.rowHeights = new int[] { 0, 0 };
		gbl_panelB.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelB.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelB.setLayout(gbl_panelB);

		rdbtnB = new JRadioButton("B.");
		GridBagConstraints gbc_rdbtnB = new GridBagConstraints();
		gbc_rdbtnB.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnB.fill = GridBagConstraints.BOTH;
		gbc_rdbtnB.gridx = 0;
		gbc_rdbtnB.gridy = 0;
		panelB.add(rdbtnB, gbc_rdbtnB);

		lblB = new JLabel("");
		GridBagConstraints gbc_lblB = new GridBagConstraints();
		gbc_lblB.fill = GridBagConstraints.BOTH;
		gbc_lblB.gridx = 1;
		gbc_lblB.gridy = 0;
		panelB.add(lblB, gbc_lblB);

		panelC = new JPanel();
		GridBagConstraints gbc_panelC = new GridBagConstraints();
		gbc_panelC.insets = new Insets(0, 0, 5, 0);
		gbc_panelC.fill = GridBagConstraints.BOTH;
		gbc_panelC.gridx = 0;
		gbc_panelC.gridy = 2;
		rightPanel.add(panelC, gbc_panelC);
		GridBagLayout gbl_panelC = new GridBagLayout();
		gbl_panelC.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelC.rowHeights = new int[] { 0, 0 };
		gbl_panelC.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelC.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelC.setLayout(gbl_panelC);

		rdbtnC = new JRadioButton("C.");
		GridBagConstraints gbc_rdbtnC = new GridBagConstraints();
		gbc_rdbtnC.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnC.fill = GridBagConstraints.BOTH;
		gbc_rdbtnC.gridx = 0;
		gbc_rdbtnC.gridy = 0;
		panelC.add(rdbtnC, gbc_rdbtnC);

		lblC = new JLabel("");
		GridBagConstraints gbc_lblC = new GridBagConstraints();
		gbc_lblC.fill = GridBagConstraints.BOTH;
		gbc_lblC.gridx = 1;
		gbc_lblC.gridy = 0;
		panelC.add(lblC, gbc_lblC);

		panelD = new JPanel();
		GridBagConstraints gbc_panelD = new GridBagConstraints();
		gbc_panelD.insets = new Insets(0, 0, 5, 0);
		gbc_panelD.fill = GridBagConstraints.BOTH;
		gbc_panelD.gridx = 0;
		gbc_panelD.gridy = 3;
		rightPanel.add(panelD, gbc_panelD);
		GridBagLayout gbl_panelD = new GridBagLayout();
		gbl_panelD.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelD.rowHeights = new int[] { 0, 0 };
		gbl_panelD.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelD.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelD.setLayout(gbl_panelD);

		rdbtnD = new JRadioButton("D.");
		GridBagConstraints gbc_rdbtnD = new GridBagConstraints();
		gbc_rdbtnD.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnD.fill = GridBagConstraints.BOTH;
		gbc_rdbtnD.gridx = 0;
		gbc_rdbtnD.gridy = 0;
		panelD.add(rdbtnD, gbc_rdbtnD);

		lblD = new JLabel("");
		GridBagConstraints gbc_lblD = new GridBagConstraints();
		gbc_lblD.fill = GridBagConstraints.BOTH;
		gbc_lblD.gridx = 1;
		gbc_lblD.gridy = 0;
		panelD.add(lblD, gbc_lblD);

		panelSubmit = new JPanel();
		GridBagConstraints gbc_panelSubmit = new GridBagConstraints();
		gbc_panelSubmit.anchor = GridBagConstraints.CENTER;
		gbc_panelSubmit.gridx = 0;
		gbc_panelSubmit.gridy = 4;
		rightPanel.add(panelSubmit, gbc_panelSubmit);

		lblSigalMuseum.setForeground(Color.WHITE);
		lblA.setForeground(Color.WHITE);
		lblB.setForeground(Color.WHITE);
		lblC.setForeground(Color.WHITE);
		lblD.setForeground(Color.WHITE);
		rdbtnA.setForeground(Color.WHITE);
		rdbtnB.setForeground(Color.WHITE);
		rdbtnC.setForeground(Color.WHITE);
		rdbtnD.setForeground(Color.WHITE);

		lblSigalMuseum.setFont(new Font("Serif", Font.BOLD, 48));
		lblA.setFont(new Font("Serif", Font.PLAIN, 26));
		lblB.setFont(new Font("Serif", Font.PLAIN, 26));
		lblC.setFont(new Font("Serif", Font.PLAIN, 26));
		lblD.setFont(new Font("Serif", Font.PLAIN, 26));
		rdbtnA.setFont(new Font("Serif", Font.PLAIN, 26));
		rdbtnB.setFont(new Font("Serif", Font.PLAIN, 26));
		rdbtnC.setFont(new Font("Serif", Font.PLAIN, 26));
		rdbtnD.setFont(new Font("Serif", Font.PLAIN, 26));

		rdbtnA.setFocusPainted(false);
		rdbtnB.setFocusPainted(false);
		rdbtnC.setFocusPainted(false);
		rdbtnD.setFocusPainted(false);

		topPanel.setOpaque(false);
		leftPanel.setOpaque(false);
		panelA.setOpaque(false);
		panelB.setOpaque(false);
		panelC.setOpaque(false);
		panelD.setOpaque(false);
		picPanel.setOpaque(false);
		rightPanel.setOpaque(false);
		panelSubmit.setOpaque(false);
		rdbtnA.setOpaque(false);
		rdbtnB.setOpaque(false);
		rdbtnC.setOpaque(false);
		rdbtnD.setOpaque(false);

		btnSubmit = new JButton("Submit");
		panelSubmit.add(btnSubmit);

		btnSubmit.setForeground(Color.BLACK);
		btnSubmit.setFont(new Font("Serif", Font.PLAIN, 26));
		btnSubmit.setFocusPainted(false);

		group = new ButtonGroup();
		group.add(rdbtnA);
		group.add(rdbtnB);
		group.add(rdbtnC);
		group.add(rdbtnD);

		rdbtnA.addActionListener(this);
		rdbtnB.addActionListener(this);
		rdbtnC.addActionListener(this);
		rdbtnD.addActionListener(this);
		setUndecorated(true);
	}

	public JButton getMenuButton() {
		return btnMainMenu;
	}

	public void prepNextTrivia(Trivia trivia, BufferedImage image) {
		nextTrivia = trivia;
		nextIco = new ImageIcon(image);
		nextTriviaAns = new ArrayList<String>();
		for(String s : trivia.getAnswers())
			nextTriviaAns.add(s);
		Collections.shuffle(nextTriviaAns);
		nextTriviaLoaded = true;
	}
	
	public void nextTrivia() {
		if(!nextTriviaLoaded) {
			Main.errMsg("TriviaUI.nextTrivia called before trivia loaded", false);
			return;
		}
		nextTriviaLoaded = false;
		exit = false;
		numHidden = 0;
		currentTrivia = nextTrivia;
		group.clearSelection();
		lblQuest.setText(currentTrivia.getQuestion());
//		ArrayList<String> temp = new ArrayList<String>();
//		for (String s : trivia.getAnswers()) {
//			temp.add(s);
//		}
//		Collections.shuffle(temp);
		lblPic.setIcon(nextIco);
		lblA.setText(nextTriviaAns.get(0));
		lblB.setText(nextTriviaAns.get(1));
		lblC.setText(nextTriviaAns.get(2));
		lblD.setText(nextTriviaAns.get(3));
		lblA.setForeground(Color.WHITE);
		lblB.setForeground(Color.WHITE);
		lblC.setForeground(Color.WHITE);
		lblD.setForeground(Color.WHITE);
		Font font = new Font("Serif", Font.PLAIN, 26);
		lblA.setFont(font);
		lblB.setFont(font);
		lblC.setFont(font);
		lblD.setFont(font);
		rdbtnA.setEnabled(true);
		rdbtnB.setEnabled(true);
		rdbtnC.setEnabled(true);
		rdbtnD.setEnabled(true);
//		ImageIcon ico = new ImageIcon(image);
		repaint();
		setVisible(true);
		requestFocusInWindow();
	}

	public boolean exit() {
		return exit;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void hideNext() {
		String lookFor = currentTrivia.getAnswers()[numHidden];
		numHidden++;
		if (lookFor.equals(lblA.getText())) {
			rdbtnA.setEnabled(false);
			lblA.setForeground(Color.RED);
			Map attrib = lblA.getFont().getAttributes();
			attrib.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			lblA.setFont(new Font(attrib));
		} else if (lookFor.equals(lblB.getText())) {
			rdbtnB.setEnabled(false);
			lblB.setForeground(Color.RED);
			Map attrib = lblB.getFont().getAttributes();
			attrib.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			lblB.setFont(new Font(attrib));
		} else if (lookFor.equals(lblC.getText())) {
			rdbtnC.setEnabled(false);
			lblC.setForeground(Color.RED);
			Map attrib = lblC.getFont().getAttributes();
			attrib.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			lblC.setFont(new Font(attrib));
		} else if (lookFor.equals(lblD.getText())) {
			rdbtnD.setEnabled(false);
			lblD.setForeground(Color.RED);
			Map attrib = lblD.getFont().getAttributes();
			attrib.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			lblD.setFont(new Font(attrib));
		} else {
			Main.errMsg("Could not hide the " + numHidden + " answer", false);
		}
		if (numHidden == 3) {
			lookFor = currentTrivia.getAnswers()[3];
			group.clearSelection();
			if (lookFor.equals(lblA.getText())) {
				rdbtnA.setEnabled(false);
				lblA.setForeground(Color.GREEN);
			} else if (lookFor.equals(lblB.getText())) {
				rdbtnB.setEnabled(false);
				lblB.setForeground(Color.GREEN);
			} else if (lookFor.equals(lblC.getText())) {
				rdbtnC.setEnabled(false);
				lblC.setForeground(Color.GREEN);
			} else if (lookFor.equals(lblD.getText())) {
				rdbtnD.setEnabled(false);
				lblD.setForeground(Color.GREEN);
			} else {
				Main.errMsg("Could not hide the " + numHidden + " answer", false);
			}
		}
	}

	public void displayCorrect(final Runnable onCompletionAction) {
		group.clearSelection();
		BaseUtils.displayResult(lblPic, "/gameFiles/pics/checkMark.png", "/gameFiles/sounds/correct.wav", onCompletionAction);
	}

	public void displayWrong() {
		final Runnable onCompletionAction = new Runnable() {
			@Override
			public void run() {
				group.clearSelection();
				repaint();
			}
		};
		BaseUtils.displayResult(lblPic, "/gameFiles/pics/xMark.png", "/gameFiles/sounds/wrong.wav", onCompletionAction);
	}

	public JButton getSubmitButton() {
		return btnSubmit;
	}

	public String getSelected() {
		return selectedTxt;
	}

	public void gameOver() {
		synchronized (this) {
			exit = true;
			notifyAll();
			setVisible(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JRadioButton b = (JRadioButton) e.getSource();
		switch (b.getText()) {
		case "A.":
			selectedTxt = lblA.getText();
			break;
		case "B.":
			selectedTxt = lblB.getText();
			break;
		case "C.":
			selectedTxt = lblC.getText();
			break;
		case "D.":
			selectedTxt = lblD.getText();
			break;
		default:
			Main.errMsg("Unable to determine RadioButton fatal because timers could not be stopped", true);
		}
	}

}
