package org.qing.action.base;

import org.qing.service.ContextManager;

import com.opensymphony.xwork2.Action;

/**
 * @author  yeeku.H.lee kongyeeku@163.com
 * @version  1.0
 * <br>Copyright (C), 2005-2008, yeeku.H.Lee
 * <br>This program is protected by copyright laws.
 * <br>Program Name:
 * <br>Date: 
 */

public class BaseActionInterface implements Action
{
    protected ContextManager mgr;

    public void setMgr(ContextManager mgr)
    {
        this.mgr = mgr;
    }

	public String execute() throws Exception
	{
		return SUCCESS;
	}
}