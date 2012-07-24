package com.hiromsoft.types.translator;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.hiromsoft.hiromform.UserDataForm;

public class NullContextTranslator extends ContextTranslator {
	
	protected static  BaseTranslator instance=null;
	
	public static BaseTranslator getInstance(){
		if(instance==null)
			instance=new NullContextTranslator();
		return instance;
	}
	public Object Translate(Map datas,String propName){
		return null;
	}
	public Object Translate(HttpServletRequest request,UserDataForm userDataForm,Map datas,String propName,String expr) throws Exception{
		Object value=datas.get(propName);
		if(value!=null&&value.toString().length()!=0) return value;
		if(expr!=null&&expr.startsWith("'")&&expr.endsWith("'")){
			String tmp=expr.substring(1, expr.length()-1);
			datas.put(propName, tmp);
			return tmp;
		}
		return super.internalTranslate(datas, propName, expr, request, userDataForm);
		
	}
}
