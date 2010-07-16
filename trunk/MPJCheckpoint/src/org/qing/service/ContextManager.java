package org.qing.service;

public interface ContextManager {
	void startMPJRun(String[] argv) throws Exception;
	void delAllPrevContextsByVersion(int versionId);
}
