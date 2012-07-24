package com.hiromsoft.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Vector;

import org.hibernate.Query;
import org.hibernate.Session;

import com.hiromsoft.hiromform.HtmlFormTemplate;
import com.hiromsoft.hiromform.TemplateField;
import com.hiromsoft.hiromview.HtmlViewTemplate;

public class UserViewUtil {

	public void createViewTemplateFromFormTemplate(Session dbsession,HtmlFormTemplate htmlform) throws Exception{
		
		HtmlViewTemplate template=saveViewTemplate(dbsession,htmlform);
		buildJsp(dbsession,template);
		
	}
	public void buildJsp(Session dbsession,HtmlViewTemplate template) throws Exception{
		
	}
	public HtmlViewTemplate saveViewTemplate(Session dbsession,HtmlFormTemplate htmlform) throws Exception{
		
		
		
		HtmlViewTemplate template=null;//new HtmlViewTemplate();
		PreparedStatement pst=null;
		PreparedStatement pst2=null;
		PreparedStatement pst3=null;

		try{
			boolean isNew=true;
			Query query=dbsession.createQuery("select htmluserview from HtmlViewTemplate htmluserview where htmluserview.type=:tp and htmluserview.tableName=:tn and htmluserview.fid=:fid");
			query.setString("tp", "0");//0system 1user-defained
			query.setString("tn", htmlform.getTableName());
			query.setString("fid", Integer.toString(htmlform.getFormId()));
			
			Iterator it=query.iterate();
			if(it.hasNext()){
				isNew=false;
				template=(HtmlViewTemplate)it.next();
			}
			query=null;
			if(isNew) {
				template=new HtmlViewTemplate();
				template.setTableName(htmlform.getTableName());
				template.setViewName(htmlform.getName());
				template.setFid(Integer.toString(htmlform.getFormId()));
				template.setType("0"); //0 system
			}
			
			dbsession.saveOrUpdate(template);
			dbsession.flush();
			
		
	//		/将视图的字段信息加入到sys_views_fields表中
			it=htmlform.getFields().iterator();
			Connection cn=dbsession.connection();

			Vector oldFields=new Vector();
			pst=cn.prepareStatement("select fieldName from sys_views_fields where viewid=?");
			pst.setInt(1, template.getViewID());
			ResultSet rst=pst.executeQuery();
			while(rst.next()){
				oldFields.add(rst.getString(1));
			}
			rst.close();
			pst.close();
			pst=null;
			
			int serialNumber=oldFields.size()+1;
			String tmp=null;
			while(it.hasNext()){
			    TemplateField htmltag = (TemplateField) it.next();
	            String fieldname = htmltag.getFieldname();
	            String fieldtype = htmltag.getDatatype();
	            String datatype = "C";
	            if (fieldtype != null) {
	                if (fieldtype.equals("numeric")){
	                    datatype = "N";
	                }else if (fieldtype.equals("date")){
	                    datatype = "D";
	                }
	            }
	            String displayName=htmltag.getTitle();
	            if(displayName==null||displayName.length()==0)
	                displayName=fieldname;
	            boolean updated=false;
	            for(int num=0;num<oldFields.size();num++){
	            	tmp=oldFields.get(num).toString();
	            	if(tmp.equals(fieldname)){
	            		oldFields.remove(num);
	            		if(pst2==null)
	            			pst2=cn.prepareStatement("update sys_views_fields set title=?,datatype=? where viewid=? and fieldname=?");
	            		pst2.setString(1, displayName);
	            		pst2.setString(2, datatype);
	            		pst2.setInt(3, template.getViewID());
	            		pst2.setString(4, fieldname);
	            		pst2.addBatch();
	            		updated=true;
	            		break;
	            	}
	            }
	            if(updated) continue;
	            
	            if(pst==null)
	            	pst=cn.prepareStatement("insert into sys_views_fields(viewid,fieldName,title,datatype,display,sn,id) values (?,?,?,?,?,?,?)");
	            pst.setInt(1,template.getViewID());
	            pst.setString(2,fieldname);
	            pst.setString(3,displayName);
	            pst.setString(4,datatype);
	            pst.setString(5,"1");
	            pst.setInt(6, serialNumber);
	            pst.setString(7, RequestUtil.GenerateUUID());
	            pst.addBatch();
	            serialNumber++;    
			}
			for(int num=0;num<oldFields.size();num++){
				if(pst3==null)
					pst3=cn.prepareStatement("delete from sys_views_fields where viewid=? and fieldname=?");
				pst3.setInt(1, template.getViewID());
				pst3.setString(2, oldFields.get(num).toString());
				pst3.addBatch();
			}
			if(pst3!=null){
				pst3.executeBatch();
				pst3.close();
				pst3=null;
			}
			
			if(pst2!=null){
				pst2.executeBatch();
				pst2.close();
				pst2=null;
			}
			
			
			if(pst!=null){
				pst.executeBatch();
				pst.close();
				pst=null;
			}
			dbsession.flush();
		
		}finally{
			try{
				if(pst!=null)
					pst.close();
			}catch(Exception ex){}
			
			try{
				if(pst2!=null)
					pst2.close();
			}catch(Exception ex){}
			
			try{
				if(pst3!=null)
					pst3.close();
			}catch(Exception ex){}
		}
		return template;
	}
	
}
