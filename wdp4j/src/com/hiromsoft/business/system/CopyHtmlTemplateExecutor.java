package com.hiromsoft.business.system;


import java.sql.PreparedStatement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.utils.RequestUtil;

public class CopyHtmlTemplateExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		String Fid=(String)userDataForm.getValue("fid");
		String newname=(String)userDataForm.getValue("newname");
		
		int maxrows=1;
		String[] ids=new String[maxrows];
		RequestUtil.getListSelection(request, ids);
		
		PreparedStatement pst=null;
		try{
			int newid=0;
			if(Fid!=null&&Fid.length()!=0){
				try{
					newid=Integer.parseInt(Fid);
				}catch(Exception ex){}
			}
			if(newid==0)
				newid=Utils.getSequenceValue(dbsession, "HIROMFORM_DESINGERFORMID");
			
			pst=dbsession.connection().prepareStatement("insert into sys_forms(ID,name,content,tablename,entityname,version,type,flag,FID) "+
						"select ?,?,content,tablename,?,version,type,flag,? from sys_forms where ID=?");
			pst.setString(1, com.hiromsoft.utils.RequestUtil.GenerateUUID());
			pst.setString(2,newname);
			pst.setString(3,"designer_userform"+newid);
			pst.setInt(4,newid);
			pst.setString(5,ids[0]);
			pst.execute();
			pst.close();
			pst=null;
						
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
		}
	}
}
