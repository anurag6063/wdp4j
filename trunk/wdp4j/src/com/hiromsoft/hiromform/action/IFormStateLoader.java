package com.hiromsoft.hiromform.action;

public interface IFormStateLoader {
	public void load(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response, org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm) throws Exception;
}
