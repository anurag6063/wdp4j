package com.hiromsoft.hiromform.action.notfound.liaoshi;


import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnNotFoundAction;
import com.hiromsoft.hiromview.action.ShowListViewAction;
import com.hiromsoft.utils.RequestUtil;


public class InitChuShenExecutor extends BaseOnNotFoundAction {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		PreparedStatement pst=null;
		try{
		
			String ID=RequestUtil.GenerateUUID();
			String sql="insert into  designer_userform48(ID,field0,jsbh,khbh,jsid) select ${__tmpid},xm,yhbh,${__khbh},id from sys_users where yhbh=${user.yhbh} and inuseto is null";
			request.setAttribute("__tmpid", ID);
			pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
			pst.execute();
			pst.close();
			pst=null;
			userDataForm.setValue("__newid", ID);
		}finally{
			if(pst!=null)
				try{pst.close();}catch(Exception ex){}
		}
		
	}

}
