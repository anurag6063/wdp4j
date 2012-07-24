package com.hiromsoft.hiromform.action;

import java.io.Reader;
import java.io.Serializable;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.lob.SerializableClob;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.MapProxy;
import org.hibernate.type.Type;

import com.hiromsoft.acl.Acl;
import com.hiromsoft.hiromform.HiromTokenProcessor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromview.HtmlViewTemplate;
import com.hiromsoft.hiromview.action.ShowListViewAction;
import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;
import com.hiromsoft.utils.StrutsUtil;
import com.hiromsoft.utils.LobUtils;

public class SearchUserDataFormAction extends Action {

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
		
		HiromTokenProcessor.getInstance().saveToken(request, response);
		
		UserDataForm designedForm = (UserDataForm) form;
		StrutsUtil.emptyCustomerData(designedForm.getMap());
		
		String viewid=(String)designedForm.getValue("__viewid");
		if(viewid==null||viewid.length()==0){
			viewid=request.getParameter("viewid");
			if(viewid==null||viewid.length()==0)
			{
				viewid=request.getParameter("VIEWID");
			}
		}
		if(viewid==null||viewid.length()==0)
			throw new UnsupportedOperationException("未指定viewid");
		
		String url="/jsp/information.jsp";
		try{
			String[] viewids=viewid.split(",");
			this.doFindAction(mapping, form, request, response, viewids,true,true,false);
			String templateId=designedForm.getStringValue("__templateId");
			designedForm.setValue("__url", "/jsp/hiromform/_genareted/template"+templateId+".jsp");
			designedForm.setValue("__url_readonly", "/jsp/hiromform/_genareted/template"+templateId+"_readonly.jsp");
			String tmp=designedForm.getStringValue("__hirompage");  //value(__hirompage)=
			if(tmp!=null&&tmp.length()!=0){
				url="/jsp/"+tmp+".jsp";
			}else{
//				没有指定个性页面，使用默认的
				String flag=(String)designedForm.getValue("__readonly");//0 false editable 1 true readonly
				if(flag==null||flag.length()==0) flag="1";//默认不能修改
				if(flag.equals("0")){
					url="/jsp/hiromform/editUserDataForm.jsp";
				}else{
					designedForm.setValue("lock", "all");
					url="/jsp/hiromform/readUserDataForm.jsp";
				}
			}
		}catch(NotFoundException ex){
			RequestUtil requestUtil=new RequestUtil();
			String strnotfound=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, designedForm, "notfound"); 
			if("create".equals(strnotfound)){
				url="/createExcel.sv";
			}else{
				request.setAttribute(Global.HIROM_MSG_ERROR, ex.getMessage());
			}
		}catch(Exception ex){
			ex.printStackTrace();
			request.setAttribute(Global.HIROM_MSG_ERROR, ex.getMessage());
		}
		ActionForward forward = new ActionForward(url);
		return forward;
		
	}
	public void doFindAction(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response,String[] viewids,boolean loadFormState,boolean loadAcl) throws Exception{
		this.doFindAction(mapping, form, request, response, viewids, loadFormState, loadAcl, false);
	}
	public void doFindAction(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response,String[] viewids,boolean loadFormState,boolean loadAcl,boolean allowNotFound) throws Exception{
		
		UserDataForm designedForm = (UserDataForm) form;

		RequestUtil requestUtil=null;
		Session dbsession = null;
		Transaction trans = null;
		try {
			dbsession = DatabaseUtil.getHibernateSession();
			trans=dbsession.beginTransaction();
			for(int num=0;num<viewids.length;num++){
				if(num==viewids.length-1){
					if(requestUtil==null) requestUtil=new RequestUtil();
					String strnotfound_executor=requestUtil.getParameterValueFromRequestOrUserDataFrom(request, designedForm, "notfound_executor");
					this.executePrevFinder(dbsession, request, response, viewids[num], designedForm, requestUtil, strnotfound_executor, false,allowNotFound);
				}else
					this.executePrevFinder(dbsession, request, response, viewids[num], designedForm, requestUtil, null, true,allowNotFound);
			}
			
			String entityName=designedForm.getStringValue("__entityname");
			String ID=designedForm.getStringValue("ID");
			String templateId=designedForm.getStringValue("__templateId");
			
			if(ID!=null&&ID.length()!=0)
				this.loadObject(dbsession, entityName, ID, templateId, designedForm);
			if(loadFormState){
				try{
					IFormStateLoader loader=(IFormStateLoader)(Class.forName("com.hiromsoft.hiromform.action.formstateloader."+entityName+"_loader").newInstance());
					if(loader!=null){
						loader.load(request, response, dbsession, designedForm);
					}
				}catch(java.lang.ClassNotFoundException ex){}
			}
			if(loadAcl){
				String fid=designedForm.getStringValue("__fid");
				String authcode=Acl.getAclForFormStore(dbsession, fid, request);
				request.setAttribute(Global.HIROM_OBJECT_ACL, authcode);
				if(Integer.parseInt(authcode)<=7){
					request.setAttribute(Global.HIROM_READONLY, "1");
				}
				designedForm.setValue("__formacl", authcode);
			}
			trans.commit();
		}catch(Exception ex){
			if(trans!=null) try{trans.rollback();}catch(Exception ex2){ex2.printStackTrace();}
			//if(dbsession!=null)	try{ dbsession.close();	dbsession=null;}catch(Exception ex2){}
			throw ex;
		}finally{
			if(dbsession!=null)	try{ dbsession.close();	}catch(Exception ex){}
		}
		
	}
	private void loadObject(Session dbsession,String entityName,String ID,String templateId,UserDataForm designedForm) throws Exception{
		Serializable id2=null;
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
		Map retobj=(Map)dbsession.load(entityName,id2);

		boolean ischildform=designedForm.getBooleanValue("__ischildform");
		
		Iterator it=retobj.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry entry=(Map.Entry)it.next();
			String keyname=entry.getKey().toString();
			if(ischildform)
				keyname=keyname+"_f"+templateId;
			designedForm.setValue(keyname,entry.getValue());
		}
		it=null;
		retobj=null;
		
		//加载大字段信息（clob）
		meta=dbsession.getSessionFactory().getClassMetadata(entityName+"_clob");
		if(meta==null) return;
		
		String fields[]=meta.getPropertyNames();
		MapProxy clobobj=(MapProxy)dbsession.load(entityName+"_clob", id2);
		for(int num=0;num<fields.length;num++){
			String tmp=fields[num].toLowerCase();
			if(!tmp.equals("id")){
				SerializableClob clob2 = (SerializableClob) clobobj.get(fields[num]);
				if(clob2!=null){
					Clob clob3 = clob2.getWrappedClob();
		            Reader reader =LobUtils.getCharReader(clob3);
		            StringBuffer aa=new StringBuffer("");
		            char[] buffer = new char[1024 * 4];
		            while (true) {
		                int len = reader.read(buffer);
		                if (len < 1)
		                    break;
		                else{
		                    aa.append( buffer, 0, len);
		                }
		            }
		            reader.close();
		            if(ischildform)
		            	designedForm.setValue(fields[num]+"_f"+templateId,aa);
		            else
		            	designedForm.setValue(fields[num],aa);
				}
			}
		}

		
	}
	private void executePrevFinder(Session dbsession,HttpServletRequest request,HttpServletResponse response,String viewid,UserDataForm designedForm,
			RequestUtil requestUtil,String strnotfound_executor,boolean onlysearch,boolean allowNotFound) throws Exception{
		HtmlViewTemplate htmlviewtemplate=new HtmlViewTemplate();
		dbsession.load(htmlviewtemplate, new Integer(viewid));
		
//		查询并获取主键ID
		Connection cn=dbsession.connection();
		String sql="select ID from "+htmlviewtemplate.getTableName();
		if(htmlviewtemplate.getFilter()!=null)
		{
			sql=sql+" where "+htmlviewtemplate.getFilter();
		}
		ShowListViewAction list=new ShowListViewAction();
		PreparedStatement pst=null;
		String ID=null;
		try{
			pst=list.createStatementAndParameters(request, cn, sql);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				ID=rst.getString(1);
			}
			rst.close();
			pst.close();
			pst=null;
		}finally{
			if(pst!=null) try{pst.close();}catch(Exception ex){}
		}
		//先保存该ID，以便于找不到的时候转向创建页面
		String templateId=htmlviewtemplate.getEditorFormId();
		if(templateId==null||templateId.length()==0||templateId.equals("0")){
			if(htmlviewtemplate.getFid()!=null&&htmlviewtemplate.getFid().length()!=0){
				templateId=htmlviewtemplate.getFid();
			}
			if(templateId==null||templateId.length()==0||templateId.equals("0")){
				throw new Exception("该视图参数设置有误，未指定主表单ID或者编辑器ID");
			}
		}
		
		designedForm.setValue("__templateId", templateId);
		
