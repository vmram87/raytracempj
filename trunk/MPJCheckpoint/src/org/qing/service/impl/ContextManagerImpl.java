package org.qing.service.impl;

import java.io.File;
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
	public void delAllPrevContextsByVersion(int versionId) throws Exception{
		List<Context> ul = contextDao.getAllPrevContextsByVersion(versionId);
		File file = null;
		for( Context c : ul){
			contextDao.delete(c);
			
			file = new File(c.getContextFilePath());
			if(file.exists())
				file.delete();
			file = new File(c.getTempFilePath());
			if(file.exists())
				file.delete();			
		}
	}
	
	
	@Override
	public List<Context> getContextsByVersion(int versionId) throws Exception {
		return contextDao.getContextsByVersion(versionId);
	}


	@Override
	public Integer getLatestVersionId() throws Exception {
		return contextDao.getLatestVersionId();
	}


	@Override
	public void saveContext(Context c) throws Exception {
		contextDao.save(c);
	}


	@Override
	public Integer getNextLatestVersionId(int versionId) throws Exception {
		return contextDao.getNextLatestVersionId(versionId);
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
