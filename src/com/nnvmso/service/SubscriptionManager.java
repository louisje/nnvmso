package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.Subscription;

@Service
public class SubscriptionManager {
	public List<Subscription> findAll(NnUser user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == userKeyParam");
		q.declareParameters(Key.class.getName() + " userKeyParam");
		@SuppressWarnings("unchecked")
		List<Subscription> subscriptions = (List<Subscription>)q.execute(user.getKey());
		subscriptions.size();//touch
		pm.close();
		return subscriptions;
	}	
	
	public List<MsoProgram> findSubscribedPrograms(NnUser user) {
		List<Subscription> subscriptions = this.findAll(user);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoProgram.class); 		
		PMF.get().getPersistenceManager();
		Key[] channelKeys = new Key[subscriptions.size()];
		int i = 0;
		for (Subscription s : subscriptions) { 
			channelKeys[i] = s.getChannelKey();
			i++;
		}
		
		List<MsoProgram> results = new ArrayList<MsoProgram>(); 		
		q = pm.newQuery(MsoProgram.class, ":p.contains(channelKey)");
		//q.setRange(0, 1);
		@SuppressWarnings("unchecked")		
		List<MsoProgram> programs = (List<MsoProgram>) q.execute(Arrays.asList(channelKeys));
		results.addAll(programs);
		Iterator<MsoProgram> iter = results.iterator();
		while(iter.hasNext()) {
		  MsoProgram p = iter.next();
		  if (!p.isPublic()) {
			  iter.remove();
		  }
		}
		pm.close();
		return results;
		//return programs;
	}
		
	public List<MsoChannel> findSubscribedChannels(NnUser user) {
		Cache cache = null;		
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            // ...
        }        

		List<Subscription> subscriptions = this.findAll(user);
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		for (Subscription s : subscriptions) {
			try {								
				MsoChannel c = (MsoChannel)cache.get(s.getChannelKey());				
				if (c == null) {				
					c = pm.getObjectById(MsoChannel.class, s.getChannelKey());
					cache.put(c.getKey(), c);				
				} else {
					System.out.println("Found from cache(" + c.getKey().getId() +"):" + c.getName());
				}
				c.setGrid(s.getSeq());
				channels.add(c);
			} catch (JDOObjectNotFoundException e) {
			}			
		}
		pm.close();
		return channels;		
	}
		
	public void channelSubscribe(NnUser user, MsoChannel channel, short grid) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Subscription s = new Subscription(user.getKey(), channel.getKey());
		s.setSeq(grid);
		System.out.println("channelSubscribe() : userKey=" + user.getKey() + "; channelKey=" + channel.getKey() + "; grid=" + grid);
		pm.makePersistent(s);
		pm.close();		
	}
	
	public void channelUnsubscribe(NnUser user, MsoChannel channel) {
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == userKeyParam && channelKey == channelKeyParam");
		q.declareParameters(Key.class.getName() + " userKeyParam, " + Key.class.getName() + " channelKeyParam");
		@SuppressWarnings("unchecked")
		List<Subscription> s = (List<Subscription>)q.execute(user.getKey(), channel.getKey());
		if (s != null && s.size() > 0) {
			pm.deletePersistent(s.get(0));
		}
		pm.close();				
	}
	
	public void msoSubscribe(NnUser user) {
		List<Subscription> subscriptions = this.findAll(user);
		
		//retrieve the mso's channels
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(MsoChannel.class);
		q.setFilter("msoKey==msoKeyParam");
		q.declareParameters(Key.class.getName() + " msoKeyParam");
		q.setOrdering("seq asc");
		@SuppressWarnings("unchecked")
		List<MsoChannel> channels = (List<MsoChannel>)q.execute(user.getMsoKey());
		//ensure user channel's uniqueness
		Set<Key> sUniqueSet = new HashSet<Key>();				
		for (Subscription s : subscriptions) {
			sUniqueSet.add(s.getChannelKey());
		}
		//add new channels 
		short seq = 1;		
		List<Subscription> newSubscriptions = new ArrayList<Subscription>();
		for (MsoChannel c : channels) {
			if (c.isPublic() && !sUniqueSet.contains(c.getKey())) {				
				Subscription s = new Subscription(user.getKey(), c.getKey());
				s.setSeq(seq);
				newSubscriptions.add(s);
			}
			seq++;
		}
		if (newSubscriptions.size() > 0) {
			pm.makePersistentAll(newSubscriptions);
		}
		pm.close();		
	}
	
	
}
