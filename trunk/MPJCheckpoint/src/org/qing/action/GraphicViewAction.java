package org.qing.action;

import java.util.List;

import org.qing.action.base.BaseActionInterface;
import org.qing.factory.ClientFactory;

public class GraphicViewAction extends BaseActionInterface {
	private List nodeList;
	private Integer interval;
	private List outputFile;

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public List getNodeList() {
		return nodeList;
	}

	public List getOutputFile() {
		return outputFile;
	}

	@Override
	public String execute() throws Exception {
		nodeList = mgr.getDaemonStausList();
		return SUCCESS;
	}
	
	public String getNodesInfo() throws Exception{
		if(!ClientFactory.isCanStarted()){
			nodeList = mgr.getDaemonStausList();
		}else{
			outputFile = fileMgr.getOutputFiles();
		}
		//System.out.println(interval);
		return SUCCESS;
	}
	
	
	
}
