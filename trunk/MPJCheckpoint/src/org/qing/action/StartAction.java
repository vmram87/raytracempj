package org.qing.action;

import org.qing.action.base.BaseActionInterface;


public class StartAction extends BaseActionInterface {
	private String args;
	
	
	
	public String getArgs() {
		return args;
	}



	public void setArgs(String args) {
		this.args = args;
	}



	public String execute() throws Exception
	{
		String argv[] = args.trim().split(" ");
		mgr.startMPJRun(argv); 
		return SUCCESS;
	}
	
	public String kill() throws Exception{
		mgr.killProccesses(); 
		return SUCCESS;
	}
}
