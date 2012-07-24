package com.hiromsoft.business.liaoshi;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.acl.Acl;
import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.RequestUtil;

public class ChangeCurrentUserExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

//		最多复制100行
		
		int maxrows=100;
		String[] rids=new String[maxrows];
		int rrowidx=RequestUtil.getListSelection(request, rids,"srid");
		 
		PreparedStatement pst=null;
		try{
			String yhbh=rids[0];
			if(yhbh==null) return;
			
			
			pst=dbsession.connection().prepareStatement("select ID from sys_users where yhbh=? and inuseto is null");
			pst.setString(1, yhbh);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				request.getSession().setAttribute("RSC_CHANGEUSERID",rst.getString(1));
				request.getSession().setAttribute("RSC_CHANGEUSERBH",yhbh);
			}
			rst.close();
			pst.close();
			pst=null;
			
		}finally{
			if(pst!=null) {try{pst.close();}catch(Exception ex){}}
		}
	}

}
