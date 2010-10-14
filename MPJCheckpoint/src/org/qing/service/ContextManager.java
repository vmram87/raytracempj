package org.qing.service;

import java.util.List;

import org.qing.object.Context;

public interface ContextManager {
	void startMPJRun(String[] argv) throws Exception;
	void delAllPrevContextsByVersion(int versionId) throws Exception;
	void saveContext(Context c) throws Exception;
	Integer getLatestVersionId() throws Exception;
	List<Context> getContextsByVersion(int versionId) throws Exception;
	Integer getNextLatestVersionId(int versionId) throws Exception;
	void killProccesses() throws Exception;
	Integer getLatestCompleteVersion(int nprocs)throws Exception;
	List getDaemonStausList() throws Exception;
	boolean isCanStartProgram();
	
}
