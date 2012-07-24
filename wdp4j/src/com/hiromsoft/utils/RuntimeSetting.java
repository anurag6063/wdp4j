package com.hiromsoft.utils;

import java.util.HashMap;
import java.util.Vector;

import com.hiromsoft.types.translator.BaseTranslator;

public class RuntimeSetting {
	
	public static int VIEWSTYPE_RECENT=1;
	public static int VIEWSTYPE_DEFINED=0; 

	public static HashMap views=new HashMap();
	public static HashMap formSettings=new HashMap();
	
	static{
		String[][] viewinfos={{"10","所有信息2011","0"},
				            {"11","最近修改信息","1"}};
		
		views.put("5",viewinfos );
	}
	
	public static String[][] getListViews(String viewid){
		String[][] temp={{"1","所有记录","0"},
	            {"1","所有记录","1"}};
		String[][] tmp=(String[][])views.get(viewid);
		if(tmp==null) tmp=temp;
		return tmp;
	}
	public static void addFormDataTypeTranslator(String formId,String propname,BaseTranslator translator,int index){
		addFormDataTypeTranslator(formId,propname,null,translator,index);
	}
	
	public static void addFormDataTypeTranslator(String formId,String propname,String value,BaseTranslator translator,int index){
		HashMap map1=(HashMap)formSettings.get("F"+formId);
		if(map1==null){
			map1=new HashMap(5);
			formSettings.put("F"+formId, map1);
		}
		HashMap trans=(HashMap)map1.get("__translators");
		if(trans==null){
			trans=new HashMap();
			map1.put("__translators", trans);
		}
		if(value==null||value.length()==0){
			trans.put(propname+"_"+index, translator);
			return;
		}
		Vector tmp=new Vector(2);
		tmp.add(0, value);
		tmp.add(1, translator);
		trans.put(propname+"_"+index, tmp);
		
	}
	public static HashMap getFormDataTypeTranslator(String formId){
		HashMap retval=null;
		HashMap map1=(HashMap)formSettings.get("F"+formId);
		if(map1!=null){
			retval=(HashMap)map1.get("__translators");
		}
		return retval;
	}
	
	public static void addFormSetting(String formId,String propname,String settingName,String settingValue){
		HashMap map1=(HashMap)formSettings.get("F"+formId);
		if(map1==null){
			map1=new HashMap(5);
			formSettings.put("F"+formId, map1);
		}
		HashMap map2=(HashMap)map1.get(propname);
		if(map2==null){
			map2=new HashMap(2);
			map1.put(propname, map2);
		}
		map2.put(settingName, settingValue);
	}
	public static HashMap getFormSetting(String formId,String propname){
		HashMap retval=null;
		HashMap map1=(HashMap)formSettings.get("F"+formId);
		if(map1!=null){
			retval=(HashMap)map1.get(propname);
		}
		return retval;
	}
	public static HashMap getFormSetting(String formId){
		HashMap map1=(HashMap)formSettings.get("F"+formId);
		return map1;
	}
}
