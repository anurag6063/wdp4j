package com.hiromsoft.hiromform.action.formstateloader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.IFormStateLoader;
import com.hiromsoft.hiromview.action.ShowListViewAction;

public class designer_userform49_loader implements IFormStateLoader{

	public void load(HttpServletRequest request, HttpServletResponse response, Session dbsession, UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		PreparedStatement pst=null;
		try{
			String state="";
			String sql="select field12 from designer_userform27 where field1=${user.yhbh} and field13=${__khbh}";
			pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				state=rst.getString(1);
			}
			rst.close();
			pst.close();
			pst=null;
			//¸Ã±íµ¥ÓÐ4¸ö×´Ì¬
			//1=Î´±¨   2=Î´ÉóºË 3=Î´¸´ºË 4=ÒÑ¸´ºË
			if("1".equals(state)){
				userDataForm.setFormState("1");
			}else if("2".equals(state)){
				userDataForm.setFormState("2");
			}else if("3".equals(state)||"4".equals(state)||"5".equals(state)){
				userDataForm.setFormState("3");
			}else if("8".equals(state)||"6".equals(state)||"7".equals(state)||"0".equals(state)){
				userDataForm.setFormState("4");
			}
			
		}finally{
			if(pst!=null) try{pst.close();}catch(Exception ex){}
		}
		
	}

	
	
}
