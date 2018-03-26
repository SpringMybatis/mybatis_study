package com.anhouse.dmapi.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {

	public static class GsonHolder {
		
		private static final Gson INSTANCE = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
			// 过滤掉字段名包含"id","address"的字段  
			public boolean shouldSkipField(FieldAttributes f) {
				List<String> attrs = new ArrayList<String>();
				attrs.add("createUser");
				attrs.add("createTime");
				attrs.add("updateUser");
				attrs.add("updateTime");
				attrs.add("memo");
				attrs.add("maintainTypeView");
				return attrs.contains(f.getName());
			}
			
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
			
		}).create();
	}
	
	public static Gson getGsonInstance() {
		return GsonHolder.INSTANCE;
	}
	
}
