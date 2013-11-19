package com.project.base;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class BaseUtils {

	public static BufferedImage loadImage(URL url, int smallerSideMax) {
		BufferedImage img = null;
		try {
			BufferedImage before = ImageIO.read(url);
			int w = before.getWidth();
			int h = before.getHeight();
			int smaller = w < h ? w : h;
			if (smaller < smallerSideMax)
				return before;
			else {
				double s = smallerSideMax * 1.0 / smaller;

				AffineTransform at = new AffineTransform();
				at.scale(s, s);
				AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
				img = scaleOp.filter(before, new BufferedImage((int) (s * w + 0.5), (int) (s * h + 0.5), BufferedImage.TYPE_INT_ARGB));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	public static void displayResult(final JLabel lblPic, String imageResourceName, final String soundResource) {
		final Icon holder = lblPic == null ? null : lblPic.getIcon();
		if (lblPic != null)
			lblPic.setIcon(new ImageIcon(BaseUtils.class.getResource(imageResourceName)));
		if (Main.soundEffects) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					AudioInputStream as = null;
					Clip clip = null;
					try {
						as = AudioSystem.getAudioInputStream(new BufferedInputStream(BaseUtils.class.getResourceAsStream(soundResource)));
						clip = AudioSystem.getClip();
						clip.open(as);
					} catch (UnsupportedAudioFileException e1) {
						Main.errMsg(soundResource + " is not supported", false);
						Main.saveStackTrace(e1);
					} catch (IOException e1) {
						Main.errMsg("IOExcaption with " + soundResource, false);
						Main.saveStackTrace(e1);
					} catch (LineUnavailableException e) {
						Main.errMsg("LineUnavailableException for " + soundResource, false);
						Main.saveStackTrace(e);
					}
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
					if (lblPic != null)
						lblPic.setIcon(holder);
				}
			});
		}
	}

	public static void showDescriptionDialog(String str, BufferedImage image, String title) {
		ImageIcon ico = new ImageIcon(image);
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
		UIManager.put("OptionPane.messageFont", new Font("Serif", Font.PLAIN, 20));
		JOptionPane.showOptionDialog(null, formatted, title, JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, ico, new String[] { "Next" }, "Next");
	}

	public static BufferedImage scaleWithLongestSide(BufferedImage bi, int size) {
		int w = 1;
		int h = 1;
		if (bi.getHeight() == bi.getWidth()) {
			w = size;
			h = size;
		} else if (bi.getHeight() > bi.getWidth()) {
			h = size;
			w = h * bi.getWidth() / bi.getHeight();
		} else if (bi.getHeight() < bi.getWidth()) {
			w = size;
			h = w * bi.getHeight() / bi.getWidth();
		}
		Image img = bi.getScaledInstance(w, h, Image.SCALE_SMOOTH);
		bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(img, 0, 0, null);
		return bi;
	}

}
