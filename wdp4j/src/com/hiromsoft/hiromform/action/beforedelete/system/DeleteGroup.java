package com.hiromsoft.hiromform.action.beforedelete.system;

import java.sql.PreparedStatement;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnDeleteAction;

public class DeleteGroup extends BaseOnDeleteAction {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm,Map proxy) throws Exception {
		// TODO Auto-generated method stub
		
		String ID=(String)proxy.get("code");

		PreparedStatement pst=null;
        try{
        	pst=dbsession.connection().prepareCall("delete from sys_groupandteam_users where gcode=?");
        	pst.setString(1, ID);
        	pst.execute();
        	pst.close();
        	pst=null;
        }finally{
        	if(pst!=null) try{pst.close();}catch(Exception ex){}
        }
        
	}
}
