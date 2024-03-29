package org.qing.service.impl;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

import org.qing.dao.ContextDao;
import org.qing.factory.ClientFactory;
import org.qing.object.Context;
import org.qing.service.ContextManager;

import runtime.starter.MPJRun;

public class ContextManagerImpl implements ContextManager {
	private ContextDao contextDao;
	private Thread MPJRunThreadStarter = null;

	public void setContextDao(ContextDao contextDao) {
		this.contextDao = contextDao;
	}


	@Override
	public void startMPJRun(String[] argv) throws Exception {
		ClientFactory.initClient(argv);
		MPJRunThreadStarter = new Thread(MPJRunThread);
		MPJRunThreadStarter.start();
		
	}
	

	@Override
	public boolean isCanStartProgram() {
		// TODO Auto-generated method stub
		return ClientFactory.isCanStarted();
	}


	@Override
	public List getDaemonStausList() throws Exception {
		MPJRun client = ClientFactory.getClient();
		if(client == null)
			return null;
		else
			return client.getDaemonStatus();
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


	@Override
	public void killProccesses() throws Exception {
		Integer ver = getLatestVersionId();
		if(ver != null){
			delAllPrevContextsByVersion(getLatestVersionId()+1);
		}
		try{
			ClientFactory.getClient().killProccesses();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}


	Runnable MPJRunThread = new Runnable() {
		
		
		@Override
		public void run() {
			try {
				Timestamp startTime = new Timestamp(System.currentTimeMillis());
				System.out.println("Start Time: " + startTime);
				ClientFactory.getClient().start();
				System.out.println("Start Time: " + startTime);
				System.out.println("End Time: " + new Timestamp(System.currentTimeMillis()));
			} catch (Exception e) {
				e.printStackTrace();
			}		
			ClientFactory.setCanStart(true);
			System.out.println("Finish MPJRun!");
		}
	};

	
	@Override
	public Integer getLatestCompleteVersion(int nprocs) throws Exception {
		Integer ver = getLatestVersionId();
		if(ver == null)
			return null;
		
		List<Context> contextList = getContextsByVersion(ver);
		while(contextList.size()!=nprocs){
			ver = getNextLatestVersionId(ver);
			if(ver == null)
				return null;
			contextList = getContextsByVersion(ver);				
		}
		return ver;
	}


	@Override
	public void restart() throws Exception {
		if(ClientFactory.isCanStarted() == false){
			ClientFactory.getClient().restart();
		}
		else
			throw new Exception("Not started, please first started!");
		
	}


	
}
