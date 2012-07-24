package com.hiromsoft.business.system;

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

public class CopyViewsExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		
		PreparedStatement pst=null;
		PreparedStatement pst2=null;
		//"insert into sys_views_fields(viewid,fieldName,title,datatype,display,sn,id) "+
		//"select ,fieldName,title,datatype,display,sn,id"
		try{
			pst=dbsession.connection().prepareStatement("select id from sys_views_fields where viewid=?");
			pst2=dbsession.connection().prepareStatement("insert into sys_views_fields(viewid,fieldName,title,datatype,display,sn,id,field0,width) "+
														"select ?,fieldName,title,datatype,display,sn,?,field0,width from sys_views_fields where id=?");
			for(int num=0;num<rowidx;num++){
				MapProxy proxy=(MapProxy) dbsession.load("designer_userform1", ids[num]);
				if(proxy==null) continue;
				HtmlViewTemplate template=new HtmlViewTemplate();
				template.setGUID(RequestUtil.GenerateUUID());
				template.setEditorFormId((String)proxy.get("editorformid"));
				template.setFilter((String)proxy.get("filter"));
				template.setFlag((String)proxy.get("flag"));
				template.setModName((String)proxy.get("modname"));
				template.setPageName((String)proxy.get("pagename"));
				template.setTableName((String)proxy.get("tablename"));
				template.setSql1((String)proxy.get("sql1"));
				template.setViewName((String)proxy.get("name")+"(复制)");
				template.setType("1");
				template.setVersion((String)proxy.get("version"));
				template.setTemplateName((String)proxy.get("templatename"));
				template.setFid((String)proxy.get("fid"));
				template.setChildtablenames1((String)proxy.get("childtablenames1"));
				template.setChildtablenames2((String)proxy.get("childtablenames2"));
				template.setFid(proxy.get("FID").toString());
				
				dbsession.saveOrUpdate(template);
				dbsession.flush();
				
				boolean executebatch=false;
				
				pst.setInt(1, ((Long)proxy.get("viewID")).intValue());
				ResultSet rst=pst.executeQuery();
				while(rst.next()){
					String fid=rst.getString(1);
					pst2.setInt(1, template.getViewID());
					pst2.setString(2, RequestUtil.GenerateUUID());
					pst2.setString(3, fid);
					pst2.addBatch();
					executebatch=true;
				}
				rst.close();
				if(executebatch)
					pst2.executeBatch();
			}
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
