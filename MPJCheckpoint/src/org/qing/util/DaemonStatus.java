package org.qing.util;

import java.util.List;

public class DaemonStatus {
	private String name;
	private String daemonStatus;
	private List process;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDaemonStatus() {
		return daemonStatus;
	}
	public void setDaemonStatus(String daemonStatus) {
		this.daemonStatus = daemonStatus;
	}
	public List getProcess() {
		return process;
	}
	public void setProcess(List process) {
		this.process = process;
	}
	
}


