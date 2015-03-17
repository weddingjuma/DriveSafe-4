package com.sunilsahoo.drivesafe.model;

import com.sunilsahoo.drivesafe.utility.Constants;

public class Report {
	private long id = Constants.EOF;
	private int reportType;
	private int reportValue;
	private long time;

	public long getId() {
		return id;
	}

	public int getReportType() {
		return reportType;
	}

	public int getReportValue() {
		return reportValue;
	}

	public long getTime() {
		return time;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setReportType(int reportType) {
		this.reportType = reportType;
	}

	public void setReportValue(int reportValue) {
		this.reportValue = reportValue;
	}

	public void setTime(long time) {
		this.time = time;
	}
}
