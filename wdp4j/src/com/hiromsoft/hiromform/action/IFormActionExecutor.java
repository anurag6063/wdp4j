package com.hiromsoft.hiromform.action;

public interface IFormActionExecutor {
	public void execute(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response, org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm) throws Exception;

}
