package com.hiromsoft.business.system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import sql4j.parser.Column;
import sql4j.parser.SQL;

import com.hiromsoft.business.BaseBusinessExecutor;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.utils.RequestUtil;

public class RefreshSQLStatement extends BaseBusinessExecutor {
	
	public void execute(Session dbsession, UserDataForm userDataForm,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
			
//		最多复制100行
		int maxrows=100;
		String[] ids=new String[maxrows];
		int rowidx=RequestUtil.getListSelection(request, ids);
		Connection conn=dbsession.connection();
		for(int num=0;num<rowidx;num++){
			this.buildSQL(ids[num], conn);
		}
	
	}
	/**
	 * 暂时有一个bug，就是SQL中若含有动态变量${name},就出错了
	 * @param id
	 * @param conn
	 * @throws Exception
	 */

	public void buildSQL(String id,Connection conn) throws Exception{
		PreparedStatement pst=null;
		try{
			String tablename="";	
			String viewid="";
			String sql="select tablename,viewid from sys_views where id=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1, id);
			ResultSet rst=pst.executeQuery();
			if(rst.next()){
				tablename=rst.getString(1);
				viewid=rst.getString(2);
			}
			rst.close();
			pst.close();
			pst=null;
			
			if(tablename==null||tablename.length()==0) return;
			
			Vector fields=new Vector();
			sql="select fieldname from sys_views_fields where viewid=? and display='1' order by sn";
			pst=conn.prepareStatement(sql);
			pst.setInt(1, Integer.parseInt(viewid));
			rst=pst.executeQuery();
			while(rst.next()){
				fields.add(rst.getString(1));
			}
			rst.close();
			pst.close();
			pst=null;
			
			
			
			HashMap dicts=new HashMap();		
			sql="select nbzdmc,datadict from sys_forms_fields where sjkbnbmc=? and datadict is not null";
			pst=conn.prepareStatement(sql);
			pst.setString(1, tablename);
			rst=pst.executeQuery();
			while(rst.next()){
				dicts.put(rst.getString(1),rst.getString(2));
			}
			rst.close();
			pst.close();
			pst=null;
			
			String strfields="a.id,";
			String relation="";
			String tables="";
			char bieming='a';
			for(int num=0;num<fields.size();num++){
				String datadict=(String)dicts.get(fields.get(num));
				if(datadict!=null&&datadict.length()!=0){
					String mm[]=datadict.split(";");
					datadict=mm[0];
					if(mm.length>1){
						strfields=strfields+"a."+fields.get(num);
						if(num!=fields.size()-1)
							strfields=strfields+",";
						continue;
					}
					SQL dictsql=new SQL(datadict);
					Vector aa =dictsql.getSelectStatement().getColumns().toVector();
					if(aa.size()<2){
						strfields=strfields+"a."+fields.get(num);
						if(num!=fields.size()-1)
							strfields=strfields+",";
						continue;
					}
					bieming++;	
					String key1=((Column)aa.get(0)).getName();
					String key2=((Column)aa.get(1)).getName();
					relation=relation+"a."+fields.get(num).toString()+"="+bieming+"."+key1+" and ";
					//暂时忽略该SQL中的where
					tables=tables+dictsql.getSelectStatement().getTables().toVector().get(0)+" "+bieming+",";
					
					strfields=strfields+bieming+"."+key2+" as "+fields.get(num);
					
				}else{
					strfields=strfields+"a."+fields.get(num);
				}
				if(num!=fields.size()-1)
					strfields=strfields+",";
			}
			if(relation.length()!=0) relation=relation.substring(0,relation.length()-5);
			if(tables.length()!=0) tables=tables.substring(0,tables.length()-1);
			
			String newsql="select " +strfields+" from "+tablename+" a,"+tables+" where "+relation;
			if(tables.length()==0)
				newsql="";
			
			sql="update sys_views set sql1=? where viewid=?";
			pst=conn.prepareStatement(sql);
			pst.setString(1, newsql);
			pst.setInt(2, Integer.parseInt(viewid));
			pst.execute();
			pst.close();
			pst=null;
			
			
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
				
		}
	}
	
}
