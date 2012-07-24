package com.hiromsoft.hiromform.action.aftersave.system;

import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;



public class AssignMenuItemToRoot extends BaseOnSaveAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		boolean isnew=userDataForm.getBooleanValue("__isnewobj");
		if(!isnew) return;
		String miid=userDataForm.getStringValue("ID");
		this.doAssignAction(dbsession, miid);
		
	}
	public void doAssignAction(Session dbsession,String miid) throws Exception{
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement("insert into sys_role_menuitems(id,MIID,RID) VALUES (?,?,?)");
			//ROOT ID=297e98f533aaf3950133ab1e40f300e8
			pst.setString(1, Utils.GenerateUUID());
			pst.setString(2, miid);
			pst.setString(3, "297e98f533aaf3950133ab1e40f300e8");
			pst.execute();
			pst.close();
			pst=null;
		}finally{
			try{
				if(pst!=null)
					pst.close();
			}catch(Exception ex){}
		}
	}

}
