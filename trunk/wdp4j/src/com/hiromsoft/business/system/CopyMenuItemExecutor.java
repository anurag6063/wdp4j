package com.hiromsoft.business.system;


import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.proxy.MapProxy;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.aftersave.system.AssignMenuItemToRoot;
import com.hiromsoft.utils.RequestUtil;

public class CopyMenuItemExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);

		try{
			String entityname="designer_userform121";
			for(int num=0;num<rowidx;num++){
				String ID=ids[num];
				MapProxy proxy=(MapProxy)dbsession.load(entityname, ID);
				HashMap newobj=new HashMap();
				newobj.putAll(proxy);
				newobj.put("ID", "0");
				newobj.put("name", (String)newobj.get("name")+"(复制)");
				dbsession.saveOrUpdate(entityname,newobj);
				
				AssignMenuItemToRoot assignment=new AssignMenuItemToRoot();
				assignment.doAssignAction(dbsession, (String)newobj.get("ID"));
				
			}
		}finally{
				
		}
	}
}
