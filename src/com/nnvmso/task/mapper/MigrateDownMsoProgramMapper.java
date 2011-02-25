package com.nnvmso.task.mapper;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

public class MigrateDownMsoProgramMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
		
	@Override
	public void map(Key key, Entity value, Context context) {
		value.removeProperty("pubDate");		
		value.removeProperty("status");
		DatastoreMutationPool mutationPool = this.getAppEngineContext(context).getMutationPool();
		mutationPool.put(value);
	}
}
