package com.sunilsahoo.drivesafe.model;

import com.sunilsahoo.drivesafe.utility.Constants;

public class DaySettings {
	private long id = Constants.EOF;


	private String day;
	private boolean enabled;

	private long startTime;


	private long stopTime;
	public String getDay() {
		return day;
	}
	public long getStartTime() {
		return startTime;
	}
	public long getStopTime() {
		return stopTime;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	

}
