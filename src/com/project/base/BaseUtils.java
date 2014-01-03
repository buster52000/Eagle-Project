package com.project.base;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class BaseUtils {
	
	private static Map<String,InputStream> soundEffectInputStreams = new HashMap<String,InputStream>(); // cache the result streams so only loaded once
	private static Map<String,AudioInputStream> soundEffectAudioStreams = new HashMap<String,AudioInputStream>(); // cache the sound effect streams so only loaded once
	private static int soundEffectCnt = 0;
	
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

	public static void playSoundEffect0(final String soundResource, final int durationInMillis, final Runnable finallyAction) {
		AudioInputStream as = soundEffectAudioStreams.get(soundResource);
		if (as == null) {
			try {
				InputStream is = new BufferedInputStream(BaseUtils.class.getResourceAsStream(soundResource));
				as = AudioSystem.getAudioInputStream(is);
				if (as.markSupported()) {
					// may not be able to reuse, so don't cache in that case
					soundEffectAudioStreams.put(soundResource, as);
					soundEffectInputStreams.put(soundResource, is);
				} else {
					System.out.println("INFO: Can't reuse this audio stream: "+soundResource);
				}
			} catch (UnsupportedAudioFileException e1) {
				Main.errMsg(soundResource + " is not supported", false);
				Main.saveStackTrace(e1);
			} catch (IOException e) {
				Main.errMsg("IOExcaption with " + soundResource, false);
				Main.saveStackTrace(e);
			} 
		}
		if (as == null) {
			Main.errMsg("Couldn't obtain Sound Effect \""+soundResource+"\"", false);
			Main.infoMsg("Stacktrace for previous errMsg: "+Thread.currentThread().getStackTrace());
			return;
		}
		
		try {
			as.reset();
			final Clip clip = AudioSystem.getClip();
			if (clip == null) {
				Main.errMsg("Couldn't obtain AudioSystem clip", false);
				Main.infoMsg("Stacktrace for previous errMsg: "+Thread.currentThread().getStackTrace());
				return;
			}
			clip.open(as);
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						clip.start();
						Thread.sleep(durationInMillis);
					} catch (InterruptedException e) {
						Main.errMsg("Thread sleep InterruptedExcaption", false);
						Main.saveStackTrace(e);
					} finally {
						clip.stop();
						clip.close();
						if (finallyAction != null)
							finallyAction.run();
					}
				}
			};
			Thread t = new Thread(null, r, "SoundEffect"+(soundEffectCnt++));
			t.setDaemon(true);
			t.start();
		} catch (IOException e1) {
			Main.errMsg("IOException with " + soundResource, false);
			Main.saveStackTrace(e1);
		} catch (LineUnavailableException e) {
			Main.errMsg("LineUnavailableException for " + soundResource, false);
			Main.saveStackTrace(e);
		}
		
	}
	
	private static Map<String,List<Clip>> soundEffectPools = new HashMap<String,List<Clip>>();
	
	public static void playSoundEffect(final String soundResource, final int durationInMillis, final Runnable finallyAction) {
		final Clip clip;
		synchronized (soundEffectPools) {
			List<Clip> clipPool = soundEffectPools.get(soundResource);
			if (clipPool == null) soundEffectPools.put(soundResource, clipPool = new LinkedList<Clip>());
			if (clipPool.size() == 0) {
				try {
					AudioInputStream as = AudioSystem.getAudioInputStream(new BufferedInputStream(BaseUtils.class.getResourceAsStream(soundResource)));
					Clip clip2 = AudioSystem.getClip();
					clip2.open(as);
					clipPool.add(clip2);
				} catch (UnsupportedAudioFileException e1) {
					Main.errMsg(soundResource + " is not supported", false);
					Main.saveStackTrace(e1);
				} catch (IOException e) {
					Main.errMsg("IOExcaption with " + soundResource, false);
					Main.saveStackTrace(e);
				} catch (LineUnavailableException e) {
					Main.errMsg("LineUnavailableException for " + soundResource, false);
					Main.saveStackTrace(e);
				}
			}
			clip = clipPool.remove(0);
		}
		if (clip == null) {
			Main.errMsg("Couldn't obtain AudioSystem clip", false);
			Main.infoMsg("Stacktrace for previous errMsg: "+Thread.currentThread().getStackTrace());
			return;
		}
		
		clip.setFramePosition(0);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					clip.start();
					Thread.sleep(durationInMillis);
				} catch (InterruptedException e) {
					Main.errMsg("Thread sleep InterruptedExcaption", false);
					Main.saveStackTrace(e);
				} finally {
					clip.stop();
					synchronized (soundEffectPools) {
						soundEffectPools.get(soundResource).add(clip);
					}
					if (finallyAction != null)
						finallyAction.run();
				}
			}
		};
		Thread t = new Thread(null, r, "SoundEffect"+(soundEffectCnt++));
		t.setDaemon(true);
		t.start();
		
	}
	
	public static void displayResult(final JLabel lblPic, String imageResourceName, final String soundResource, final Runnable onCompletionAction) {
		final Icon holder = lblPic == null ? null : lblPic.getIcon();
		if (lblPic != null)
			lblPic.setIcon(new ImageIcon(BaseUtils.class.getResource(imageResourceName)));
		if (Main.soundEffects) {
			playSoundEffect(soundResource, 2000, new Runnable() {
				@Override
				public void run() {
					if (lblPic != null)
						lblPic.setIcon(holder);
					if (onCompletionAction != null)
						onCompletionAction.run();
				}
			});
		} else {
			if (lblPic != null)
				lblPic.setIcon(holder);
			if (onCompletionAction != null)
				onCompletionAction.run();
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
			if (currentCharNum >= str.length()) {
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
		if(bi == null)
			return null;
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
	
	public static void playClick() {
		playSoundEffect("/gameFiles/sounds/click.wav", 500, null); // review of softclick.wav in Audacity confirms that 50ms is it's duration
		// callers should not call this function more frequently than about 30ms apart
	}
	
	public static void closeAllOpenJDialogs() {
    	for (Window w : JDialog.getWindows()) {
    		if ( w instanceof JDialog) {
    			JDialog jd = (JDialog) w;
    			Main.infoMsg("Forcibly closing JDialog \""+jd.getTitle()+"\"");
    			jd.dispose();
    		}
    	}
    }

}
