package com.hiromsoft.hiromform.action;

import java.util.HashMap;

public interface ISaveFormActionExecutor extends IFormActionExecutor {

	public void parseChildObjects(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response,
			org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm,
			String[] props,String datas[],HashMap childobj,String action) throws Exception;
	
	public void saveChildObjects(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response,
			org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm,
			HashMap childobj,String action) throws Exception;
	
	public boolean changeDeleteRelationToUpdate(HashMap childObj);
	
	public boolean canGoon();
	
	public SaveUserDataFormAction getService() ;
	public void setService(SaveUserDataFormAction service);
	
}
