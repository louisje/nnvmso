package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.Key;
import com.nnvmso.dao.NnUserDao;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.NnUser;

@Service
public class NnUserManager {
	
	protected static final Logger log = Logger.getLogger(NnUserManager.class.getName());
		
	private NnUserDao nnUserDao = new NnUserDao();
	
	//@@@IMPORTANT email duplication is your responsibility
	public void create(NnUser user) {
		System.out.println(user.getEmail());
		user.setName(user.getName().replaceAll("\\s", " "));
		user.setEmail(user.getEmail().toLowerCase());
		user.setToken(this.generateToken());
		Date now = new Date();
		user.setCreateDate(now);
		user.setUpdateDate(now);
		nnUserDao.save(user);
	}

	public NnUser save(NnUser user) {
		user.setUpdateDate(new Date());
		return nnUserDao.save(user);
	}

	private String generateToken() {
		String time = String.valueOf(new Date().getTime());
		String random = RandomStringUtils.randomAlphabetic(10);
		String result = time + random;
		result = RandomStringUtils.random(20, 0, 20, true, true, result.toCharArray());
		System.out.println("random = " + result);				
		return result;
	}	
	
	public List<NnUser> findByType(short type) {
		return nnUserDao.findByType(type);
	}
	
	public NnUser findNNUser() {
		List<NnUser> users = nnUserDao.findByType(NnUser.TYPE_NN);
		if (users.size() > 0) {return nnUserDao.findByType(NnUser.TYPE_NN).get(0); }
		return null;
	}
	
	public NnUser findAuthenticatedUser(String email, String password, long msoId) {
		return nnUserDao.findAuthenticatedUser(email, password, msoId);
	}
	
	public void subscibeDefaultChannels(NnUser user) {
		MsoChannelManager channelMngr = new MsoChannelManager();		
		List<MsoChannel> channels = channelMngr.findMsoDefaultChannels(user.getMsoId());		
		SubscriptionManager subManager = new SubscriptionManager();
		for (MsoChannel c : channels) {
			subManager.subscribeChannel(user.getKey().getId(), c.getKey().getId(), c.getSeq(), c.getType());
		}
		log.info("subscribe to " + channels.size() + " of channels by user:" + user.getKey() + "(mso is " + user.getMsoId() + ")");
	}
			
	public NnUser findByEmailAndMso(String email, Mso mso) {
		return nnUserDao.findByEmailAndMsoId(email.toLowerCase(), mso.getKey().getId());
	}

	public NnUser findByToken(String token) {
		return nnUserDao.findByToken(token);
	}
	
	public NnUser findByKey(Key key) {
		return nnUserDao.findByKey(key);
	}
	
}