//		没有查询到记录，若用户指定了没有查询的notfount_executor参数，则执行该参数
		if(ID==null||ID.length()==0){
			if(strnotfound_executor!=null&&strnotfound_executor.length()!=0){
				IFormActionExecutor executor=(IFormActionExecutor)(Class.forName("com.hiromsoft.hiromform.action.notfound."+strnotfound_executor).newInstance());
				if(executor!=null){
					designedForm.setValue("__listview", htmlviewtemplate);
					executor.execute(request, response, dbsession, designedForm);
					ID=(String)designedForm.getValue("__newid");
				}
			}
			String msg="未找到满足条件的记录。";
			if(htmlviewtemplate.getNotfoundMsg()!=null&&htmlviewtemplate.getNotfoundMsg().length()!=0)
				msg=htmlviewtemplate.getNotfoundMsg();
			if(ID==null||ID.length()==0) {
				if(!allowNotFound)	throw new NotFoundException(msg);
			}
		}
		if(onlysearch) return; 
			
		
		
		String entityName="designer_userform"+templateId;
		
		designedForm.setValue("lock", "none");
		designedForm.setValue("formname", templateId);
		designedForm.setValue("__entityname", entityName);
		designedForm.setValue("ID", ID);
		designedForm.setValue("__fid", htmlviewtemplate.getFid());
		
		String tmp=designedForm.getStringValue("__hirompage");  //value(__hirompage)=
		if(tmp!=null&&tmp.length()!=0) return;
		
		if(htmlviewtemplate.getTemplateName()!=null&&htmlviewtemplate.getTemplateName().length()!=0)
			designedForm.setValue("__hirompage",htmlviewtemplate.getTemplateName());
		else{
			if(htmlviewtemplate.getModName()!=null&&htmlviewtemplate.getModName().length()!=0&&
					htmlviewtemplate.getPageName()!=null&&htmlviewtemplate.getPageName().length()!=0){
				designedForm.setValue("__hirompage",htmlviewtemplate.getModName()+"/"+htmlviewtemplate.getPageName());
			}
			
		}
		
	}

}