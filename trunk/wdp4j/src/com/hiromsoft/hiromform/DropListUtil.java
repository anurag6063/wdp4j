package com.hiromsoft.hiromform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.hiromsoft.hiromview.action.ShowListViewAction;
import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.HtmlUtil;

public class DropListUtil {
	
	public static void GenareteDropList(HttpServletRequest request,JspWriter out,String fieldname,
			String datasource,String fieldname_suffix){
		GenareteDropList(request,out,fieldname,datasource,fieldname_suffix,-1);
	}
	
	
	public static void GenareteDropList(HttpServletRequest request,JspWriter out,String fieldname,
			String datasource,String fieldname_suffix,int defaultIndex){
		PreparedStatement pst=null;
		Connection  conn=null;
		try{
			String[] aa=datasource.split(";");
			String sql=aa[0];
			String data1="";
			String data2="";
			conn=DatabaseUtil.getConn();
			boolean fromview=false;
			if(sql.startsWith("viewid")){
				ShowListViewAction action=new ShowListViewAction();
				HashMap infos=new HashMap();
				sql=action.getViewSQL(infos, sql.replaceAll("viewid:", ""), request);
				fromview=true;
			}	
			
			pst=new ShowListViewAction().createStatementAndParameters(request, conn, sql);
			int startnum=1;
			if(fromview)
				startnum=2;
			ResultSet rst=pst.executeQuery();
			boolean hasRID=false;
			SimpleDateFormat sdf=new SimpleDateFormat();
			sdf.applyPattern("yyyy-MM-dd");
			while(rst.next()){
				String tmp=rst.getString(startnum);
				if(tmp==null||tmp.length()==0) continue;
				data1=data1+"'"+HtmlUtil.JsStringEncode(tmp)+"',";
				String tmp2="";
				int offset=0;
				for(int num=1;num<aa.length;num++){
					if(aa[num].indexOf("__RID")!=-1){
						tmp=rst.getString(1);
						if(tmp==null||tmp.length()==0) tmp="";
						tmp2=tmp2+"'"+HtmlUtil.JsStringEncode(tmp)+"'";
						if(num!=aa.length-1)
							tmp2=tmp2+",";
						offset=-1;
						hasRID=true;
						continue;
					}
					Object obj=rst.getObject(num+startnum+offset);
					if(obj==null)
						tmp="";
					else{
						if(obj instanceof java.util.Date){
							tmp=sdf.format((java.util.Date)obj);
						}else{
							tmp=obj.toString();
						}
					}
					//tmp=rst.getString(num+startnum+offset);
					if(tmp==null||tmp.length()==0) tmp="";
					tmp2=tmp2+"'"+HtmlUtil.JsStringEncode(tmp)+"'";
					if(num!=aa.length-1)
						tmp2=tmp2+",";
					
				}
				data2=data2+"["+tmp2+"],";
			}
			rst.close();
			pst.close();
			pst=null;
			if(data1.length()!=0) data1=data1.substring(0,data1.length()-1);
			if(data2.length()!=0) data2=data2.substring(0,data2.length()-1);
			
			data1="["+data1+"]";
			data2="["+data2+"]";
			String id="dl"+System.currentTimeMillis();
			String script="var "+id+" = new mSift('"+id+"',25);"+id+".Data="+data1+";";
			if(aa.length>1){
				String tmp="";
				for(int num=1;num<aa.length;num++){
					if(aa[num].startsWith("value("))
						tmp=tmp+aa[num].substring(0, aa[num].length()-1)+fieldname_suffix+")";
					else
						tmp=tmp+"value("+aa[num]+fieldname_suffix+")";
					if(num!=aa.length-1)
						tmp=tmp+",";
				}
				script=script+id+".AdditionFields='"+tmp+"';"+id+".AdditionDatas="+data2+";";
			}
			script=script+id+".Create(document.getElementsByName('"+fieldname+"')[0]);";
			if(hasRID)
				script=script+id+".onSetValue=function(obj,name,num){if(name.indexOf(\"__RID\")!=-1){var chk=obj.Target.parentElement.parentElement.childNodes[0].childNodes[0];chk.value=obj.AdditionDatas[obj.ReDataIdx[obj.SelIndex]][num];return false;}return true;}";
			if(defaultIndex!=-1){
				script=script+id+".SetDefaultValues("+defaultIndex+");";
			}
			out.write("<script>");
			out.write(script);
			out.write("</script>");
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(pst!=null){
				try{pst.close();}catch(Exception ex){}
			}
			if(conn!=null){
				try{conn.close();}catch(Exception ex){}
			}
			
		}
	}
}
