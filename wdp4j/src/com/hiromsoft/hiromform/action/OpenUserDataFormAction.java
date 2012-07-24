//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.8.2/xslt/JavaClass.xsl

package com.hiromsoft.hiromform.action;


import java.io.Serializable;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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

import com.hiromsoft.acl.Acl;
import com.hiromsoft.hiromform.HiromTokenProcessor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.LobUtils;
import com.hiromsoft.utils.StrutsUtil;

public class OpenUserDataFormAction extends Action {

	private Logger logger=Logger.getLogger(OpenUserDataFormAction.class);
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
		
		String pagename=this.doOpenAction(mapping, form, request, response);
		ActionForward forward = new ActionForward(pagename);
		forward.setRedirect(false);
		return forward;
	}
	public String doOpenAction(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response){
		
		String pagename=null;
		UserDataForm designedForm = (UserDataForm) form;
		StrutsUtil.emptyCustomerData(designedForm.getMap());
		
		String templateId=(String)designedForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0){
			templateId=request.getParameter("templateId");
			if(templateId==null||templateId.length()==0)
			{
				templateId=request.getParameter("TEMPLATEID");
			}
		}
		if(templateId==null||templateId.length()==0)
			throw new UnsupportedOperationException("未指定要编辑的template的Id，正确使用方法：\n"+
						"http://host:port/app/editWebForm.do?templateId=111&id=1143434");
		
		String ID=request.getParameter("id");
		
		if(ID==null||ID.length()==0)
		{
			ID=request.getParameter("ID");
			if(ID==null||ID.length()==0){
				ID=request.getParameter("Id");
				if(ID==null||ID.length()==0){
					ID=request.getParameter("iD");
				}
			}
		}
		if(ID==null||ID.length()==0)
			ID=(String)designedForm.getValue("__id");
		
		//System.out.println("ID="+ID);
		if(ID==null||ID.length()==0)
			throw new UnsupportedOperationException("未指定要编辑的数据的ID，正确使用方法：\n"+
										"http://host:port/app/editWebForm.do?template=123&id=23232323");
		
		Session dbsession = null;
		Transaction trans = null;
		try {
			HiromTokenProcessor.getInstance().saveToken(request, response);
			String entityName="designer_userform"+templateId;
		    Serializable id2=null;
			dbsession = com.hiromsoft.utils.DatabaseUtil.getHibernateSession();
			ClassMetadata meta=dbsession.getSessionFactory().getClassMetadata(entityName);
			if(meta!=null)
			{
			    Type type= meta.getIdentifierType();
			    if(type.getName().equals(Hibernate.INTEGER.getName()))
			        id2=new Integer(ID);
			    else if(type.getName().equals(Hibernate.LONG.getName()))
			        id2=new Long(ID);
			    else if(type.getName().equals(Hibernate.STRING.getName()))
			        id2=ID;
			}
			MapProxy retobj=(MapProxy)dbsession.load(entityName,id2);
			//retobj.getHibernateLazyInitializer().initialize();
			Iterator it=retobj.keySet().iterator();
			while(it.hasNext()){
				String keyname=(String)it.next();
				designedForm.setValue(keyname,retobj.get(keyname));
			}
			//加载大字段信息（clob）
			meta=dbsession.getSessionFactory().getClassMetadata(entityName+"_clob");
			if(meta!=null){
				String fields[]=meta.getPropertyNames();
				MapProxy clobobj=(MapProxy)dbsession.load(entityName+"_clob", id2);
				for(int num=0;num<fields.length;num++){
					String tmp=fields[num].toLowerCase();
					if(!tmp.equals("id")){
			            designedForm.setValue(fields[num],LobUtils.getContent(clobobj, fields[num]));
					}
				}
			}
			
			designedForm.setValue("__entityname", entityName);
			designedForm.setValue("lock", "none");
			designedForm.setValue("formname", templateId);
			designedForm.setValue("__url", "/jsp/hiromform/_genareted/template"+templateId+".jsp");
			designedForm.setValue("__url_readonly", "/jsp/hiromform/_genareted/template"+templateId+"_readonly.jsp");
			
			it=null;
			retobj=null;
			
			try{
				IFormStateLoader loader=(IFormStateLoader)(Class.forName("com.hiromsoft.hiromform.action.formstateloader."+entityName+"_loader").newInstance());
				if(loader!=null){
					loader.load(request, response, dbsession, designedForm);
				}
			}catch(java.lang.ClassNotFoundException ex){}
			
			String fid=entityName.replaceAll("designer_userform","");
			String authcode=Acl.getAclForFormStore(dbsession, fid, request);
			request.setAttribute(Global.HIROM_OBJECT_ACL, authcode);
			if(Integer.parseInt(authcode)<=7){
				request.setAttribute(Global.HIROM_READONLY, "1");
			}
			if(designedForm.getBooleanValue("sys_lockforever")){
				request.setAttribute(Global.HIROM_READONLY, "1");
			}
			designedForm.setValue("__formacl", authcode);
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally{
			
			if(dbsession!=null)
			{
				try{
					dbsession.close();
				}catch(Exception ex)
				{
				}
			}
		}
		
		pagename=this.getPageName(designedForm, request);
		return pagename;
	}
	
	public String getPageName(UserDataForm designedForm,HttpServletRequest request){
		String pagename=(String)designedForm.getValue("__hirompage");// 自定义的显示页面
		String templateId=(String)designedForm.getValue("__templateId");
		if(logger.isDebugEnabled()){
			logger.info(pagename);
		}
		if(pagename==null||pagename.length()==0){
			//没有指定个性页面，使用默认的
			String flag=(String)designedForm.getValue("__readonly");//0 false editable 1 true readonly
			if(flag==null||flag.length()==0) flag="1";//默认不能修改
			if(flag.equals("0")){
				pagename="/jsp/hiromform/editUserDataForm.jsp";
			}else{
				designedForm.setValue("lock", "all");
				pagename="/jsp/hiromform/readUserDataForm.jsp";
				designedForm.setValue("__url_readonly", "/jsp/hiromform/_genareted/template"+templateId+"_readonly.jsp");
			}

		}else{
			pagename="/jsp/"+pagename+".jsp";
		}
		return pagename;
	}

}