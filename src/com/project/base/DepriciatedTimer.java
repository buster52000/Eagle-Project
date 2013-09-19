package com.project.base;

import java.util.Calendar;

public class DepriciatedTimer {

	private long timeStarted = 0;
	
	public void startTimer() {
		timeStarted = Calendar.getInstance().getTimeInMillis();
	}

	public void stopTimer() {
		timeStarted = 0;
	}

	public int getTime() {
		if (timeStarted == 0)
			Main.errMsg("Attempted to call getTimer when Timer was not started. " + this.toString(), false);
		else
			return (int) ((int) (Calendar.getInstance().getTimeInMillis() - timeStarted) / 1000);
		return 0;
	}

}
