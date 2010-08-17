package org.qing.action.base;

import org.qing.service.ContextManager;
import org.qing.service.FileManager;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author  yeeku.H.lee kongyeeku@163.com
 * @version  1.0
 * <br>Copyright (C), 2005-2008, yeeku.H.Lee
 * <br>This program is protected by copyright laws.
 * <br>Program Name:
 * <br>Date: 
 */

public class BaseAction extends ActionSupport
{
	protected ContextManager mgr;
    protected FileManager fileMgr;

    public void setMgr(ContextManager mgr)
    {
        this.mgr = mgr;
    }

	public void setFileMgr(FileManager fileMgr) {
		this.fileMgr = fileMgr;
	}
}