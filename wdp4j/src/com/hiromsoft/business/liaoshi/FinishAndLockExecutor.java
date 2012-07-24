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

public class FinishAndLockExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		//int maxrows=100;
		//String[] ids=new String[maxrows];
		//int rowidx=RequestUtil.getListSelection(request, ids);
		
		PreparedStatement pst=null;

		try{
			String tableIds[]=new String[]{"29","31","32","33","35","36","41","42","44","146","150"};
			
			for(int num=0;num<tableIds.length;num++){
				pst=dbsession.connection().prepareStatement("update designer_userform"+tableIds[num]+" set sys_lockforever='1'");
				pst.execute();
				pst.close();
				pst=null;
			}
			
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
		}
	}
}
