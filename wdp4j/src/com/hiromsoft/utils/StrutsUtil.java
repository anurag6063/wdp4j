package com.hiromsoft.utils;

import java.util.HashMap;
import java.util.Iterator;

public class StrutsUtil {
	
	public static void emptyCustomerData(HashMap map){
		Iterator it=map.keySet().iterator();
		while(it.hasNext()){
			String name=(String)it.next();
			if(name!=null&&!name.startsWith("__")){
				map.put(name, "");
			}
			
		}
	}
	public static void emptyCustomerData2(HashMap map){
		String formid=(String)map.get("__formid");
		if(formid==null||formid.length()==0){
			String tmp=(String)map.get("__entityname");
			if(tmp!=null&&tmp.length()!=0){
				formid=tmp.replaceAll("designer_userform", "");
				try{
					Integer.parseInt(formid);
				}catch(Exception ex){
					formid=null;
				}
			}
		}
		if(formid==null||formid.length()==0){
			emptyCustomerData(map);
			return;
		}
		HashMap formMap=(HashMap)com.hiromsoft.utils.RuntimeSetting.getFormSetting(formid);
		if(formMap==null||formMap.size()==0){
			emptyCustomerData(map);
			return;
		}
		
		Iterator it=map.keySet().iterator();
		HashMap propmap=null;
		while(it.hasNext()){
			String name=(String)it.next();
			if(name!=null&&!name.startsWith("__")){
				propmap=(HashMap)formMap.get(name);
				if(propmap!=null&&propmap.size()!=0){
					String tmp=(String)propmap.get("dizeng");
					if(tmp!=null&&"1".equals(tmp)){
						Object value1=map.get(name);
						if(value1!=null){
							if(value1 instanceof String){
								try{
									int value2=Integer.parseInt((String)value1)+1;
									map.put(name, Integer.toString(value2));
									continue;
								}catch(Exception ex){}
							}else if(value1 instanceof java.lang.Number){
								try{
									int value2=Integer.parseInt(value1.toString())+1;
									map.put(name, Integer.toString(value2));
									continue;
								}catch(Exception ex){}
							}
						}
					}
					tmp=(String)propmap.get("jiyi");
					if(tmp!=null&&"1".equals(tmp)){
						continue;
					}
				}
				map.put(name, "");
			}
			
		}
	}

}
