package com.nnvmso.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.SubscriptionLog;

public class SubscriptionLogDao extends GenericDao<SubscriptionLog> {
	
	protected static final Logger logger = Logger.getLogger(SubscriptionLogDao.class.getName());
	
	public SubscriptionLogDao() {
		super(SubscriptionLog.class);
	} 
	
	public List<SubscriptionLog> findAll() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<SubscriptionLog> detached = new ArrayList<SubscriptionLog>();		
		try {
			Query query = pm.newQuery(SubscriptionLog.class);
			query.setOrdering("count desc");
			@SuppressWarnings("unchecked")
			List<SubscriptionLog> list = (List<SubscriptionLog>)query.execute();
			detached = (List<SubscriptionLog>)pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;
		
	}
	
	public SubscriptionLog save(SubscriptionLog log) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(log);
			log = pm.detachCopy(log);
		} finally {
			pm.close();
		}
		return log;
	}
	
	public SubscriptionLog findById(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		SubscriptionLog log = null;
		try {
			log = pm.getObjectById(SubscriptionLog.class, id);
			log = pm.detachCopy(log);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();			
		}
		return log;		
	}	
	
	public SubscriptionLog findByMsoIdAndChannelId(long msoId, long channelId) {
		SubscriptionLog s = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(SubscriptionLog.class);
			q.setFilter("msoId == msoIdParam && channelId== channelIdParam");
			q.declareParameters("long msoIdParam, long channelIdParam");
			@SuppressWarnings("unchecked")
			List<SubscriptionLog> subs = (List<SubscriptionLog>)q.execute(msoId, channelId);
			if (subs.size() > 0) {
				s = subs.get(0);
				s = pm.detachCopy(s);
			}
		} finally {
			pm.close();
		}
		return s;		
	}

	public SubscriptionLog findByMsoIdAndSetId(long msoId, long setId) {
		SubscriptionLog s = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(SubscriptionLog.class);
			q.setFilter("msoId == msoIdParam && setId== setIdParam");
			q.declareParameters("long msoIdParam, long setIdParam");
			@SuppressWarnings("unchecked")
			List<SubscriptionLog> subs = (List<SubscriptionLog>)q.execute(msoId, setId);
			if (subs.size() > 0) {
				s = subs.get(0);
				s = pm.detachCopy(s);
			}
		} finally {
			pm.close();
		}
		return s;		
	}
	
	public int findTotalCountByChannelId(long channelId) {
		
		int totalCount = 0;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(SubscriptionLog.class);
			q.setFilter("channelId == channelIdParam");
			q.declareParameters("long channelIdParam");
			@SuppressWarnings("unchecked")
			List<SubscriptionLog> subs = (List<SubscriptionLog>)q.execute(channelId);
			for (SubscriptionLog s : subs)
				totalCount += s.getCount();
		} finally {
			pm.close();
		}
		logger.info("subscriptionCount(" + channelId + ") = " + totalCount);
		return totalCount;
	}
	
	public SubscriptionLog findByChannelId(long channelId) {
		SubscriptionLog s = null;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(SubscriptionLog.class);
			q.setFilter("channelId == channelIdParam");
			q.declareParameters("long channelIdParam");
			@SuppressWarnings("unchecked")
			List<SubscriptionLog> subs = (List<SubscriptionLog>)q.execute(channelId);
			if (subs.size() > 0) {
				s = subs.get(0);
				s = pm.detachCopy(s);
			}
		} finally {
			pm.close();
		}
		return s;		
	}
	
	public int findCountByChannelIdAndMsoId(long channelId, long msoId) {
		
		int totalCount = 0;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query q = pm.newQuery(SubscriptionLog.class);
			q.setFilter("channelId == channelIdParam && msoId == msoIdParam");
			q.declareParameters("long channelIdParam, long msoIdParam");
			@SuppressWarnings("unchecked")
			List<SubscriptionLog> subs = (List<SubscriptionLog>)q.execute(channelId, msoId);
			for (SubscriptionLog s : subs)
				totalCount += s.getCount();
		} finally {
			pm.close();
		}
		logger.info("subscriptionCount(" + channelId + ", " + msoId + ") = " + totalCount);
		return totalCount;
	}		
	
}
