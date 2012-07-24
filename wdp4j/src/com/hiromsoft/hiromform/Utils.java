
package com.hiromsoft.hiromform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;



import org.hibernate.Hibernate;
import org.hibernate.Session;

import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDHexGenerator;

import com.hiromsoft.hiromview.action.ShowListViewAction;
import com.hiromsoft.utils.DatabaseUtil;


public class Utils {
    
	private static Vector sysfields=null;
	private static Vector sysfieldnames=null;
	
	public static int getSequenceValue(String seqname) throws Exception{
		int retval=-1;
		Connection cn=null;
        try{
        	cn=DatabaseUtil.getConn();
        	retval=getSequenceValue(cn,seqname);
        }finally{
        	if(cn!=null) try{cn.close();}catch(Exception ex){}
        }
        return retval;
    }
	
	public static int getSequenceValue(Session dbsession,String seqname) throws Exception{
    	return getSequenceValue(dbsession.connection(),seqname);
    }
	
	public static int getSequenceValue(Connection conn,String seqname) throws Exception{
    	int retval=0;
    	PreparedStatement pst=null;
    	try{
    		pst=conn.prepareStatement("select "+seqname+".nextval from dual");
    		ResultSet rst=pst.executeQuery();
    		if(rst.next())
    			retval=rst.getInt(1);
    		rst.close();
    		pst.close();
    		pst=null;
    	}finally{
    		if(pst!=null) try{pst.close();}catch(Exception ex){}
    	}
    	return retval;
    }
    
	public static StringBuffer escapeMySQLVars(String sql){
		StringBuffer replacedSQL=new StringBuffer();
		Pattern pttn=Pattern.compile("\\$\\{[^\\s&&[^\\$\\{\\}]]+\\}");
        Matcher matcher=pttn.matcher(sql);
        String varName="";
        while(matcher.find()){
            varName=matcher.group();
            matcher.appendReplacement(replacedSQL,"'\\#100"+varName.substring(1).replaceAll("'", "&#93;")+"'");
        }
        matcher.appendTail(replacedSQL);
        
        return replacedSQL;
	}
	public static StringBuffer unescapeMySQLVars(String sql){
		StringBuffer replacedSQL=new StringBuffer();
		Pattern pttn=Pattern.compile("\\'\\#100\\{[^\\s&&[^\\$\\{\\}]]+\\}\\'");
        Matcher matcher=pttn.matcher(sql);
        String varName="";
        while(matcher.find()){
            varName=matcher.group();
            matcher.appendReplacement(replacedSQL,"\\$"+varName.substring(5,varName.length()-1).replaceAll("&#93;", "'"));
        }
        matcher.appendTail(replacedSQL);
        return replacedSQL;
	}
	
    public String getDateText(Date dt) {
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyPattern("yyyy-MM-dd");
        return formatter.format(dt);
    }
    public static Vector getSystemFields(){
    	if(sysfields==null){
    		sysfields=new Vector();
    		if(sysfieldnames==null)
    			sysfieldnames=new Vector();
    		TemplateField field=new TemplateField();
    		field.setDatatype("char");
    		field.setFieldname("sys_creator");
    		field.setTitle("创建人");
    		field.setPropname("sys_creator");
    		sysfields.add(field);
    		sysfieldnames.add(field.getFieldname().toUpperCase());
    		
    		field=new TemplateField();
    		field.setDatatype("char");
    		field.setFieldname("sys_creator_groups");
    		field.setPropname("sys_creator_groups");
    		field.setTitle("创建工作组");
    		sysfields.add(field);
    		sysfieldnames.add(field.getFieldname().toUpperCase());
    		
    		field=new TemplateField();
    		field.setDatatype("date");
    		field.setFieldname("sys_createddate");
    		field.setPropname("sys_createddate");
    		field.setTitle("创建时间");
    		sysfields.add(field);
    		sysfieldnames.add(field.getFieldname().toUpperCase());
    		
    		field=new TemplateField();
    		field.setDatatype("char");
    		field.setFieldname("sys_lastmodifier");
    		field.setPropname("sys_lastmodifier");
    		field.setTitle("最后修改人");
    		sysfields.add(field);
    		sysfieldnames.add(field.getFieldname().toUpperCase());
    		
    		field=new TemplateField();
    		field.setDatatype("datetime");
    		field.setFieldname("sys_lastmodifieddate");
    		field.setPropname("sys_lastmodifieddate");
    		field.setTitle("最后修改时间");
    		sysfields.add(field);
    		sysfieldnames.add(field.getFieldname().toUpperCase());
    		
    		field=new TemplateField();
    		field.setDatatype("char");
    		field.setFieldname("sys_lockforever");
    		field.setPropname("sys_lockforever");
    		field.setTitle("永远锁");
    		sysfields.add(field);
    		sysfieldnames.add(field.getFieldname().toUpperCase());
    		
    	}
    	return sysfields;
    }
    public static Vector getSystemFieldNames(){
    	if(sysfieldnames==null)
    		getSystemFields();
    	return sysfieldnames;
    }
    public static String GenerateUUID()
	{
		String retval="";
		Properties props = new Properties();
		//props.setProperty("separator", "/");
		IdentifierGenerator gen = new UUIDHexGenerator();
		( (Configurable) gen ).configure(Hibernate.STRING, props, null);
		retval = (String) gen.generate(null, null);
		return retval;
	}
		
	
	
