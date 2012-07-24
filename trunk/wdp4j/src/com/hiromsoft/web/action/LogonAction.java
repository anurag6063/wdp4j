package com.hiromsoft.web.action;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Session;
import org.hibernate.Transaction;


import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;
import com.hiromsoft.web.modal.User;



public class LogonAction extends Action {

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
		
		String username=request.getParameter("username");
		String password=request.getParameter("password");
		String randomcode=request.getParameter("randomcode");
		
		ActionForward forward=null;
		Session dbsession = null;
		PreparedStatement pst=null;
    	Connection conn=null;
    	Transaction trans=null;
		try {
			
			String tmp=RequestUtil.getUserRandomCode(request.getSession());
			if(randomcode==null||!randomcode.equals(tmp))
				throw new Exception("验证码错误。");
			
			dbsession = DatabaseUtil.getHibernateSession();
			trans=dbsession.beginTransaction();
			conn=dbsession.connection();
			String oldmm="";
			String ID="";
			boolean inuse=false;
			String sql="select field9,ID,inuseto from sys_users where dlzh=? and inuseto is null order by inuseto asc";
			pst=conn.prepareStatement(sql);
			pst.setString(1, username);
			ResultSet rst=pst.executeQuery();
			while(rst.next()){
				Object tmp2=rst.getDate(3);
				if(tmp2!=null) continue;
				inuse=true;
				oldmm=rst.getString(1);
				ID=rst.getString(2);
			}
			rst.close();
			pst.close();
			pst=null;
			
			if(!inuse){
				throw new Exception("账号已经停用或用户帐号不存在。");
			}
			
			if(password.equals(oldmm)){
				setSessions(request,response,dbsession,ID);
				forward=new ActionForward(Global.HOME_URL);
				forward.setRedirect(true);
			}else{
				throw new Exception("用户名或密码错误。");
			}
			trans.commit();
			
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
	public void setSessions(HttpServletRequest request,HttpServletResponse response,Session dbsession, String userId) throws Exception{
		
		User user=new User();
		HttpSession session=request.getSession();
		session.setAttribute(Global.HIROM_USER,user);
		
		PreparedStatement pst=null;
		PreparedStatement pst2=null;
    	Connection conn=null;
		try {
			String khbh="";
			conn=dbsession.connection();
			
			String sql="select yhbh,field10,xm,dlzh from sys_users where ID=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1, userId);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				user.setID(userId);
				user.setYhbh(rst.getString(1));
				user.setDepartment(rst.getString(2));
				user.setDisplayName(rst.getString(3));
				user.setLogonName(rst.getString(4));
			}
			rst.close();
			pst.close();
			pst=null;
			
			sql="select code from designer_userform47 where zhuangtai='1'";
			pst=conn.prepareStatement(sql);
			rst=pst.executeQuery();
			if(rst.next()){
				khbh=rst.getString(1);
			}
			rst.close();
			pst.close();
			pst=null;
			
			if(khbh!=null&&khbh.length()!=0)
				session.setAttribute("__khbh",khbh);
			
			sql="select varname,varvalue,type from sys_envvars where ucode=?";
			pst=conn.prepareStatement(sql);
			pst2=conn.prepareStatement("select ordercode from sys_depts where id=?");
			pst.setString(1,user.getYhbh());
			rst=pst.executeQuery();
			while(rst.next()){
				user.setAttribute(rst.getString(1), rst.getString(2));
				String type=rst.getString(3);
				if(type!=null&&type.equals("0")){
					pst2.setString(1, rst.getString(2));
					ResultSet rst2=pst2.executeQuery();
					if(rst2.next()){
						user.setAttribute(rst.getString(1)+"_ORDERCODE", rst2.getString(1));
					}
					rst2.close();
				}
			}
			rst.close();
			pst.close();
			pst=null;
			
			pst2.close();
			pst2=null;
			
			Cookie cookie=new Cookie("USERID",userId);
			cookie.setPath(request.getContextPath());
			
			response.addCookie(cookie);
			
			sql="select rcode from sys_role_users where uhid=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1,user.getID());
			rst=pst.executeQuery();
			int num=0;
			while(rst.next()&&num<5){
				user.getRoles()[num]=rst.getString(1);
				num++;
			}
			rst.close();
			pst.close();
			pst=null;
			
			sql="select gcode from sys_groupandteam_users where uhid=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1,user.getID());
			rst=pst.executeQuery();
			num=0;
			while(rst.next()&&num<5){
				user.getGroups()[num]=rst.getString(1);
				num++;
			}
			rst.close();
			pst.close();
			pst=null;
			
			
		}finally{
			if(pst!=null)
				try{pst.close();}catch(Exception ex){}
			if(pst2!=null)
					try{pst2.close();}catch(Exception ex){}
			
		}
				
	}
}
