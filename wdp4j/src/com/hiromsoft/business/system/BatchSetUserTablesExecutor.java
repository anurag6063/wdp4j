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

public class BatchSetUserTablesExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

//		最多复制100行
		
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		
		String[] fields=new String[8];
		fields[0]=userDataForm.getStringValue("field0");
		fields[1]=userDataForm.getStringValue("field1");
		fields[2]=userDataForm.getStringValue("field2");
		fields[3]=userDataForm.getStringValue("field3");
		fields[4]=userDataForm.getStringValue("field4");
		fields[5]=userDataForm.getStringValue("field5");
		fields[6]=userDataForm.getStringValue("field6");
		fields[7]=userDataForm.getStringValue("field7");
		
		PreparedStatement pst=null;

		try{
			
			
			pst=dbsession.connection().prepareStatement("update designer_userform139 set field0=?,field1=?,field2=?,field3=?,field4=?,field5=?,field6=?,field7=? where id=?");
			boolean exec=false;
			for(int i=0;i<rowidx;i++){
				String id=ids[i];
				pst.setString(9, id);
				for(int a=0;a<fields.length;a++){
					if(fields[a]!=null&&fields[a].length()!=0)
						pst.setString(a+1, fields[a]);
					else
						pst.setString(a+1,null);
				}
				pst.addBatch();
				exec=true;
			}
			if(exec) pst.executeBatch();
			pst.close();
			
			
			
		}finally{
			if(pst!=null) {try{pst.close();}catch(Exception ex){}}
			
		}
	}

}
