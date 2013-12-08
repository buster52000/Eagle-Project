package com.project.base;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class SleepControler {

	private FutureAction sleepTimer, wakeupTimer;
	// private long[] openTimeSummer = { 39600000, -1, 36000000, 36000000, 36000000, 36000000, 32400000 };
	// private long[] closeTimeSummer = { 57600000, -1, 57600000, 57600000, 57600000, 61200000, 57600000 };
	// private long[] openTimeWinter = { 43200000, -1, -1, 36000000, 36000000, 36000000, 36000000 };
	// private long[] closeTimeWinter = { 57600000, -1, -1, 57600000, 57600000, 61200000, 57600000 };
	private long openTime = 28800000;
	private long closeTime = 64800000;
	private long oneDay = 86400000;

	public SleepControler() {
		sleepTimer = new FutureAction("SleepTimer") {

			@Override
			public void performAction() {
				sleepMode();
				setTimer();
			}

			@Override
			public void actionCancelled() {

			}
		};
		wakeupTimer = new FutureAction("WakeupTimer") {

			@Override
			public void performAction() {
				wakeUp();
				setTimer();
			}

			@Override
			public void actionCancelled() {

			}
		};
	}

	public void setTimer() {
		if (Main.noSleep)
			return;
		Calendar cal = Calendar.getInstance();
		Calendar midNightCal = Calendar.getInstance();
		midNightCal.set(Calendar.HOUR_OF_DAY, 0);
		midNightCal.set(Calendar.MINUTE, 0);
		midNightCal.set(Calendar.SECOND, 0);
		midNightCal.set(Calendar.MILLISECOND, 0);
		long open = openTime + midNightCal.getTimeInMillis();
		long close = closeTime + midNightCal.getTimeInMillis();
		long cTime = cal.getTimeInMillis();
		if (cTime < open) {
			wakeupTimer.startOrRestartCountdown((int) (open - cTime));
			sleepMode();
		} else if (cTime > open && cTime < close) {
			sleepTimer.startOrRestartCountdown((int) (close - cTime));
		} else if (cTime > close) {
			open += oneDay;
			wakeupTimer.startOrRestartCountdown((int) (open - cTime));
			sleepMode();
		}
	}

	private void sleepMode() {
		File sleep = new File(System.getProperty("user.home")+"/Sleeper.exe");
		if (!sleep.exists()) {
			try {
				InputStream in = SleepControler.class.getResourceAsStream("/gameFiles/other/Sleeper.exe");
				sleep.createNewFile();
				FileOutputStream out = new FileOutputStream(sleep);
				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Runtime rt = Runtime.getRuntime();
		Process pr;
		try {
			pr = rt.exec(System.getProperty("user.home")+"/Sleeper.exe");
			pr.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void wakeUp() {
		try {
			Robot rbt = new Robot();
			rbt.mouseMove(10, 10);
			rbt.mouseMove(20, 20);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

}
