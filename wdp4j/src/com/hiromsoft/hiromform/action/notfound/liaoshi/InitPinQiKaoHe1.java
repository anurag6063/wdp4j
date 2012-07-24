package com.hiromsoft.hiromform.action.notfound.liaoshi;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.proxy.MapProxy;

import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnNotFoundAction;

import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;

public class InitPinQiKaoHe1 extends BaseOnNotFoundAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
		//初始化用户工作量统计情况记录表，添加一条新记录
		HashMap aaa=new HashMap();
		aaa.put("ID", "0");
		aaa.put("jsxm", user.getDisplayName());
		aaa.put("field0", user.getDepartment());
		aaa.put("JSBH", user.getYhbh());
		aaa.put("JSID", user.getID());
		String khbh=(String)request.getSession().getAttribute("__khbh");
		aaa.put("khbh", khbh);
		
		boolean firstDeptAsDefault=true;
		PreparedStatement pst=null;
		try{
			pst=dbsession.connection().prepareStatement("select field5 from designer_userform139 where yhbh=? and khbh=?");
			pst.setString(1, user.getYhbh());
			pst.setString(2, khbh);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				if("是".equals(rst.getString(1))){
					firstDeptAsDefault=false;
				}
			}
			rst.close();
			pst.close();
			pst=null;
		}finally{
			if(pst!=null) try{pst.close();}catch(Exception ex){}
		}
		
		
		MapProxy user1=(MapProxy)dbsession.load("designer_userform21", user.getID());
		aaa.put("field20", user1.get("CJGZSJ")); //考核期限开始
		aaa.put("field21", user1.get("CJGZSJ2"));//考核期限结束
		aaa.put("yqkh", user1.get("yq"));//考核期限结束
		
		if(firstDeptAsDefault){
			aaa.put("field1", user1.get("field0"));   //岗位类别
			aaa.put("field2", user1.get("field1"));   //岗位级别
			
		}else{
			aaa.put("field0", user1.get("field14")); //部门
			aaa.put("field1", user1.get("field18"));   //岗位类别
			aaa.put("field2", user1.get("field19"));   //岗位级别
			
		}
		
		dbsession.saveOrUpdate((String)"designer_userform132",aaa);
		dbsession.flush();
		userDataForm.setValue("__newid", aaa.get("ID"));
	}

}
