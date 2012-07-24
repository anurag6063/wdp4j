package com.hiromsoft.hiromform.action.beforesave.liaoshi;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.acl.Acl;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;


public class FinishShangBao_jinji extends BaseOnSaveAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		String fs=userDataForm.getStringValue("formState");
		if(fs==null||fs.length()==0) fs="1";
		int nextfs=Integer.parseInt(fs)+1;
		fs=Integer.toString(nextfs);
		userDataForm.setValue("formState", fs);
		
		
	}

}
