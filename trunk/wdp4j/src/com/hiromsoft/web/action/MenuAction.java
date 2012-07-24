package com.hiromsoft.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;



public class MenuAction extends Action {
		
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
		
		String defaultMenu="/jsp/frameset/menus/blank.jsp";
		String menu=null;
		
		User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
		
		//1 老师 2 学院教书秘书 3 人事处科员 4人事处领导
		if(user!=null){
			if(user.contains(User.TY_ROLE, "1"))
				menu="/jsp/frameset/menus/liaoshi/laoshi.jsp";
			else if(user.contains(User.TY_ROLE, "2"))
				menu="/jsp/frameset/menus/liaoshi/mishu.jsp";
			else if(user.contains(User.TY_ROLE, "3"))
				menu="/jsp/frameset/menus/liaoshi/keyuan.jsp";
			else if(user.contains(User.TY_ROLE, "4"))
				menu="/jsp/frameset/menus/liaoshi/chuzhang.jsp";
					
		}
		if(menu==null||menu.length()==0)
			menu=defaultMenu;
		ActionForward forward=new ActionForward(menu);
		forward.setRedirect(false);
		return forward;
	}
}
