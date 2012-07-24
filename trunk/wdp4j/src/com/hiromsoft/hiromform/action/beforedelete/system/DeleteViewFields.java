package com.hiromsoft.hiromform.action.beforedelete.system;

import java.sql.PreparedStatement;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnDeleteAction;

public class DeleteViewFields extends BaseOnDeleteAction {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm,Map proxy) throws Exception {
		// TODO Auto-generated method stub
		
		long vid=((Long)proxy.get("viewID")).longValue();//userDataForm.getLongValue("viewID");

		PreparedStatement pst=null;
        try{
        	pst=dbsession.connection().prepareCall("delete from sys_views_fields where viewid=?");
        	pst.setLong(1, vid);
        	pst.execute();
        	pst.close();
        	pst=null;
        }finally{
        	if(pst!=null) try{pst.close();}catch(Exception ex){}
        }
        
	}
}
