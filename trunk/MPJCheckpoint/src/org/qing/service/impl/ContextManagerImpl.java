package org.qing.service.impl;

import java.util.List;

import org.qing.dao.ContextDao;
import org.qing.object.Context;
import org.qing.service.ContextManager;

import runtime.starter.MPJRun;

public class ContextManagerImpl implements ContextManager {
	private ContextDao contextDao;
	private MPJRun client;
	private Thread MPJRunThreadStarter = null;

	public void setContextDao(ContextDao contextDao) {
		this.contextDao = contextDao;
	}


	@Override
	public void startMPJRun(String[] argv) throws Exception {
		client = new MPJRun(argv);
		MPJRunThreadStarter = new Thread(MPJRunThread);
		MPJRunThreadStarter.start();
	}

	@Override
	public void delAllPrevContextsByVersion(int versionId) {
		List<Context> ul = contextDao.getAllPrevContextsByVersion(versionId);
		for( Context c : ul){
			contextDao.delete(c);
		}
	}
	
	Runnable MPJRunThread = new Runnable() {
		
		@Override
		public void run() {
			try {
				client.start();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	};
}
