package com.hiromsoft.business.liaoshi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.proxy.MapProxy;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromview.HtmlViewTemplate;
import com.hiromsoft.utils.RequestUtil;

public class SendBackExecutor_jinji3 extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		//最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		
		PreparedStatement pst=null;
		try{
			
			pst=dbsession.connection().prepareStatement("update designer_userform162 set formstate='3' where id=?");
			boolean executebatch=false;
			for(int num=0;num<rowidx;num++){
				pst.setString(1, ids[num]);
				pst.addBatch();
				executebatch=true;
			}
			if(executebatch)
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
