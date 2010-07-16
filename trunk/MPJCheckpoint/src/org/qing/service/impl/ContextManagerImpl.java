package org.qing.service.impl;

import org.qing.dao.ContextDao;
import org.qing.service.ContextManager;

public class ContextManagerImpl implements ContextManager {
	private ContextDao contextDao;
	

	public void setContextDao(ContextDao contextDao) {
		this.contextDao = contextDao;
	}


	@Override
	public void startMPJRun(String[] argv) throws Exception {
		// TODO Auto-generated method stub

	}

}
