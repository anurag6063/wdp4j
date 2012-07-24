package com.hiromsoft.business.system;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.proxy.MapProxy;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.utils.RequestUtil;

public class AsignMenuItemToRoleExecutor extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
//		最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		Vector menuitems=new Vector();
		
		String[] rids=new String[maxrows];
		int rrowidx=RequestUtil.getListSelection(request, rids,"srid");

		PreparedStatement pst=null;
		PreparedStatement pst1=null;
		try{
			pst=dbsession.connection().prepareStatement("select id from sys_menuitems where ordercode=?");
			String entityname="designer_userform121";
			for(int num=0;num<rowidx;num++){
				String ID=ids[num];
				MapProxy proxy=(MapProxy)dbsession.load(entityname, ID);
				String code=(String)proxy.get("ordercode");
				if(!contians(menuitems,ID)){
					menuitems.add(ID);
					if(code.length()<=4) continue;
				}else{
					continue;
				}
				for(int num2=0;num2<(code.length()-4)/2;num2++){
					String tmp=code.substring(0, code.length()-2*(num2+1));
					String PID=getIDbyCode(tmp,pst);
					if(!contians(menuitems,PID)){
						menuitems.add(PID);
						continue;
					}else{
						break;
					}
				}
			}
			pst.close();
			pst=null;
			
			pst=dbsession.connection().prepareStatement("select MIID from sys_role_menuitems where RID=?");
			pst1=dbsession.connection().prepareStatement("insert into sys_role_menuitems(id,MIID,RID) VALUES (?,?,?)");
			boolean executable=false;
			for(int num=0;num<rrowidx;num++){
				String ID=rids[num];
				Vector oldmenuitems=getOldMenuItems(ID,pst);
				for(int num2=0;num2<menuitems.size();num2++){
					String MID=(String)menuitems.get(num2);
					if(contians(oldmenuitems,MID)) continue;
					pst1.setString(1, Utils.GenerateUUID());
					pst1.setString(2, MID);
					pst1.setString(3, ID);
					pst1.addBatch();
					executable=true;
				}
			}	
			pst.close();
			pst=null;
			if(executable)
				pst1.executeBatch();
			pst1.close();
			pst1=null;
			
		}finally{
			if(pst!=null) {try{pst.close();}catch(Exception ex){}}
			if(pst1!=null) {try{pst1.close();}catch(Exception ex){}}
		}
	}
	private Vector getOldMenuItems(String rid,PreparedStatement pst) throws Exception{
		ResultSet rst=null;
		Vector retval=new Vector();
		try{
			pst.setString(1, rid);
			rst=pst.executeQuery();
			while(rst.next()){
				retval.add(rst.getString(1));
			}
			rst.close();
			rst=null;
		}finally{
			if(rst!=null) try{rst.close();}catch(Exception ex){}
		}
		return retval;
	}
	private String getIDbyCode(String code,PreparedStatement pst) throws Exception{
		ResultSet rst=null;
		String retval=null;
		try{
			pst.setString(1, code);
			rst=pst.executeQuery();
			if(rst.next()){
				retval=rst.getString(1);
			}
			rst.close();
			rst=null;
		}finally{
			if(rst!=null) try{rst.close();}catch(Exception ex){}
		}
		return retval;
	}
	private boolean contians(Vector coll,String value){
		if(coll==null) return false;
		if(coll.size()==0) return false;
		boolean retval=false;
		for(int num=0;num<coll.size();num++){
			if(((String)coll.get(num)).equals(value)){
				return true;
			}
		}
		return retval;
	}
}
