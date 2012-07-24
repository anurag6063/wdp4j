package com.hiromsoft.types.translator;

import java.util.Map;

public class IntegerTranslator extends BaseTranslator{
	
	protected static  BaseTranslator instance=null;
	
	public static BaseTranslator getInstance(){
		if(instance==null)
			instance=new IntegerTranslator();
		return instance;
	}
	
	public Object Translate(Map datas,String propName){
		Object value = datas.get(propName);
		if(value==null) return null;
		if(value instanceof Integer) return value;
		String strvalue = value.toString();
		if(strvalue.indexOf("${")!=-1) return null;
        if (strvalue.length() == 0){
        	//value=new Integer("0");
    	    datas.put(propName,null);
            return null;
        } 
        try {
        	int idx=strvalue.indexOf(".");
        	if(idx!=-1)
        		strvalue=strvalue.substring(0, idx);
        	value=new Integer(strvalue);
        	datas.put(propName,value);
        	return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return value;
	}
	
}
