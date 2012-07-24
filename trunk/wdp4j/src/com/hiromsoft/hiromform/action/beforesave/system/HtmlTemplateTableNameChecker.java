package com.hiromsoft.hiromform.action.beforesave.system;

import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.proxy.MapProxy;

import com.hiromsoft.hiromform.HtmlFormTemplate;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;
import com.hiromsoft.utils.UserViewUtil;

public class HtmlTemplateTableNameChecker extends BaseOnSaveAction {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		String ID=userDataForm.getStringValue("ID");
		if(ID!=null&&ID.equals("0")) return;
		String newtablename=userDataForm.getStringValue("tablename");
		if(newtablename==null||newtablename.length()==0) return;
		if(newtablename.charAt(0)<'A'||(newtablename.charAt(0)>'Z'&&newtablename.charAt(0)<'a')||newtablename.charAt(0)>'z'){
			throw new Exception("数据库表名中含有非法字符");
		}
		
		MapProxy old=(MapProxy)dbsession.load("designer_userform86", ID);
		if(old==null) return;
		String oldtablename=(String)old.get("tablename");
		dbsession.evict(old);
		
		if(oldtablename==null||oldtablename.length()==0) return;
		
		if(newtablename.equals(oldtablename)) return;
		
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement("update sys_forms_fields set sjkbnbmc=? where sjkbnbmc=? and fid=?");
			pst.setString(1,newtablename);
			pst.setString(2,oldtablename );
			pst.setLong(3,userDataForm.getLongValue("fid"));
			pst.execute();
			pst.close();
			pst=null;
			
			pst=dbsession.connection().prepareStatement("update sys_views set tablename=? where tablename=?");
			pst.setString(1,newtablename);
			pst.setString(2,oldtablename);
			pst.execute();
			pst.close();
			pst=null;
			
			pst=dbsession.connection().prepareStatement("update sys_views set sql1=replace(sql1,?,?) where sql1 like ?");
			pst.setString(1,oldtablename);
			pst.setString(2,newtablename);
			pst.setString(3,'%'+oldtablename+"%");
			pst.execute();
			pst.close();
			pst=null;
			
			
			try{
				pst=dbsession.connection().prepareStatement("alter table "+oldtablename+" rename to "+newtablename);
				pst.execute();
				pst.close();
				pst=null;
			}catch(Exception ex){}
			
			HtmlFormTemplate form=new ParseHtmlTemplate().doParse(request, response, dbsession, userDataForm);
			UserViewUtil viewUtil=new UserViewUtil();
			viewUtil.saveViewTemplate(dbsession,form);
			
		}finally{
			if(pst!=null) try{pst.close();}catch(Exception ex){}
		}
	}
}
