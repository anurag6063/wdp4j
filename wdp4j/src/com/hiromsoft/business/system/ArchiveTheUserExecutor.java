package com.hiromsoft.business.system;


import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.proxy.MapProxy;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.types.translator.ContextTranslator;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;
import com.hiromsoft.web.modal.User;

public class ArchiveTheUserExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

//		最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);

		for(int num=0;num<rowidx;num++){
			String ID=ids[num];
			this.doArchiveUser(dbsession, request, ID);
		}

	}
	public void doArchiveUser(Session dbsession,HttpServletRequest request,String uid) throws Exception{
		PreparedStatement pst=null;
		ResultSet rst=null;
		try{
			//归档的用户包括用户基本信息和用户团组信息、岗位信息及部门信息
			ContextTranslator translator=(ContextTranslator)ContextTranslator.getInstance();
			String entityName="designer_userform21";
			MapProxy proxy=(MapProxy)dbsession.load(entityName, uid);
			HashMap newuser=new HashMap();
			newuser.putAll(proxy);
			newuser.put("ID", "0");
			translator.TranslateSystemProperties(newuser, request, null);
			dbsession.saveOrUpdate(entityName, newuser);
			String nuid=(String)newuser.get("ID");
			
			proxy.put("inuseto",new Date(System.currentTimeMillis()));
			translator.TranslateSystemProperties(proxy, request, null);
			dbsession.update(proxy);
			
			pst=dbsession.connection().prepareStatement("update sys_role_users set uhid=? where uhid=?");
			pst.setString(1,nuid);
			pst.setString(2, uid);
			pst.execute();
			pst.close();
			pst=null;
			
			Vector teams=new Vector();
			pst=dbsession.connection().prepareStatement("select gid,gcode from  sys_groupandteam_users where uhid=?");
			pst.setString(1, uid);
			rst=pst.executeQuery();
			while(rst.next()){
				HashMap row=new HashMap();
				teams.add(row);
				row.put("GID", rst.getString(1));
				row.put("GCODE", rst.getString(2));
			}
			rst.close();
			pst.close();
			pst=null;
			
			entityName="designer_userform115";
			for(int num=0;num<teams.size();num++){
				HashMap obj=(HashMap)teams.get(num);
				obj.put("UHID", nuid);
				translator.TranslateSystemProperties(obj, request, null);
				dbsession.saveOrUpdate(entityName, obj);
			}
			
			User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
			if(user!=null){
				if(user.getID().equals(uid)){
					user.setID(nuid);
				}
			}
		}finally{
			if(rst!=null) try{rst.close();}catch(Exception ex){}
			if(pst!=null) try{pst.close();}catch(Exception ex){}
		}
	}

}
