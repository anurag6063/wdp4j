package com.hiromsoft.hiromform.action.notfound.liaoshi;


import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.business.system.ArchiveTheUserExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnNotFoundAction;

import com.hiromsoft.utils.Global;
import com.hiromsoft.web.modal.User;

public class InitShangBaoExecutor extends BaseOnNotFoundAction{

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		User user=(User)request.getSession().getAttribute(Global.HIROM_USER);
		//初始化用户上报情况记录表，添加一条新记录
		//首先需要将用户的当前信息归档，以便于查询历史数据，还原历史真像
		ArchiveTheUserExecutor archiver=new ArchiveTheUserExecutor();
		archiver.doArchiveUser(dbsession, request, user.getID());
		
		HashMap aaa=new HashMap();
		aaa.put("ID", "0");
		aaa.put("field0", user.getDisplayName());
		aaa.put("field1", user.getYhbh());
		aaa.put("field2", user.getID());
		aaa.put("field12", "1");
		aaa.put("field13", request.getSession().getAttribute("__khbh"));
		dbsession.saveOrUpdate((String)"designer_userform27",aaa);
		dbsession.flush();
		userDataForm.setValue("__newid", aaa.get("ID"));
		
	}

}
