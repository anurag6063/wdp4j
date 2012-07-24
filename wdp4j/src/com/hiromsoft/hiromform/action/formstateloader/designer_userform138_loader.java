package com.hiromsoft.hiromform.action.formstateloader;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.IFormStateLoader;
import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;


public class designer_userform138_loader implements IFormStateLoader{

	public void load(HttpServletRequest request, HttpServletResponse response, Session dbsession, UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		userDataForm.setFormState("1");
		String yhbh=userDataForm.getStringValue("YHBH");
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement("select formState from designer_userform136 where khbh=? and jsbh=?");
			pst.setString(1, (String)request.getSession().getAttribute("__khbh"));
			pst.setString(2, yhbh);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				request.setAttribute("JS_FORMSTATE", rst.getString(1));
			}
			rst.close();
			pst.close();
			pst=null;
		}finally{
			if(pst!=null)try{pst.close();}catch(Exception ex){}
		}
		
	}

	
	
}
