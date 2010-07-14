package org.qing.factory;

import org.qing.dao.ContextDao;
import org.qing.dao.impl.ContextDaoImpl;

public class ContextFactory {
	private static ContextDao contextDao = new ContextDaoImpl();
	
	private ContextFactory(){}
	
	public static ContextDao getContextDao(){
		return contextDao;
	}
}
