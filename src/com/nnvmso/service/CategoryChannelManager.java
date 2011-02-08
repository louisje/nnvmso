package com.nnvmso.service;

import java.util.Date;
import java.util.List;

import com.nnvmso.dao.CategoryChannelDao;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;

public class CategoryChannelManager {
	
	private CategoryChannelDao ccDao = new CategoryChannelDao();
	
	public void create(CategoryChannel cc) {
		Date now = new Date();
		cc.setCreateDate(now);
		cc.setUpdateDate(now);
		ccDao.save(cc);
	}	

	public void delete(CategoryChannel cc) {
		CategoryManager categoryMngr = new CategoryManager();
		Category category = categoryMngr.findById(cc.getCategoryId()); 
		ccDao.delete(cc);
		category.setChannelCount(category.getChannelCount()-1);
		categoryMngr.save(category);
	}
	
	public List<CategoryChannel> findAllByCategoryId(long categoryId) {
		List<CategoryChannel> ccs = ccDao.findAllByCategoryId(categoryId);
		System.out.println("findByCategoryKey(): found " + ccs.size() + " with id " + categoryId);
		return ccs;
	}
	
	public List<CategoryChannel> findAllByChannelId(long channelId) {
		List<CategoryChannel> ccs = ccDao.findAllByChannelId(channelId);
		return ccs;
	}

	public CategoryChannel findByCategoryIdAndChanelId(long categoryId, long channelId) {
		return ccDao.findByCategoryIdAndChannelId(categoryId, channelId);
	}
	
	public CategoryChannel findById(long id) {
		return ccDao.findById(id);
	}
}
