package com.hiromsoft.business.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;

public class InitLogonNameExecutor extends BaseBusinessExecutor {
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		PreparedStatement pst=null;
		PreparedStatement pst1=null;
		PreparedStatement pst2=null;
		try{
			pst=dbsession.connection().prepareStatement("select xm,id from sys_users where dlzh is null");
			ResultSet rst=pst.executeQuery();
			pst1=dbsession.connection().prepareStatement("select py from sys_chs_words where chs_word=?");
			pst2=dbsession.connection().prepareStatement("update sys_users set dlzh=? where id =?");
			ResultSet rst2=null;
			while(rst.next()){
				String name=rst.getString(1);
				String dlzh="";
				if(name!=null&&name.length()!=0){
					boolean ok=true;
					for(int num=0;num<name.length();num++){
						String word="";
						if(num!=name.length()-1)
							word=name.substring(num,num+1);
						else
							word=name.substring(num);
						pst1.setString(1, word);
						rst2=pst1.executeQuery();
						if(rst2.next()){
							dlzh=dlzh+rst2.getString(1);
						}else{
							ok=false;
						}
						rst2.close();
						if(!ok) break;
					}
					if(ok){
						pst2.setString(1, dlzh);
						pst2.setString(2, rst.getString(2));
						pst2.execute();
					}
				}
			}
			pst.close();
			pst1.close();
			pst2.close();
			pst=null;
			pst1=null;
			pst2=null;
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
			if(pst1!=null){
				try{pst1.close();}catch(Exception ex){}
			}
			if(pst2!=null){
				try{pst2.close();}catch(Exception ex){}
			}
		}
	}
}
