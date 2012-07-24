package com.hiromsoft.hiromform.action;



import java.io.Serializable;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.MapProxy;
import org.hibernate.type.Type;


import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;


public class DeleteUserDataFormAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) {

		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		
		String forward=null;
		
		UserDataForm webForm = (UserDataForm) form;
		
		String templateId=(String)webForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0){
			templateId=request.getParameter("templateId");
			if(templateId==null||templateId.length()==0)
			{
				templateId=request.getParameter("TEMPLATEID");
			}
		}
		if(templateId==null||templateId.length()==0)
			throw new UnsupportedOperationException("the deleting webform's templateId is expected, please correct it like this:\n"+
						"http://host:port/app/editWebForm.do?templateId=10129&id=1143434");
		
		//最多删除100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);

		Session dbsession = null;
		Transaction trans = null;
		PreparedStatement pst=null;
		try {
		    Serializable id2=null;
		    String entityName="designer_userform"+templateId;
			dbsession = com.hiromsoft.utils.DatabaseUtil.getHibernateSession();
			trans=dbsession.beginTransaction();
			ClassMetadata meta=dbsession.getSessionFactory().getClassMetadata(entityName);
			
			IDeleteFormActionExecutor beforeDelete=null;
			IDeleteFormActionExecutor afterDelete=null;
			String beforedelete=request.getParameter("beforedelete");
			if(beforedelete!=null&&beforedelete.length()!=0){
				beforeDelete=(IDeleteFormActionExecutor)(Class.forName("com.hiromsoft.hiromform.action.beforedelete."+beforedelete).newInstance());
			}
			String afterdelete=request.getParameter("afterdelete");
			if(afterdelete!=null&&afterdelete.length()!=0){
				afterDelete=(IDeleteFormActionExecutor)(Class.forName("com.hiromsoft.hiromform.action.afterdelete."+afterdelete).newInstance());
			}
			for(int num=0;num<rowidx;num++){
				
				if(meta!=null)
				{
				    Type type= meta.getIdentifierType();
				    if(type.getName().equals(Hibernate.INTEGER.getName()))
				        id2=new Integer(ids[num]);
				    else if(type.getName().equals(Hibernate.LONG.getName()))
				        id2=new Long(ids[num]);
				    else if(type.getName().equals(Hibernate.STRING.getName()))
				        id2=ids[num];
				}
				
				MapProxy proxy=(MapProxy)dbsession.load(entityName, id2);
				if(proxy==null) continue;
				if(beforeDelete!=null){
					try{
						beforeDelete.execute(request, response, dbsession, webForm,proxy);
					}catch(StopActionException ex){
						continue;
					}
				}
				String lock=(String)proxy.get("sys_lockforever");
				if(lock!=null&&lock.equals("1")) throw new Exception("试图删除不可以删除的数据，操作终止."); 
				dbsession.delete(proxy);
				if(afterDelete!=null){
					afterDelete.execute(request, response, dbsession, webForm);
				}
			}
			
			/*
			boolean executable=false;
			pst=dbsession.connection().prepareStatement("delete from "+entityName+" where id=?");
			for(int num=0;num<rowidx;num++){
				pst.setString(1, ids[num]);
				executable=true;
				pst.addBatch();
			}
			if(executable)
				pst.executeBatch();
			pst.close();
			pst=null;
			*/
			trans.commit();
		}catch(Exception e)
		{
			if(trans!=null)
			{
				try{
					trans.rollback();
				}catch(Exception ex)
				{}
			}
			e.printStackTrace();
			forward="failure";
			request.setAttribute(Global.HIROM_MSG_ERROR, e.getMessage());
		}finally{
			if(pst!=null){
				try{
					pst.close();
				}catch(Exception ex){}
				
			}
			if(dbsession!=null)
			{
				try{
					dbsession.close();
				}catch(Exception ex)
				{
				}
			}
		}
		
		ActionForward retval=null;
		
		if(forward==null){
			forward=(String)webForm.getValue("__hirompage");
			if(forward!=null&&forward.length()!=0){
				forward="/jsp/"+forward+".jsp";
				retval=new ActionForward(forward);
			}else{
				retval=mapping.findForward("success");
			}
			
		}else{
			retval=mapping.findForward(forward);
		}
		
		return retval;
	}
	

}