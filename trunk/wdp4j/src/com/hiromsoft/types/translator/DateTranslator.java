package com.hiromsoft.types.translator;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DateTranslator extends BaseTranslator{
	
	protected static  BaseTranslator instance=null;
	
	protected SimpleDateFormat sdf =null;
	protected static String lock="lock";
	
	public DateTranslator(){
		 sdf=new SimpleDateFormat("yyyy-MM-dd");
		 sdf.setDateFormatSymbols(new DateFormatSymbols(Locale.CHINESE));
	}
	
	public static BaseTranslator getInstance(){
		if(instance==null)
			instance=new DateTranslator();
		return instance;
	}
	
	public Object Translate(Map datas,String propName){
		Object value = datas.get(propName);
		if(value==null) return null;
		if(value instanceof java.sql.Date) return value;
		if (value instanceof java.util.Date) {
			java.util.Date dt = (java.util.Date) value;
            java.sql.Date dt2 = new java.sql.Date(dt.getTime());
            datas.put(propName, dt2);
            return dt2;
        }
        String strvalue =value.toString();
        if(strvalue.indexOf("${")!=-1) return null;
        if (strvalue.length() == 0){
        	datas.put(propName, null);
            return null;
        } 
        try {
        	Date dt = null;
        	synchronized(lock){
        		if(strvalue.length()>10){
        			sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        		}else{
        			sdf.applyPattern("yyyy-MM-dd");
        		}
        		dt = sdf.parse(strvalue);
        	}
            java.sql.Date dt2 = new java.sql.Date(dt.getTime());
            datas.put(propName, dt2);
            return dt2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
	} 
}
