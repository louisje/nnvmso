package com.nnvmso.dao;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.nnvmso.lib.PMF;
import com.nnvmso.model.ChannelSetChannel;

public class ChannelSetChannelDao extends GenericDao<ChannelSetChannel> {
	
	protected static final Logger logger = Logger.getLogger(ChannelSetChannel.class.getName());
	
	public ChannelSetChannelDao() {
		super(ChannelSetChannel.class);
	}
	
	public List<ChannelSetChannel> findByChannelSetId(long channelSetId) {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ChannelSetChannel> detached = new ArrayList<ChannelSetChannel>();
		
		try {
			Query query = pm.newQuery(ChannelSetChannel.class);
			query.setFilter("ChannelSetId == ChannelSetIdParam");
			query.declareParameters("long ChannelSetIdParam");
			@SuppressWarnings("unchecked")
			List<ChannelSetChannel> list = (List<ChannelSetChannel>)query.execute(channelSetId);
			detached = (List<ChannelSetChannel>)pm.detachCopyAll(list);
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
		return detached;
	}
}