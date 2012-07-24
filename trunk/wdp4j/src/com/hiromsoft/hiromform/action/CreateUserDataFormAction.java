//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.8.2/xslt/JavaClass.xsl

package com.hiromsoft.hiromform.action;

import java.util.HashMap;
import com.hiromsoft.acl.Acl;
import com.hiromsoft.hiromform.HiromTokenProcessor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.StrutsUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CreateUserDataFormAction extends Action {

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog( CreateUserDataFormAction.class );
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

		UserDataForm designedForm = (UserDataForm) form;
		emptyCustmerData(designedForm.getMap());
		
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		
		if(log.isInfoEnabled())
			log.info("Enter new WebForm Action");
		
		String templateId=(String)designedForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0){
			templateId=request.getParameter("templateId");
			if(templateId==null||templateId.length()==0)
			{
				templateId=request.getParameter("TEMPLATEID");
			}
		}
		if(templateId==null||templateId.length()==0)
			throw new UnsupportedOperationException("webform's template name is expected, please try again like this: http://host:port/app/newWebForm.do?templateId=1009");
		
	
		designedForm.setValue("ID", "0");
		designedForm.setValue("lock", "none");
		designedForm.setValue("formname", templateId);
		designedForm.setValue("__url", "/jsp/hiromform/_genareted/template"+templateId+".jsp");
		Session dbsession = null;
		Transaction trans = null;
		try {
			HiromTokenProcessor.getInstance().saveToken(request, response);	
			dbsession = com.hiromsoft.utils.DatabaseUtil.getHibernateSession();
			String fid=templateId;
			String authcode=Acl.getAclForFormStore(dbsession, fid, request);
			request.setAttribute(Global.HIROM_OBJECT_ACL, authcode);
			if(Integer.parseInt(authcode)<=7){
				request.setAttribute(Global.HIROM_READONLY, "1");
			}
			designedForm.setValue("__formacl", authcode);
			
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
		
		ActionForward forward = new ActionForward(getPageName(designedForm));
		forward.setRedirect(false); //forward,not redirect
		
		return forward;
	}
	
	private void emptyCustmerData(HashMap map){
		String gooncreate=(String)map.get("__gooncreate");
		if(gooncreate!=null&&"1".equals("1")){
			StrutsUtil.emptyCustomerData2(map);
		}else	
			StrutsUtil.emptyCustomerData(map);
	}
	public String getPageName(UserDataForm designedForm){
		String pagename=(String)designedForm.getValue("__hirompage");// 自定义的显示页面
		if(pagename==null||pagename.length()==0){
			pagename="/jsp/hiromform/createUserDataForm.jsp";
		}else{
			pagename="/jsp/"+pagename+".jsp";
		}
		return pagename;
	}
	

}