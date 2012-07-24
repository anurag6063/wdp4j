package com.hiromsoft.types.translator;

import java.util.Map;

public class FloatTranslator extends BaseTranslator{
	
	protected static  BaseTranslator instance=null;
	
	public static BaseTranslator getInstance(){
		if(instance==null)
			instance=new FloatTranslator();
		return instance;
	}
	public Object Translate(Map datas,String propName){
		Object value = datas.get(propName);
		if(value==null) return null;
		if(value instanceof Float) return value;
		String strvalue = value.toString();
		if(strvalue.indexOf("${")!=-1) return null;
        if (strvalue.length() == 0){
        	value=new Float("0");
    	    datas.put(propName,value);
            return null;
        } 
        try {
        	value=new Float(strvalue);
        	datas.put(propName,value);
        	return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return value;
	}
	
}
