package org.qing.util;

import java.util.List;

public class SystemConfig {
	private String runType;
	private String runFile;
	private Integer nproc;
	private List outputFile;
	public String getRunType() {
		return runType;
	}
	public void setRunType(String runType) {
		this.runType = runType;
	}
	public String getRunFile() {
		return runFile;
	}
	public void setRunFile(String runFile) {
		this.runFile = runFile;
	}
	public Integer getNproc() {
		return nproc;
	}
	public void setNproc(Integer nproc) {
		this.nproc = nproc;
	}
	public List getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(List outputFile) {
		this.outputFile = outputFile;
	}
	
	
}
