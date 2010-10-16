package org.qing.action;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.qing.action.base.BaseActionInterface;
import org.qing.object.MyFile;
import org.qing.util.SystemConfig;


public class StartAction extends BaseActionInterface {
	private String args;
	private MyFile libFolder;
	private String tip;
	
	
	public String getArgs() {
		return args;
	}



	public void setArgs(String args) {
		this.args = args;
	}



	public String getTip() {
		return tip;
	}



	public String execute() throws Exception
	{
		if(mgr.isCanStartProgram() == false){
			tip = "Started";
			return SUCCESS;
		}
		
		SystemConfig config = fileMgr.getConfig();
		String str = "";
		str = str + " -np " + config.getNproc();
		str = str + " -dev niodev ";

		libFolder = fileMgr.getUserLib();
		MyFile lib = null;
		List libs = fileMgr.getFilesByDirectory(libFolder.getId());
		if(libs != null && libs.size() > 0){
			str = str + " -cp ";
			for(Object o : libs){
				lib = (MyFile) o;
				str = str + fileMgr.getDestPath(lib.getId()) + File.pathSeparator;
			}
			str = str.substring(0, str.length()-File.pathSeparator.length());
		}
		
		
		if(config.getRunType().equals("Class")){
			str = str + " " +config.getRunFile();
		}else{
			str = str + " -jar " + config.getRunFile();
		}
		
		String argv[] = str.trim().split("\\s+");
//		try{
			mgr.startMPJRun(argv);
//		}catch(Exception e){
//			tip = "Error: " + e.getMessage();
//		}
		return SUCCESS;
	}
	
	public String stop() throws Exception{
		mgr.killProccesses(); 
		return SUCCESS;
	}
}
