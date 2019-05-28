package com.tec13.hive.loganalyer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class LogAnalyzer {
	private static final String LOG_DIR = "/Users/zhouxiang/develop/test/kehutong/logs_0527";
	public static final Logger LOG = Logger.getLogger(LogAnalyzer.class);

	private static final String NEW_SQL_BEGIN = "INFO  : Executing command";
	private static final String NEW_SQL_TOTAL_TIME = "INFO  : Completed executing command";
	public static final SimpleDateFormat SFD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	private static final Charset CHARSET_UTF8 = Charset.forName("utf-8");
	private List<SqlFile> sqlFileList;
	
	public LogAnalyzer() {
		sqlFileList = new ArrayList<SqlFile>();
	}
	private void parseLog(String logDir) throws Exception {
		File[] files = new File(logDir).listFiles();
		sqlFileList = new ArrayList<SqlFile>();
		for (File file : files) {
			SqlFile sqlFile = new SqlFile(file.getName());
			List<String> lines = FileUtils.readLines(file,CHARSET_UTF8);
			SqlLog currentSqlLog = null;
			boolean sqlBegin = false;
			for (String line : lines) {
				if(sqlBegin&&line.startsWith("INFO  :")) {
					sqlBegin = false;
				}
				if(sqlBegin) {
					currentSqlLog.appendSql(line);
				}
				
				if (line.startsWith(NEW_SQL_BEGIN)) {
					String sql = line.substring(line.indexOf(":") + 1);
					if (currentSqlLog != null) {
						sqlFile.addLog(currentSqlLog);
					}
					currentSqlLog = new SqlLog();
					currentSqlLog.setSql(sql);
					sqlBegin =true;
				}

				// 获取任务执行总时间
				if (line.startsWith(NEW_SQL_TOTAL_TIME)) {
					Matcher m = match("(\\d+\\.\\d+)", line);
					if (m.find()) {
						try {
							currentSqlLog.setTotalTime(Float.valueOf(m.group(1)));
						} catch (Exception e) {
							LOG.error(e);
						}
					}
				}
				
				
				//收集任务的时间和资源使用情况
				Matcher m = match("INFO  : (\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2},\\d+)", line);
				if(m.find()) {
					String time = m.group(1);
					Matcher im = match("\\s(\\d+\\(\\+(\\d+)\\)/\\d+)\\s", line);
					int totalExTask = 0;
					while (im.find()) {
							int taskEx = Integer.valueOf(im.group(2));
							totalExTask += taskEx;
					}
					currentSqlLog.addSqlLogResourceWithTime(new SqlLogResourceWithTime(	SFD.parse(time), totalExTask+""));
				}
			}
			if(currentSqlLog != null) {
				sqlFile.addLog(currentSqlLog);
			}
			sqlFileList.add(sqlFile);
		}

	}
	
	public static void main(String[] args) throws Exception {
		String logDir = "/Users/zhouxiang/develop/test/kehutong/logs_0527";
		LogAnalyzer logAnalyzer = new LogAnalyzer();
		logAnalyzer.parseLog(logDir);
		
		LOG.debug("=============ANALYSE  OUTPUT===============");
		String tempPath = System.getProperty("java.io.tmpdir");
		logAnalyzer.taskNumByTimeReport(new File(tempPath+"/test.xlsx"));
	}

	private static final float SLOW_SQL_TIME_IN_SECONDS_MAX = 600;
	private static final float SLOW_SQL_TIME_IN_SECONDS_MIN = 120;

	public static void findSlowSql(List<SqlFile> sqlFileList) {
		for (SqlFile file : sqlFileList) {
			for (SqlLog log : file.getLogList()) {
				if (log.getTotalTime() >= SLOW_SQL_TIME_IN_SECONDS_MIN && log.getTotalTime() <= SLOW_SQL_TIME_IN_SECONDS_MAX) {
					LOG.debug(file.getFileName());
					LOG.debug(log.toString());
				}
			}
		}
	}
	public static void exportResourceWithTime(List<SqlFile> sqlFileList) throws IOException {
		FileUtils.forceDelete(new File(LOG_DIR+"/../output_filename.txt"));
		FileUtils.forceDelete(new File(LOG_DIR+"/../output_datetime.txt"));
		FileUtils.forceDelete(new File(LOG_DIR+"/../output_tasknum.txt"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		for (SqlFile file : sqlFileList) {
			for (SqlLog log : file.getLogList()) {
				for(SqlLogResourceWithTime rt : log.getTimeResourceList()) {
//					LOG.debug(file.getFileName()+","+SFD.format(rt.getDate())+","+rt.getTaskNum());
					FileUtils.write(new File(LOG_DIR+"/../output_filename.txt"), file.getFileName()+"\n",true);
					FileUtils.write(new File(LOG_DIR+"/../output_datetime.txt"), sdf.format(rt.getDate())+"\n",true);
					FileUtils.write(new File(LOG_DIR+"/../output_tasknum.txt"), rt.getTaskNum()+"\n",true);
				}
			}
		}
	}
	
	public void taskNumByTimeReport(File outputFile) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Map<String, Integer> map1 = new HashMap<String, Integer> () ;
		
		for (SqlFile file : sqlFileList) {
			for (SqlLog log : file.getLogList()) {
				for(SqlLogResourceWithTime rt : log.getTimeResourceList()) {
					Integer num = map1.get(sdf.format(rt.getDate())+"_"+file.getFileName());
					if(num == null) {
						num = 0;
					}
					num = Math.max(num,Integer.valueOf(rt.getTaskNum()));
					map1.put(sdf.format(rt.getDate())+"_"+file.getFileName(),num);
				}
			}
		}
		
		Map<String, Integer> map = new HashMap<String, Integer> () ;
		for(Entry<String, Integer> e:map1.entrySet()) {
			String tm = e.getKey().split("_")[0];
			Integer num = map.get(tm);
			if(num == null) {
				num = 0;
			}
			num += e.getValue();
			map.put(tm,num);
		}
		
		List<SqlLogResourceWithTime> l = new ArrayList<>();
		for(Entry<String, Integer> e:map.entrySet()) {
			l.add(new SqlLogResourceWithTime((sdf.parse(e.getKey())), e.getValue()+""));
		}
		
		Collections.sort(l, new Comparator<SqlLogResourceWithTime>() {

			@Override
			public int compare(SqlLogResourceWithTime o1, SqlLogResourceWithTime o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		});
		File templateReport = new File(this.getClass().getResource("/analyse_tasknumbytime_report.xlsx").getPath());
//		FileUtils.copyFile(templateReport, outputFile);
		
		Workbook xlsxBook = WorkbookFactory.create(templateReport);
		Sheet reportSheet = xlsxBook.getSheet("report");
		
		int i=1;
		for(SqlLogResourceWithTime srwt: l) {
			Row row = reportSheet.createRow(i++);
			row.createCell(0).setCellValue(sdf.format(srwt.getDate()));
			row.createCell(1).setCellValue(Integer.valueOf(srwt.getTaskNum()));
		}
	 try (OutputStream fileOut = new FileOutputStream(outputFile)) {
		 xlsxBook.write(fileOut);
        }
		xlsxBook.close();
		
	}
	public static void sumTaskNumByTimeWithFileName(List<SqlFile> sqlFileList) throws IOException, ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Map<String, Integer> map1 = new HashMap<String, Integer> () ;
		FileUtils.forceDelete(new File(LOG_DIR+"/../output_datetime.txt"));
		FileUtils.forceDelete(new File(LOG_DIR+"/../output_tasknumwithfilename.txt"));
		for (SqlFile file : sqlFileList) {
			for (SqlLog log : file.getLogList()) {
				for(SqlLogResourceWithTime rt : log.getTimeResourceList()) {
					Integer num = map1.get(sdf.format(rt.getDate())+"_"+file.getFileName());
					if(num == null) {
						num = 0;
					}
					num = Math.max(num,Integer.valueOf(rt.getTaskNum()));
					map1.put(sdf.format(rt.getDate())+"_"+file.getFileName(),num);
				}
			}
		}
		
		Map<String, List<ImmutablePair<String, Integer>>> map = new HashMap<String, List<ImmutablePair<String, Integer>>> () ;
		for(Entry<String, Integer> e:map1.entrySet()) {
			String tm = e.getKey().split("_")[0];
			String fileName =  e.getKey().substring(e.getKey().indexOf("_")+1);
			ImmutablePair<String, Integer> pair = new ImmutablePair<String, Integer>(fileName, e.getValue());
			
			List<ImmutablePair<String, Integer>> pairList = map.get(tm);
			if(pairList == null) {
				pairList = new ArrayList<>();
				map.put(tm,pairList);
			}
			pairList.add(pair);
			
		}
		
		List<SqlLogResourceWithTime> l = new ArrayList<>();
		for(Entry<String, List<ImmutablePair<String, Integer>>> e:map.entrySet()) {
			StringBuilder sb = new StringBuilder();
			for(ImmutablePair<String, Integer> pair : e.getValue()) {
				sb.append(pair.getKey()+"("+pair.getValue()+") ");
			}
			l.add(new SqlLogResourceWithTime((sdf.parse(e.getKey())), sb.toString()));
		}
		
		Collections.sort(l, new Comparator<SqlLogResourceWithTime>() {

			@Override
			public int compare(SqlLogResourceWithTime o1, SqlLogResourceWithTime o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		});
		for(SqlLogResourceWithTime srwt: l) {
			FileUtils.write(new File(LOG_DIR+"/../output_datetime.txt"),sdf.format(srwt.getDate())+"\n",true);
			FileUtils.write(new File(LOG_DIR+"/../output_tasknumwithfilename.txt"), srwt.getTaskNum()+"\n",true);
		}
	}


	public static Matcher match(String pattern, String line) {
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(line);
		return m;
	}
	public List<SqlFile> getSqlFileList() {
		return sqlFileList;
	}
}
