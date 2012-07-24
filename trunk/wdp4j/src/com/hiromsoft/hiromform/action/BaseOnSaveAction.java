package com.hiromsoft.hiromform.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.proxy.MapProxy;

import com.hiromsoft.hiromform.UserDataForm;

public class BaseOnSaveAction implements ISaveFormActionExecutor {
	
	private SaveUserDataFormAction service=null;

	public SaveUserDataFormAction getService() {
		return service;
	}

	public void setService(SaveUserDataFormAction service) {
		this.service = service;
	}

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception{
		
	}

	public void parseChildObjects(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm, String[] props, String[] datas,
			HashMap childobj,String action) throws Exception {
		
		if(this.changeDeleteRelationToUpdate(childobj)&&action.equals("del")){
			childobj.put("__action", "upd");
			for(int cnt=0;cnt<props.length;cnt++){
				childobj.put(props[cnt],null);
			}
			return;
		}
		for(int cnt=0;cnt<props.length;cnt++){
			if(cnt+2>=datas.length){
				childobj.put(props[cnt],null);
				continue;
			}
			if(datas[cnt+2]==null||datas[cnt+2].length()==0)
			{
				childobj.put(props[cnt],null);
				continue;
			}
			if(datas[cnt+2].equals("${nc}")) continue;
			childobj.put(props[cnt],datas[cnt+2]);
		}
		

	}
	public boolean changeDeleteRelationToUpdate(HashMap childObj){
		return false;
	}
	

	public boolean canGoon() {
		return true;
	}

	public void saveChildObjects(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm, HashMap childobj,String action) throws Exception {
		
		String __action=action;
		if(__action.equals("del")){
			this.doDeleteChildObject(request, response, dbsession, userDataForm, childobj);
			return;
		}
		
		if(this.getService()==null) return;
		this.getService().TranslateDataType(childobj, request, userDataForm);
		
		if(__action.equals("upd")){
			this.doUpdateChildObject(request, response, dbsession, userDataForm, childobj);
			return;
		}
		this.doCreateChildObject(request, response, dbsession, userDataForm, childobj);
	}
	public void doDeleteChildObject(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response,
			org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm,
			Map childobj) throws Exception{
		MapProxy proxy=(MapProxy)dbsession.load((String)childobj.get("__entityname"),(String)childobj.get("ID"));
		dbsession.delete(proxy);
	}
	
	public void doUpdateChildObject(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response,
			org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm,
			Map childobj) throws Exception{
		MapProxy proxy=(MapProxy)dbsession.load((String)childobj.get("__entityname"),(String)childobj.get("ID"));
		proxy.putAll(childobj);
		dbsession.update(proxy);
		
	}
	public void doCreateChildObject(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response,
			org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm,
			Map childobj) throws Exception{
		dbsession.saveOrUpdate((String)childobj.get("__entityname"), childobj);
	}
	
}
