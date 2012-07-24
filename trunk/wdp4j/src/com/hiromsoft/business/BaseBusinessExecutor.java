package com.hiromsoft.business;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;

public class BaseBusinessExecutor implements IBusinessExecutor {

	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void execute(UserDataForm userDataForm, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub

	}
}
