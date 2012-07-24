package com.hiromsoft.types.translator;

public class DateTimeTranslator extends DateTranslator{
	
	protected static  BaseTranslator instance=null;
	
	public static BaseTranslator getInstance(){
		if(instance==null)
			instance=new DateTimeTranslator();
		return instance;
	}
	public DateTimeTranslator(){
		super();
		super.sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
	}
	
}
