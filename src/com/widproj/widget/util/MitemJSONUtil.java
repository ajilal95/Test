package com.widproj.widget.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;

import com.widproj.widget.MultiItemSelector;
import com.widproj.widget.MultiItemSelector.MItem;
import com.widproj.widget.data.LiteMItem;
import com.widproj.widget.data.LiteMItemWraperForJsonUtil;

public class MitemJSONUtil {
	
	public static List<LiteMItem> getLiteMItemList(MultiItemSelector multiItemSelector){
		List<LiteMItem> liteMItemList = null;
		if(multiItemSelector != null){
			Map<Object, MItem> dataSource = multiItemSelector.getComponentDataSource();
			liteMItemList = getLiteMItemList(dataSource);
		}
		return liteMItemList;
	}
	
	public static List<LiteMItem> getLiteMItemList(Map<Object, MItem> dataSource){
		List<LiteMItem> liteMItemList = new LinkedList<LiteMItem>();
		if(dataSource != null && !dataSource.isEmpty()){
			for(Entry<Object, MItem> entry : dataSource.entrySet()){
				if(entry.getValue() == null){
					continue;
				}
				
				LiteMItem liteMitem = new LiteMItem();
				liteMitem.setCaption(entry.getValue().getCaption());
				liteMitem.setIsSelected(entry.getValue().isSelected());
				liteMitem.setChildJSON(getJSON(entry.getValue().getChildDataSource()));
				liteMItemList.add(liteMitem);
			}
		}
		return liteMItemList;
	}
	
	public static String getJSON(List<LiteMItem> liteMItemList){
		LiteMItemWraperForJsonUtil wraper = new LiteMItemWraperForJsonUtil();
		wraper.setLiteMItemList(liteMItemList);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(wraper);
		} catch (IOException e) {
			e.printStackTrace();
			return "{}";
		}
	}
	
	public static String getJSON(Map<Object, MItem> dataSource){
		return getJSON(getLiteMItemList(dataSource));
	}
	
	public static String getJSON(MultiItemSelector multiItemSelector){
		return getJSON(multiItemSelector.getComponentDataSource());
	}
	
	public static List<LiteMItem> getLiteMitemList(String json){
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LiteMItemWraperForJsonUtil wraper =  objectMapper.readValue(json, LiteMItemWraperForJsonUtil.class);
			return wraper.getLiteMItemList();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Map<Object, MItem> populateMultiItemSelector(MultiItemSelector component, String json){
		if(json == null 
				|| json.isEmpty() 
				|| component == null){
			return null;
		}
		
		List<LiteMItem> liteMitems = getLiteMitemList(json);
		if(liteMitems != null){
			component.clear();
			for(LiteMItem liteMitem : liteMitems){
				MItem mitem = component.addMItem(liteMitem.getCaption());
				mitem.setSelected(liteMitem.getIsSelected());
				mitem.setChildDataSource(populateMultiItemSelector(component.getChildMultiItemSelector(), liteMitem.getChildJSON()));
			}
		}
		
		return component.getComponentDataSource();
	}
	
}
