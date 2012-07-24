package com.hiromsoft.hiromform.action.beforedelete.system;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnDeleteAction;
import com.hiromsoft.hiromform.action.StopActionException;


public class SetUserNoUse extends BaseOnDeleteAction {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm,Map proxy) throws Exception {
		// TODO Auto-generated method stub
		proxy.put("inuseto", new java.sql.Date(System.currentTimeMillis()));
		dbsession.update(proxy);
		throw new StopActionException("");
	}
}
