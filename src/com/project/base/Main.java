package com.project.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.JOptionPane;

import com.google.common.base.Throwables;

public class Main {

	private static File logFile;
	public static boolean test = false;
	public static boolean noSleep = false;
	public static boolean soundEffects = true;
	public static boolean music = true;

	private static File getLogFile() {
		if (logFile == null) {
			File logdir = new File(System.getProperty("user.home") + "/logs");
			if (!logdir.exists())
				logdir.mkdirs();
			
			logFile = new File(logdir+"/game.log");
			try {
				if (!logFile.exists()) {
					logFile.createNewFile();
				}
			} catch (IOException e) {
				Main.saveStackTrace(e);
			}
		}
		return logFile;
	}
	
	public static void main(String[] args) {
		try {
			if (!test) {
				String testIndicator = System.getProperty("user.home")+"/enableGameTests";
				test = new File(testIndicator).exists();
			}
			
			if (test) {
				soundEffects = false;
				music = false;
				noSleep = true;
			}
			
			getLogFile(); // makes sure it's initialized
			infoMsg("Program Started");
			SleepControler sleepCont = new SleepControler();
			sleepCont.setTimer();
			GameController cont = new GameController();
			if (cont.start()) {
				infoMsg("Program Terminated Successfully");
				System.exit(0);
			} else {
				errMsg("Unknown Fatal Error", true);
			}
		} catch (Throwable e) {
			Main.saveStackTrace(e);
		}
	}

	private static void fatalErr() {
		JOptionPane.showMessageDialog(null, "Fatal Error: Please notify museum staff.");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		errMsg("Program Terminated due to Fatal Error", false);
		System.exit(1);
	}

	public static void errMsg(String msg, boolean fatal) {
		File logFile = getLogFile(); // gets and makes sure it's initialized
		int date[] = getDate();
		String dates[] = new String[3];
		for (int i = 0; i < 3; i++)
			if (date[i] < 10)
				dates[i] = "0" + Integer.toString(date[i]);
			else
				dates[i] = Integer.toString(date[i]);
		String str = date[4] + "/" + date[5] + "/" + date[3] + " " + dates[0] + ":" + dates[1] + ":" + dates[2] + " [ERR] " + msg;
		System.out.println(str);
		try {
			BufferedWriter write = new BufferedWriter(new FileWriter(logFile, true));
			write.write(str);
			write.newLine();
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (fatal)
			fatalErr();
	}

	public static void infoMsg(String msg) {
		File logFile = getLogFile(); // gets and makes sure it's initialized
		int date[] = getDate();
		String dates[] = new String[3];
		for (int i = 0; i < 3; i++)
			if (date[i] < 10)
				dates[i] = "0" + Integer.toString(date[i]);
			else
				dates[i] = Integer.toString(date[i]);
		String str = date[4] + "/" + date[5] + "/" + date[3] + " " + dates[0] + ":" + dates[1] + ":" + dates[2] + " [INFO] " + msg;
		System.out.println(str);
		try {
			BufferedWriter write = new BufferedWriter(new FileWriter(logFile, true));
			write.write(str);
			write.newLine();
			write.close();
		} catch (IOException e) {
			Main.saveStackTrace(e);
		}
	}

	public static void saveStackTrace(Throwable e) {
		String stackTrace = Throwables.getStackTraceAsString(e);
		Main.errMsg(stackTrace, false);
	}

	public static int[] getDate() {
		Calendar cal = Calendar.getInstance();
		int date[] = new int[6];
		date[0] = cal.get(Calendar.HOUR_OF_DAY);
		date[1] = cal.get(Calendar.MINUTE);
		date[2] = cal.get(Calendar.SECOND);
		date[3] = cal.get(Calendar.YEAR);
		date[4] = cal.get(Calendar.MONTH) + 1;
		date[5] = cal.get(Calendar.DAY_OF_MONTH);
		return date;
	}
}
