package com.hiromsoft.hiromform.action.beforesave.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;

public class SetFormId extends BaseOnSaveAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		int val=userDataForm.getIntValue("fid");
		if(val!=0) return;
		int newid=Utils.getSequenceValue(dbsession, "HIROMFORM_DESINGERFORMID");
		userDataForm.setValue("fid",new Long(newid));
		userDataForm.setValue("entityname","designer_userform"+newid);
		userDataForm.setValue("tablename","designer_userform"+newid);
	}

}