	public void InitOptions2(String sql,String name,String nameLabel,Object scope,String[] args) throws Exception
	{
		this.InitOptions(sql, name, nameLabel, scope, args, 0,1);
	}
	public void InitOptions3(String sql,String name,String nameLabel,Object scope,String[] args) throws Exception
	{
		this.InitOptions(sql, name, nameLabel, scope, args, 1,1);
	}
	public void InitOptions1(String sql,String name,String nameLabel,Object scope,String[] args) throws Exception
	{
		this.InitOptions(sql, name, nameLabel, scope, args, 1,0);
	}
	public void InitOptions(String sql,String name,String nameLabel,Object scope,String[] args) throws Exception
	{
		this.InitOptions(sql, name, nameLabel, scope, args, 0,0);
	}
	public void InitOptions(String sql,String name,String nameLabel,Object scope,String[] args,int type,int psttype) throws Exception
	{
		Connection cn=null;
        PreparedStatement pst=null;
        try{
        	if(name==null||name.length()==0||nameLabel==null||nameLabel.length()==0||scope==null)
        		throw new Exception("InitOptions调用失败，参数不全");
        	cn=DatabaseUtil.getConn();
        	if(psttype==0){
        		pst=cn.prepareStatement(sql);
	        	if(args!=null){
		        	for(int num=0;num<args.length;num++){
		        		pst.setString(num+1,args[num]);
		        	}
	        	}
        	}else if(psttype==1){
        		pst=new ShowListViewAction().createStatementAndParameters((HttpServletRequest)scope, cn, sql);
        	}
        	ResultSet rst=pst.executeQuery();
        	Vector names=new Vector();
        	Vector nameLabels=new Vector();
        	int startlen=-1;
        	while(rst.next())
        	{
        		String name1=rst.getString(1);
        		if(rst.wasNull()) name1="";
        		String name2=rst.getString(2);
        		if(rst.wasNull()) name2="";
        		names.add(name1);
        		if(type==1){
        			if(rst.getMetaData().getColumnCount()>2)
        				name1=rst.getString(3);
            		if(startlen==-1){
            			startlen=name1.length();
            		}
        			int cnt=(name1.length()-startlen)/2;
        			for(int num=0;num<cnt;num++){
        				name2="—"+name2; //　
        				if(num==cnt-1)
        					name2="|"+name2;
        			}
        		}
        		nameLabels.add(name2);
        	}
        	rst.close();
        	if(scope instanceof HttpServletRequest)
    		{
    			HttpServletRequest request=(HttpServletRequest)scope;
    			request.setAttribute(name,names);
    			request.setAttribute(nameLabel,nameLabels);
    		}else if(scope instanceof HttpSession)
    		{
    			HttpSession session=(HttpSession)scope;
    			session.setAttribute(name,names);
    			session.setAttribute(nameLabel,nameLabels);
    		}else{
    			throw new Exception("InitOptions调用失败，scope参数错误");
    		}
        }catch(Exception ex)
		{
        	ex.printStackTrace();
        	throw new Exception(ex);
        }
        finally{
        	if(pst!=null)
        	{
        		try{
        			pst.close();
        		}catch(Exception e){}
        	}
        	if(cn!=null)
        	{
        		try{
        			cn.close();
        		}catch(Exception e){}
        	}
        }
	}
	public String getOptionValue(String sql,String[] args) throws Exception
	{
		Connection cn=null;
        PreparedStatement pst=null;
        String retval=null;
        try{
        	
        	cn=DatabaseUtil.getConn();
        	pst=cn.prepareStatement(sql);
        	if(args!=null){
	        	for(int num=0;num<args.length;num++){
	        		pst.setString(num+1,args[num]);
	        	}
        	}
        	ResultSet rst=pst.executeQuery();
        	if(rst.next())
        	{
        		retval=rst.getString(1);
        		if(rst.wasNull()) retval="";
        	}
        	rst.close();
        }catch(Exception ex)
		{
        	ex.printStackTrace();
        	throw new Exception(ex);
        }
        finally{
        	if(pst!=null)
        	{
        		try{
        			pst.close();
        		}catch(Exception e){}
        	}
        	if(cn!=null)
        	{
        		try{
        			cn.close();
        		}catch(Exception e){}
        	}
        }
        return retval;
	}
	 public  static String deleteKuoHao(String val){
	    	String retval=val;
	    	if(retval==null||retval.length()==0) return retval;
	    	int idx=retval.lastIndexOf("(");
	    	if(idx==-1){
	    		idx=retval.lastIndexOf("（");
	    		if(idx!=-1){
	    			retval=retval.substring(0, idx);
	    		}
	    	}else{
	    		retval=retval.substring(0, idx);
	    	}
	    	return retval;
	 }
	 public static Map getViewContext(UserDataForm userDataForm){
		 Map retval=null;
		 String tmp=userDataForm.getStringValue("__viewcontext");
		 if(tmp!=null&&tmp.length()!=0){
			 String[] oo=tmp.split(";");
			 for(int num=0;num<oo.length;num++){
				 String pp=oo[num];
				 if(pp==null||pp.length()==0) continue;
				 String qq[]=pp.split(":");
				 if(qq.length<2) continue;
				 if(retval==null) retval=new HashMap();
				 retval.put(qq[0],qq[1]);
			 }
		 }
		 return retval;
	 }
	 public static void main(String[] args){
		 String aaa="啊啊啊阿萨啊（ajsja）";
		 System.out.println(deleteKuoHao(aaa));
		 
	 }
		 
		
		

}