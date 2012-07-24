package com.hiromsoft.hiromform.action.beforedelete.system;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnDeleteAction;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;

public class DeleteHtmlTemplate extends BaseOnDeleteAction {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm,Map proxy) throws Exception {
		// TODO Auto-generated method stub
		
		long fid=((Long)proxy.get("fid")).longValue();
		String f1=Global.BASEPATH+RequestUtil.getRealPath("jsp/hiromform/_genareted/template")+fid;
		File file = new File(f1+".jsp");
        if (file.exists())
            file.delete();
        
        file = new File(f1+"_print.jsp");
        if (file.exists())
            file.delete();
        
        
        file = new File(f1+"_readonly.jsp");
        if (file.exists())
            file.delete();
        
        file = new File(f1+"_searchbar.jsp");
        if (file.exists())
            file.delete();
        
        String f2=Global.BASEPATH+RequestUtil.getRealPath("/WEB-INF/mappings/hiromforms/")+userDataForm.getStringValue("entityname");
        
        file = new File(f2+".hbm.xml");
        if (file.exists())
            file.delete();
        
        file = new File(f2+"_clob.hbm.xml");
        if (file.exists())
            file.delete();
        
        PreparedStatement pst=null;
        try{
        	pst=dbsession.connection().prepareCall("drop table "+userDataForm.getStringValue("tablename"));
        	pst.execute();
        	pst.close();
        	pst=null;
        }catch(Exception ex){
        	
        }finally{
        	if(pst!=null) try{pst.close();}catch(Exception ex){}
        }
        
	}
}
