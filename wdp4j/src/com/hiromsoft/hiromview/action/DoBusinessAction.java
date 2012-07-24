package com.hiromsoft.hiromview.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hiromsoft.business.IBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;

public class DoBusinessAction extends Action {
	
	public ActionForward execute(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		ActionForward retval=null;
		UserDataForm userDataForm =(UserDataForm)form;
		String executorName= new RequestUtil().getParameterValueFromRequestOrUserDataFrom(request, userDataForm, "__executor");
		if(executorName!=null&&executorName.length()!=0){
			String packageName="com.hiromsoft.business.";
			Session dbsession=null;
			Transaction trans=null;
			try{
				IBusinessExecutor executor=(IBusinessExecutor)(Class.forName(packageName+executorName).newInstance());
				if(executor!=null){
					dbsession=DatabaseUtil.findHibernateSession();
					trans=dbsession.beginTransaction();
					executor.execute(dbsession,userDataForm, request, response);
					
					String tmp=(String)userDataForm.getValue("__hirompage");
					if(tmp!=null&&tmp.length()!=0){
						retval=new ActionForward("/jsp/"+tmp+".jsp");
					}else{
						request.setAttribute(Global.HIROM_MSG_SECSESS, "²Ù×÷³É¹¦!");
						retval=mapping.findForward("success");
					}
					trans.commit();
				}
			}catch(Exception ex){
				if(trans!=null){
					try{
						trans.rollback();
					}catch(Exception ex2){}
				}
				ex.printStackTrace();
				request.setAttribute(Global.HIROM_MSG_ERROR, ex.getClass().getName()+": "+ex.getMessage());
				retval=mapping.findForward("failure");
			}finally{
				try{
					if(dbsession!=null)
						dbsession.close();
				}catch(Exception ex){}
				
			}
		}
		
		return retval;
	}

}
