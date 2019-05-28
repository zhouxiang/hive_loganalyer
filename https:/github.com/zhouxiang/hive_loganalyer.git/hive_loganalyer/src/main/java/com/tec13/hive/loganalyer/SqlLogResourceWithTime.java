package com.tec13.hive.loganalyer;

import java.util.Date;

public class SqlLogResourceWithTime {
	private Date date;
	private String taskNum;
	
	
	public SqlLogResourceWithTime(Date date, String taskNum) {
		super();
		this.date = date;
		this.taskNum = taskNum;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(String taskNum) {
		this.taskNum = taskNum;
	}
	@Override
	public String toString() {
		return "SqlLogResourceWithTime [date=" + LogAnalyzer.SFD.format(date) + ", taskNum=" + taskNum + "]";
	}
}
