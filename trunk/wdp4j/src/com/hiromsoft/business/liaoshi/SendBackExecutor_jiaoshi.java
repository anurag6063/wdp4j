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

public class SendBackExecutor_jiaoshi extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		//最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		
		PreparedStatement pst=null;
		PreparedStatement pst1=null;
		try{
			pst1=dbsession.connection().prepareStatement("select * from  designer_userform136 where (formstate='2') and id=?");
			
			
			pst=dbsession.connection().prepareStatement("update designer_userform136 set formstate=1 where id=?");
			boolean executebatch=false;
			for(int num=0;num<rowidx;num++){
				boolean ok=false;
				pst1.setString(1, ids[num]);
				ResultSet rst=pst1.executeQuery();
				if(rst.next())
					ok=true;
				rst.close();
				if(!ok) throw new Exception("已经完成审核的考核表不能重新填写");
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
			if(pst1!=null){
				try{pst1.close();}catch(Exception ex){}
			}
		}
	}
}
