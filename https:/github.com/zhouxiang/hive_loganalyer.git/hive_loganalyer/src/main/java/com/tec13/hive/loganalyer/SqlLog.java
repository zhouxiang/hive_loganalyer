package com.tec13.hive.loganalyer;

import java.util.ArrayList;
import java.util.List;

public class SqlLog {
	private String sql;
	private float totalTime;
	private List<SqlLogResourceWithTime> timeResourceList;
	
	public SqlLog() {
		timeResourceList = new ArrayList<SqlLogResourceWithTime>();
	}

	public void addSqlLogResourceWithTime(SqlLogResourceWithTime t) {
		timeResourceList.add(t);
	}
	
	public String getSql() {
		return sql;
	}
	public void appendSql(String sql) {
		this.sql += "\n "+sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}

	public float getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(float totalTime) {
		this.totalTime = totalTime;
	}

	public List<SqlLogResourceWithTime> getTimeResourceList() {
		return timeResourceList;
	}
	public void setTimeResourceList(List<SqlLogResourceWithTime> timeResourceList) {
		this.timeResourceList = timeResourceList;
	}

	@Override
	public String toString() {
		return "SqlLog [sql=" + sql + ", totalTime=" + totalTime + ", timeResourceList=" + timeResourceList + "]";
	}

}
