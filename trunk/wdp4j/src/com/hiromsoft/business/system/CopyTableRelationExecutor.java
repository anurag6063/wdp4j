package com.hiromsoft.business.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.RequestUtil;

public class CopyTableRelationExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		String sourcetable=(String)userDataForm.getValue("sourcetable");
		String targettable=(String)userDataForm.getValue("targettable");
		if(sourcetable==null||sourcetable.length()==0||targettable==null||targettable.length()==0)
			throw new Exception("参数不正确，未设置源表名称或者目标表名称");
		
		PreparedStatement pst=null;
		PreparedStatement pst2=null;
		try{
			pst=dbsession.connection().prepareStatement("insert into designer_userform81(id,table1,field1,table2,field2) values (?,?,?,?,?)");
			pst2=dbsession.connection().prepareStatement("select table1,field1,table2,field2 from designer_userform81 where table1=? or table2=?");
			pst2.setString(1, sourcetable);
			pst2.setString(2, sourcetable);
			boolean executebatch=false;
		
			ResultSet rst=pst2.executeQuery();
			while(rst.next()){
				pst.setString(1, RequestUtil.GenerateUUID());
				String tmp=rst.getString(1);
				if(tmp.equals(sourcetable)) tmp=targettable;
				pst.setString(2, tmp);
				
				pst.setString(3, rst.getString(2));
				
				tmp=rst.getString(3);
				if(tmp.equals(sourcetable)) tmp=targettable;
				pst.setString(4, tmp);
				
				pst.setString(5, rst.getString(4));
				pst.addBatch();
				executebatch=true;
			}
			rst.close();
			if(executebatch)
				pst.executeBatch();
			
			pst.close();
			pst2.close();
			pst=null;
			pst2=null;
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
			if(pst2!=null){
				try{pst2.close();}catch(Exception ex){}
			}
			
		}
	}
}
