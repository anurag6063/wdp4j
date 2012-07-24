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

public class InitJinJiKaoHe extends BaseOnNotFoundAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
		//��ʼ���û�������ͳ�������¼�����һ���¼�¼
		HashMap aaa=new HashMap();
		aaa.put("ID", "0");
		aaa.put("jsxm", user.getDisplayName());
		aaa.put("field0", user.getDepartment());
		aaa.put("JSBH", user.getYhbh());
		aaa.put("JSID", user.getID());
		String khbh=(String)request.getSession().getAttribute("__khbh");
		aaa.put("khbh", khbh);
		
		MapProxy user1=(MapProxy)dbsession.load("designer_userform21", user.getID());
		aaa.put("field16", user1.get("field0"));   //��λ���
		aaa.put("field2", user1.get("field1"));   //��λ����
		aaa.put("field17", user1.get("field1"));   //�������λ����
		//aaa.put("field13", user1.get("field1"));   //��׼��λ����
		
		dbsession.saveOrUpdate((String)"designer_userform162",aaa);
		dbsession.flush();
		userDataForm.setValue("__newid", aaa.get("ID"));
	}

}
