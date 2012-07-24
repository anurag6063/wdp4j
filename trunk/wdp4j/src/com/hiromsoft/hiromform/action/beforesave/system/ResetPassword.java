package com.hiromsoft.hiromform.action.beforesave.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;
import com.hiromsoft.hiromview.action.ShowListViewAction;


public class ResetPassword extends BaseOnSaveAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		String templateId=(String)userDataForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0)
		{
			templateId=((String)userDataForm.getValue("__entityname")).replaceAll("designer_userform", "");
		}
		if(!"67".equals(templateId)) return;
		
		PreparedStatement pst=null;
		try{
			String tmp1=(String)userDataForm.getValue("field11");
			String tmp2=(String)userDataForm.getValue("field12");
			
			if(!tmp1.equals(tmp2)){
				throw new Exception ("两次输入的新密码不一致，重新输入。");
			}
			
			String oldmm="";
			String sql="select field9 from sys_users where yhbh=${user.yhbh}";
			pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
			ResultSet rst=pst.executeQuery();
			if(rst.next())
				oldmm=rst.getString(1);
			rst.close();
			pst.close();
			pst=null;
			
			String tmp3=(String)userDataForm.getValue("field13");
			if(!tmp3.equals(oldmm)){
				throw new Exception ("原密码输入错误，重新输入。");
			}
			userDataForm.setValue("field9",tmp1);
			userDataForm.setValue("field13","");
			userDataForm.setValue("field11","");
			userDataForm.setValue("field12","");
			
			
		}finally{
			try{
				if(pst!=null)
					pst.close();
			}catch(Exception ex){}
		}
	}

}
