package com.tec13.hive.loganalyer;

import java.util.ArrayList;
import java.util.List;

public class SqlFile {
	private String fileName;
	private List<SqlLog> logList;
	public SqlFile(String fileName) {
		super();
		this.fileName = fileName;
		this.logList = new ArrayList<SqlLog>();
	}
	public void addLog(SqlLog log) {
		this.logList.add(log);
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public List<SqlLog> getLogList() {
		return logList;
	}
	public void setLogList(List<SqlLog> logList) {
		this.logList = logList;
	}
	@Override
	public String toString() {
		return "SqlFile [fileName=" + fileName + ", logList=" + logList + "]";
	}
	
}
