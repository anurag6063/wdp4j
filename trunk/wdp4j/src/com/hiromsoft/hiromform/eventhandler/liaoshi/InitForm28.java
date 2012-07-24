package com.hiromsoft.hiromform.eventhandler.liaoshi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.eventhandler.IInitFormHandler;
import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;

public class InitForm28 implements IInitFormHandler {

	public void execute(UserDataForm userDataForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// JSBH教师编号   //nianfen年度 //dtype 类型
		HttpSession session=request.getSession();
		User user=(User)session.getAttribute(Global.HIROM_USER);
		String id=(String)request.getSession().getAttribute("RSC_CHANGEUSERID");
		if(id!=null&&id.length()!=0){
			userDataForm.setValue("JSBH", (String)request.getSession().getAttribute("RSC_CHANGEUSERBH"));
			userDataForm.setValue("JSID", id);
		}else{
			userDataForm.setValue("JSBH", user.getYhbh());
			userDataForm.setValue("JSID", user.getID());
		}
		userDataForm.setValue("khbh", session.getAttribute("__khbh"));
		 
	}

	
}
