package com.hiromsoft.business.system;

import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.RequestUtil;

public class NoShowViewFieldsExecutor extends BaseBusinessExecutor {
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		
		PreparedStatement pst=null;
		//"insert into sys_views_fields(viewid,fieldName,title,datatype,display,sn,id) "+
		//"select ,fieldName,title,datatype,display,sn,id"
		try{
			pst=dbsession.connection().prepareStatement("update sys_views_fields set display='0' where id=?");
			for(int num=0;num<rowidx;num++){
				pst.setString(1, ids[num]);
				pst.addBatch();
			}
			pst.executeBatch();
			pst.close();
			pst=null;
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
		}
	}
}
