package org.qing.action;

import java.util.List;

import org.qing.action.base.BaseActionInterface;

public class GraphicViewAction extends BaseActionInterface {
	private List nodeList;
	private Integer interval;
	

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public List getNodeList() {
		return nodeList;
	}

	@Override
	public String execute() throws Exception {
		nodeList = mgr.getDaemonStausList();
		return SUCCESS;
	}
	
	public String getNodesInfo() throws Exception{
		nodeList = mgr.getDaemonStausList();
		//System.out.println(interval);
		return SUCCESS;
	}
	
	
	
}
