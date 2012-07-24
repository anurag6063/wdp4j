package com.hiromsoft.hiromform.action.aftersave.liaoshi;

import java.sql.PreparedStatement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;
import com.hiromsoft.hiromview.action.ShowListViewAction;

public class FinishFuShen extends BaseOnSaveAction {
	

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		String templateId=(String)userDataForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0)
		{
			templateId=((String)userDataForm.getValue("__entityname")).replaceAll("designer_userform", "");
		}
		
		PreparedStatement pst=null;
		try{
			if(!"48".equals(templateId)) return;
			
			String sql="update designer_userform27 set field12 ='6' where field1=${user.yhbh} and field13=${__khbh}";
			pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
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
