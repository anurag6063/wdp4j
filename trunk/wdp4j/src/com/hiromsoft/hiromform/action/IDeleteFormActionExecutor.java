package com.hiromsoft.hiromform.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;

public interface IDeleteFormActionExecutor extends IFormActionExecutor {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm,Map proxy) throws Exception;
}
