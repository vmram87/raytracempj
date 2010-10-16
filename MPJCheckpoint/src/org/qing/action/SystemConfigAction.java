package org.qing.action;

import java.util.List;

import org.qing.action.base.BaseAction;
import org.qing.factory.ClientFactory;
import org.qing.util.SystemConfig;

public class SystemConfigAction extends BaseAction {
	private String runType;
	private String runFile;
	private Integer nproc;
	private List outputFile;
	private String tip;
	
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

	public String sysConfig() throws Exception{
		SystemConfig config = fileMgr.getConfig();
		runType = config.getRunType();
		runFile = config.getRunFile();
		nproc = config.getNproc();
		outputFile = config.getOutputFile();
		return SUCCESS;
	}
	
	public String userGuide(){
		
		return SUCCESS;
	}
	
	public String getTip() {
		return tip;
	}

	public String saveConfig() throws Exception {
		if(ClientFactory.isCanStarted()){
			SystemConfig config = new SystemConfig();
			config.setRunType(runType);
			config.setRunFile(runFile);
			config.setNproc(nproc);
			config.setNumOfFile(outputFile.size());
			config.setOutputFile(outputFile);
			fileMgr.saveConfig(config);
			tip="Configuration Saved!";
		}
		else{
			tip="The program is running, you can't save the configuration until the program ends.";
		}
		return SUCCESS;
	}
}
