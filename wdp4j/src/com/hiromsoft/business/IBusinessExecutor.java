package com.hiromsoft.business;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.IHandler;
import com.hiromsoft.hiromform.UserDataForm;

public interface IBusinessExecutor extends IHandler {
	
	public void execute(Session dbsession,UserDataForm userDataForm,HttpServletRequest request,HttpServletResponse response) throws Exception;
}
