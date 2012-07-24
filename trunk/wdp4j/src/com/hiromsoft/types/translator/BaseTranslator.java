package com.hiromsoft.types.translator;

import java.util.Map;

public class BaseTranslator {
	
	
	
	public static BaseTranslator getInstance(){
		return null;
	}
	
	public Object Translate(Map datas,String propName){
		Object value = datas.get(propName);
		if(value==null) return null;
		String strvalue = value.toString();
		if(strvalue.indexOf("${")!=-1) return null;
		return value;
	}
}
