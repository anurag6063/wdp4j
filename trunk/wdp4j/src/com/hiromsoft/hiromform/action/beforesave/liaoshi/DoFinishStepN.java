package com.hiromsoft.hiromform.action.beforesave.liaoshi;

import java.sql.PreparedStatement;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;
import com.hiromsoft.hiromview.action.ShowListViewAction;
import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;

public class DoFinishStepN extends BaseOnSaveAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		String templateId=(String)userDataForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0)
		{
			templateId=((String)userDataForm.getValue("__entityname")).replaceAll("designer_userform", "");
		}
		String formIds[]={"28","29","31","32","33","35","36","41","42","130","44","46","49"};
		String fields[]={"field4","field5","field5","field5","field5","field5","field6","field7",
				"field8","field9","field10","field11","field12"};
		
		PreparedStatement pst=null;
		try{
			String fieldname="";
			for(int num=0;num<formIds.length;num++){
				if(formIds[num].equals(templateId))
				{
					fieldname=fields[num];
					break;
				}
			}
			if(fieldname.length()==0) return;
			
			String sql="update designer_userform27 set "+fieldname + " ='1' where field1=${user.yhbh} and field13=${__khbh}";
			if("49".equals(templateId))
				sql="update designer_userform27 set "+fieldname + " ='2' where field1=${user.yhbh} and field13=${__khbh}";
			pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
			pst.execute();
			pst.close();
			pst=null;
			
		}finally{
			try{
				if(pst!=null)
					pst.close();
			}catch(Exception ex){}
		}
	}
	
	public void doCreateChildObject(javax.servlet.http.HttpServletRequest request,javax.servlet.http.HttpServletResponse response,
			org.hibernate.Session dbsession,com.hiromsoft.hiromform.UserDataForm userDataForm,
			Map childobj) throws Exception{
		
		String templateId=(String)userDataForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0)
		{
			templateId=((String)userDataForm.getValue("__entityname")).replaceAll("designer_userform", "");
		}
		dbsession.saveOrUpdate((String)childobj.get("__entityname"), childobj);
		if(templateId==null||!templateId.equals("130")) return;
		
		User  user=(User)request.getSession().getAttribute(Global.HIROM_USER);
		String Gcode=Utils.getSequenceValue(dbsession, "seq_f130_GCODE")+"";
		childobj.put("code", Gcode);
		childobj.put("active", "1");
		childobj.put("type", "2");
		childobj.put("field0", user.getDepartment());
		
		dbsession.saveOrUpdate((String)childobj.get("__entityname"), childobj);
		
		String GID=(String)childobj.get("ID");
		userDataForm.setValue("GID", GID);
		userDataForm.setValue("GCODE", Gcode);
		dbsession.saveOrUpdate((String)userDataForm.getValue("__entityname"), userDataForm.getMap());
	}

}
