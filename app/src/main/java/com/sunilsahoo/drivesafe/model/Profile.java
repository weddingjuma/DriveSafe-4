package com.sunilsahoo.drivesafe.model;


import java.util.ArrayList;
import java.util.List;

import com.sunilsahoo.drivesafe.utility.Constants;
import com.sunilsahoo.drivesafe.utility.Utility;


public class Profile {

	private String emergencyNo;
	private long postUsertestAccessTime;
	private int speedRechckInterval;
    private boolean isHeadsetConnectionAllowed = false;
	private float thresholdSpeed;
	//char interval in sec
	private int charIntervalMax = 10;
	//char interval in sec
	private int charIntervalMin = 1;
	//char display time in sec
	private int charPresentTime = 3;
	//char response time in sec
	private int charResponseTime = 5;
	private boolean usertestEnable;
	private long id = Constants.EOF;
	private int initTimeInCall = 5; 
	private int passTimeInCall = 40;
    private ArrayList<DaySettings> daySettiingsList = null;

	public ArrayList<DaySettings> getDaySettings() {
		return daySettiingsList;
	}

    public void setDaySettings(ArrayList<DaySettings> daySettingsList) {
        this.daySettiingsList = daySettingsList;
    }

	public String getEmergencyNos() {
		return emergencyNo;
	}
	

	public long getPostUsertestAccessTime() {
		return postUsertestAccessTime;
	}

	public int getSpeedRechckInterval() {
		return speedRechckInterval;
	}

	public float getThresholdSpeed() {
		return thresholdSpeed;
	}
	
	//char interval in milli seconds
	public int getCharIntervalMax() {
		return charIntervalMax * 1000;
	}

	public int getCharIntervalMin() {
		return charIntervalMin * 1000;
	}

	public int getCharPresentTime() {
		return charPresentTime * 1000;
	}

	public int getInitTimeInCall() {
		return initTimeInCall;
	}

	public int gettestPassTimeInCall() {
		return passTimeInCall;
	}

	
	public boolean isTestEnable() {
		return usertestEnable;
	}

	public void setEmergencyNos(String emergencyNo) {
		this.emergencyNo = emergencyNo;
	}

	public void setPostUsertestAccessTime(long postUsertestAccessTime) {
		this.postUsertestAccessTime = postUsertestAccessTime;
	}

	public void setSpeedRechckInterval(int speedRechckInterval) {
		this.speedRechckInterval = speedRechckInterval;
	}

	public void setThresholdSpeed(float thresholdSpeed) {
		this.thresholdSpeed = thresholdSpeed;
	}


	public void setCharIntervalMax(int usertestCharIntervalMax) {
		this.charIntervalMax = usertestCharIntervalMax;
	}


	public void setCharIntervalMin(int usertestCharIntervalMin) {
		this.charIntervalMin = usertestCharIntervalMin;
	}


	public void setCharPresentTime(int charPresentTimes) {
		this.charPresentTime = charPresentTimes;
	}
	
	public void setTestEnable(boolean usertestEnable) {
		this.usertestEnable = usertestEnable;
	}

	public void setInitTimeInCall(int usertestInitTimeInCall) {
		this.initTimeInCall = usertestInitTimeInCall;
	}
	
	public void setTestPassTimeInCall(int usertestPassTimeInCall) {
		this.passTimeInCall = usertestPassTimeInCall;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getCharResponseTime() {
		return charResponseTime * 1000;
	}

	public void setCharResponseTime(int charResponseTime) {
		this.charResponseTime = charResponseTime;
	}

    public boolean isHeadsetConnectionAllowed() {
        return isHeadsetConnectionAllowed;
    }

    public void setHeadsetConnectionAllowed(boolean isHeadsetConnectionAllowed) {
        this.isHeadsetConnectionAllowed = isHeadsetConnectionAllowed;
    }
}
