package com.hiromsoft.business.system;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.HtmlFormTemplate;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.beforesave.system.ParseHtmlTemplate;
import com.hiromsoft.utils.LobUtils;
import com.hiromsoft.utils.RequestUtil;
import com.hiromsoft.utils.UserViewUtil;

public class RebuildHtmlTemplateExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		try{
			ParseHtmlTemplate parser=new ParseHtmlTemplate();
			UserViewUtil viewUtil=new UserViewUtil();
			for(int num=0;num<rowidx;num++){
				HtmlFormTemplate form=new HtmlFormTemplate();
				String ID=ids[num];
				dbsession.load(form, ID);
				form.setContent(LobUtils.getContent(form.get_content()).toString());
				form=parser.doParse(request, response, dbsession, form);
				viewUtil.saveViewTemplate(dbsession,form);
				form=null;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}finally{}
	}
}
