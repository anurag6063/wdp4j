package com.hiromsoft.hiromform.action.aftersave.liaoshi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.hiromsoft.acl.Acl;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;
import com.hiromsoft.hiromview.action.ShowListViewAction;

public class FinishChuShen extends BaseOnSaveAction {
	
	static String a="0";

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		
		String templateId=(String)userDataForm.getValue("__templateId");
		if(templateId==null||templateId.length()==0)
		{
			templateId=((String)userDataForm.getValue("__entityname")).replaceAll("designer_userform", "");
		}
		
		PreparedStatement pst=null;
		try{
			if(!"48".equals(templateId)) return;
			
			String sql="update designer_userform27 set field12 ='5' where field1=${user.yhbh} and field13=${__khbh}";
			pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
			pst.execute();
			pst.close();
			pst=null;
			
			Acl acl=new Acl(dbsession);
			acl.setEveryoneAuthCodeForView("1033",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1034",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1035",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1036",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1037",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1043",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1044",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1045",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1046",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1048",Acl.ALLOW_ALL);
			acl.setEveryoneAuthCodeForView("1049",Acl.ALLOW_ALL);
			
			String jsbh=(String)userDataForm.getValue("JSBH");
			
			acl.setUserAuthCodeForView(jsbh,"1033",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1034",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1035",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1036",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1037",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1043",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1044",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1045",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1046",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1048",Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForView(jsbh,"1049",Acl.ALLOW_READONLY_MINE);
			
			acl.setUserAuthCodeForFormStore(jsbh, "28", Acl.ALLOW_READONLY_MINE);
			acl.setUserAuthCodeForFormStore(jsbh, "49", Acl.ALLOW_READONLY_MINE);
			
						
			acl.saveOrUpdate();
			
			
			String khjg=(String)userDataForm.getValue("field7");
			if("1".equals(khjg)){
				//若是优秀
				int maxcnt=0;
				
				sql="select a.field3 from designer_userform73 a,sys_users b where a.field1=b.field10 and a.field0=${__khbh} and b.id=${user.ID}";
				pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
				ResultSet rst=pst.executeQuery();
				if(rst.next())
					maxcnt=rst.getInt(1);
				rst.close();
				pst.close();
				pst=null;
				
				if(maxcnt==0) throw new Exception("未设置优秀人数或者优秀人数为0");
				
				
				synchronized(a){
					int exist=0;
					dbsession.flush();
					sql="select count(*) from (select a.* from designer_userform48 a,sys_users b  where a.field7='1' and a.jsid=b.id and b.field10=${user.department} and a.khbh=${__khbh})";
					pst=new ShowListViewAction().createStatementAndParameters(request, dbsession.connection(), sql);
					rst=pst.executeQuery();
					if(rst.next())
						exist=rst.getInt(1);
					rst.close();
					pst.close();
					pst=null;
					
					if(exist>maxcnt) throw new Exception("超出最多优秀人数,最多为"+maxcnt+"个，实际为"+exist+"个");
						
				}
			}
			
		}finally{
			try{
				if(pst!=null)
					pst.close();
			}catch(Exception ex){}
		}
	}

}
