package com.hiromsoft.hiromform.action.formstateloader;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.IFormStateLoader;
import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;


public class designer_userform132_loader implements IFormStateLoader{

	public void load(HttpServletRequest request, HttpServletResponse response, Session dbsession, UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		String fs=userDataForm.getStringValue("formState");
		if(fs==null||fs.length()==0)
			userDataForm.setFormState("1");
		else if(fs.equals("2")){
			User  user=(User)request.getSession().getAttribute(Global.HIROM_USER);
			//if(user.ge)
			userDataForm.setFormState(fs);
		}else
			userDataForm.setFormState(fs);
		
	}

	
	
}
