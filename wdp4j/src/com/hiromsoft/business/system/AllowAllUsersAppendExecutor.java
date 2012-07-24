package com.hiromsoft.business.system;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.acl.Acl;
import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;

public class AllowAllUsersAppendExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

//		最多复制100行
		/*
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		String[] rids=new String[maxrows];
		int rrowidx=RequestUtil.getListSelection(request, rids,"srid");
		 */
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement("select yhbh from sys_users where inuseto is null");
			ResultSet rst=pst.executeQuery();
			String jsbh=null;
			while(rst.next()){
				Acl acl=new Acl(dbsession);
				jsbh=rst.getString(1);
				acl.setUserAuthCodeForView(jsbh,"1033",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1034",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1035",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1036",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1037",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1043",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1044",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1045",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1046",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1048",Acl.ALLOW_ALL);
				acl.setUserAuthCodeForView(jsbh,"1049",Acl.ALLOW_ALL);
				
				acl.setUserAuthCodeForFormStore(jsbh, "28", Acl.ALLOW_ALL);
				acl.setUserAuthCodeForFormStore(jsbh, "49", Acl.ALLOW_ALL);
				acl.saveOrUpdate();
				acl=null;
			}
			rst.close();
			pst.close();
			pst=null;
			
		}finally{
			if(pst!=null) {try{pst.close();}catch(Exception ex){}}
		}
	}

}
