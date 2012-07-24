package com.hiromsoft.business.system;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.acl.Acl;
import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.RequestUtil;

public class SetSelectedUsersAppendExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

//		最多复制100行
		
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		String[] rids=new String[maxrows];
		int rrowidx=RequestUtil.getListSelection(request, rids,"srid");
		 
		PreparedStatement pst=null;
		PreparedStatement pst1=null;
		PreparedStatement pst2=null;
		try{
			String khid=ids[0];
			String yhbhs[]=rids;
			String khbh=null;
			String khname=null;
			
			pst=dbsession.connection().prepareStatement("select code,name from designer_userform47 where id=?");
			pst.setString(1,khid);
			ResultSet rst=pst.executeQuery();
			
			if(rst.next()){
				khbh=rst.getString(1);
				khname=rst.getString(2);
			}
			rst.close();
			pst.close();
			
			pst=dbsession.connection().prepareStatement("insert into designer_userform139(id,khbh,khmc,yhbh,xm) values (?,?,?,?,?)");
			pst.setString(2,khbh);
			pst.setString(3,khname);
			pst1=dbsession.connection().prepareStatement("select xm from sys_users where yhbh=? and inuseto is null");
			pst2=dbsession.connection().prepareStatement("select * from designer_userform139 where yhbh=? and khbh=?");
			pst2.setString(2, khbh);
			boolean exec=false;
			for(int i=0;i<rrowidx;i++){
				
				
				Acl acl=new Acl(dbsession);
				String jsbh=yhbhs[i];
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
				
				boolean exist=false;
				pst2.setString(1, yhbhs[i]);
				rst=pst2.executeQuery();
				if(rst.next())
					exist=true;
				rst.close();
				if(exist) continue;
				
				pst1.setString(1, yhbhs[i]);
				rst=pst1.executeQuery();
				if(rst.next()){
					pst.setString(1,com.hiromsoft.utils.RequestUtil.GenerateUUID());
					pst.setString(4, yhbhs[i]);
					pst.setString(5, rst.getString(1));
					pst.addBatch();
					exec=true;
				}
				rst.close();
			}
			pst1.close();
			if(exec) pst.executeBatch();
			pst.close();
			
		}finally{
			if(pst!=null) {try{pst.close();}catch(Exception ex){}}
			if(pst1!=null) {try{pst1.close();}catch(Exception ex){}}
			if(pst2!=null) {try{pst2.close();}catch(Exception ex){}}
		}
	}

}
