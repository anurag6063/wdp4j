package com.hiromsoft.web.action;


import java.sql.PreparedStatement;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Session;
import org.hibernate.Transaction;


import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;



public class LogoffAction extends Action {

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
		
		ActionForward forward=null;
		Session dbsession = null;
		PreparedStatement pst=null;
    	Transaction trans=null;
		try {
			User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
			/*
			dbsession = DatabaseUtil.getHibernateSession();
			trans=dbsession.beginTransaction();
			conn=dbsession.connection();
			String sql="delete from designer_userform71 where sid=? and userid=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1, request.getSession().getId());
			pst.setString(2, user.getID());
			pst.execute();
			pst.close();
			pst=null;
			trans.commit();
			*/
			request.getSession().invalidate();
			
			Cookie cookie=new Cookie("USERID",user.getID());
			cookie.setPath(request.getContextPath());
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			
			forward=new ActionForward("/index.jsp");
			
		}catch(Exception e)
		{
			if(trans!=null)
				try{trans.rollback();}catch(Exception ex2){}
			e.printStackTrace();
			request.setAttribute(Global.HIROM_MSG_ERROR, e.getMessage());
			forward=new ActionForward("/index.jsp");
		}finally{
			if(pst!=null)
				try{pst.close();}catch(Exception ex){}
			if(dbsession!=null)
			{
				try{
					dbsession.close();
				}catch(Exception ex)
				{
				}
			}
		}
		return forward;
	}
	
}
