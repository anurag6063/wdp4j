package com.hiromsoft.types.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;

import com.hiromsoft.hiromform.TemplateField;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;

public class ContextTranslator  extends BaseTranslator{

	protected static  BaseTranslator instance=null;
	
	public static BaseTranslator getInstance(){
		if(instance==null)
			instance=new ContextTranslator();
		return instance;
	}
	public Object Translate(HashMap datas,String propName){
		return null;
	}
	public Object Translate(HashMap datas,String propName,String expr,HttpServletRequest request,UserDataForm userDataForm) throws Exception{
		Object value = datas.get(propName);
		if(value==null) return null;
		String strvalue = value.toString();
		if(strvalue.length()==0) return null;
		if(strvalue.indexOf("${")==-1) return null;
		return this.internalTranslate(datas, propName, strvalue, request, userDataForm);
	}
	public Object internalTranslate(Map datas,String propName,String expr,HttpServletRequest request,UserDataForm userDataForm) throws Exception{
		Object value=null;
		String strvalue =expr;
        if (strvalue.length() == 0){
        	datas.put(propName, null);
        	return null;
        }
        if(strvalue.indexOf("${")==-1) return null;
        if(strvalue.startsWith("${")&&strvalue.endsWith("}")){
        	value=this.getContextValue(strvalue.substring(2, strvalue.length()-1), request, userDataForm);
        	datas.put(propName, value);
        	return value;
        }
    	Pattern pttn=Pattern.compile("\\$\\{[^\\s&&[^\\$\\{\\}]]+\\}");
		String varName=null;
		Object tmpo=null;
		StringBuffer text=new StringBuffer();
		Matcher matcher=pttn.matcher(strvalue);
        while(matcher.find()){
            varName=matcher.group();
            varName=varName.substring(2,varName.length()-1);
            tmpo=this.getContextValue(varName, request, userDataForm);
            if(tmpo==null) tmpo="";
            matcher.appendReplacement(text,tmpo.toString());
        }
        if(!matcher.find()) return null;
        matcher.appendTail(text);
        datas.put(propName, text.toString());
    	return text.toString();
	}
	private Object getContextValue(String varName,HttpServletRequest request,UserDataForm userDataForm) throws Exception{
		Object value=null;
		if(varName.equals("sysdate")){
			value=new java.sql.Date(System.currentTimeMillis());
			return value;
		}
		if(varName.startsWith("user.")){
			User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
			if(user==null) return null;
			try{value=BeanUtils.getProperty(user, varName.substring(5));}catch(Exception ex){
				value=user.getAttribute(varName.substring(5));
			}
			return value;
		}
		if(varName.startsWith("request.")){
			value=request.getAttribute(varName.substring(8));
			return value;
		}
		if(varName.startsWith("session.")){
			value=request.getAttribute(varName.substring(8));
			return value;
		}
		value=userDataForm.getValue(varName);
		return value;
	}
	
	public Object TranslateSystemProperties(Map datas,HttpServletRequest request,UserDataForm userDataForm) throws Exception{
		User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
		if(user==null) return null;
		datas.put("sys_lastmodifier", user.getYhbh());
		datas.put("sys_lastmodifieddate", new java.sql.Date(System.currentTimeMillis()));
		Object tmp=DateTimeTranslator.getInstance().Translate(datas, "sys_createddate");
		if(tmp==null){
			datas.put("sys_createddate", new java.sql.Date(System.currentTimeMillis()));
		}
		Object value = datas.get("sys_creator");
    	if(value!=null&&value.toString().length()!=0) return null;
    	datas.put("sys_creator", user.getYhbh());
    	datas.put("sys_lockforever","0");
    	String group=",0,";//deault everyone
    	if(user.getGroups()==null||user.getGroups().length==0) {
    		datas.put("sys_creator_groups", group);
    		return null;
    	}
    	group=",";
		for(int num=0;num<user.getGroups().length;num++){
			if(user.getGroups()[num]==null||user.getGroups()[num].length()==0) continue;
			group=group+user.getGroups()[num]+",";
		}
		if(group.length()==1) group=",0,";
		datas.put("sys_creator_groups", group);
		return null;
	}
	public static Object TranslateSystemProperties(Map obj,boolean empty){
		Utils.getSystemFields();
		Vector sysfields=Utils.getSystemFields();
		if(sysfields==null) return null;

		for(int num=0;num<sysfields.size();num++){
			if(empty)
				obj.put(((TemplateField)sysfields.get(num)).getPropname(), null);
			else{
				String tmp=((TemplateField)sysfields.get(num)).getPropname();
				if("sys_creator".equals(tmp)){
					obj.put(tmp, "0");
				}else if("sys_creator_groups".equals(tmp)){
					obj.put(tmp, "01");
				}else if("sys_createddate".equals(tmp)){
					obj.put(tmp, new java.sql.Date(System.currentTimeMillis()));
				}else if("sys_lastmodifier".equals(tmp)){
					obj.put(tmp, "0");
				}else if("sys_lastmodifieddate".equals(tmp)){
					obj.put(tmp, new java.sql.Date(System.currentTimeMillis()));
				}else if("sys_lockforever".equals(tmp)){
					obj.put(tmp, "0");
				}
				
			}
		}
		return null;
	}

}
