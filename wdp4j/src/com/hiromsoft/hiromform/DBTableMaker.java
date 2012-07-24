package com.hiromsoft.hiromform;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hiromsoft.utils.Global;

public class DBTableMaker {
	private static Log log = LogFactory.getLog(DBTableMaker.class);
	private Connection conn=null;
	
	//public static Log loger=LogFactory
	
	
    public void buildTable(HtmlFormTemplate htmlform) throws Exception {
        buildTable(htmlform,32);
    }
    public void buildTable(HtmlFormTemplate htmlform,int idlength) throws Exception {
        
        Vector newfields=new Vector();
        Vector oldfields=new Vector();
        Vector sysFieldNames=Utils.getSystemFieldNames();
        
        StringBuffer SQL = new StringBuffer("create table "
                + htmlform.getTableName());
        String keyFieldName=htmlform.getKeyFieldName();
        if(keyFieldName==null||keyFieldName.length()==0)
        	keyFieldName="id";

        SQL.append(" ("+keyFieldName+" "+getTypeName("1")+"("+idlength+"),");
        newfields.add(keyFieldName+":1");

        if (htmlform.getFields() != null) {
            Iterator it = htmlform.getFields().iterator();
            while (it.hasNext()) {
                TemplateField htmltag = (TemplateField) it.next();
                String fieldname = htmltag.getFieldname();
                if(keyFieldName.equalsIgnoreCase(fieldname))
                	continue;
                if(Pattern.matches(".+\\_f\\d+\\z", fieldname)) continue; 
                String fieldtype = htmltag.getDatatype();
                String datatype = getTypeName("1");
                if(Global.DBTYPE.equals("oracle"))
                    datatype=datatype+"(255 char)";
                else
                    datatype=datatype+"(255)";
                
                String typecode="1";
                if (fieldtype != null) {
                    if (fieldtype.equals("numeric")){
                        typecode="2";
                        datatype = getTypeName(typecode)+"(18,5)";
                    }else if (fieldtype.equals("date")||fieldtype.equals("datetime")){
                        typecode="3";
                        datatype = getTypeName(typecode);
                    }else if (fieldtype.equals("clob")){
                    	typecode="4";
                        datatype = getTypeName(typecode);
                    }else if (fieldtype.equals("int")){
                    	typecode="5";
                        datatype = getTypeName(typecode)+"(18,0)";
                    }
                }
                if(sysFieldNames!=null&&!sysFieldNames.contains(fieldname.toUpperCase())){
                	
                	if(fieldname.startsWith("_")){
                		newfields.add(fieldname.substring(1).toLowerCase()+":"+typecode);
                		SQL.append(fieldname.substring(1));
                	}else{
                		SQL.append(fieldname);
                		newfields.add(fieldname.toLowerCase()+":"+typecode);
                	}
                	SQL.append(" ");
                	SQL.append(datatype);
                	SQL.append(",");
                }
            }
        }
        if(sysFieldNames!=null){
        	Vector sysfields=Utils.getSystemFields();
        	for(int num=0;num<sysfields.size();num++){
        		TemplateField field=(TemplateField)sysfields.get(num);
        		if("char".equals(field.getDatatype())){
        			SQL.append(field.getFieldname()+" "+getTypeName("1")+"(255),");
        			newfields.add(field.getFieldname().toLowerCase()+":"+"1");
        		}else if("date".equals(field.getDatatype())||"datetime".equals(field.getDatatype())){
        			SQL.append(field.getFieldname()+" "+getTypeName("3")+",");
        			newfields.add(field.getFieldname().toLowerCase()+":"+"3");
        		}else if("numeric".equals(field.getDatatype())){
        			SQL.append(field.getFieldname()+" "+getTypeName("2")+"(18,2),");
        			newfields.add(field.getFieldname().toLowerCase()+":"+"2");
        		}else if("int".equals(field.getDatatype())){
        			SQL.append(field.getFieldname()+" "+getTypeName("5")+"(18,0),");
        			newfields.add(field.getFieldname().toLowerCase()+":"+"5");
        		}
        	}
        }
        /*
        SQL.append("EXT_ZIHAO "+getTypeName("1")+"(10),EXT_NIANFEN "+getTypeName("1")+"(4),EXT_BIANHAO "+getTypeName("1")+"(30),"+
        		"EXT_CERTNO "+getTypeName("1")+"(45),EXT_OLDCERTNO "+getTypeName("1")+"(45),EXT_PRINTDATE "+getTypeName("3")+","+
                "EXT_DATEFROM "+getTypeName("3")+",EXT_DATETO "+getTypeName("3")+",EXT_ISVALID "+getTypeName("1")+"(1) DEFAULT 'N',"+
                "EXT_DEPTNAME "+getTypeName("1")+"(100),EXT_XMBH "+getTypeName("1")+"(30),EXT_PROJID "+getTypeName("1")+"(45),"+
                "EXT_DELETED "+getTypeName("1")+"(1) default 'N',EXT_DELETEDDATE "+getTypeName("3")+","+
                "EXT_CHUFAZHENGFZR "+getTypeName("1")+"(40),EXT_CHUFAZHENGRIQI "+getTypeName("3")+",");
        */
        SQL.deleteCharAt(SQL.length() - 1);
        SQL.append(")");
        if(Global.DBTYPE.equals("mysql"))
            SQL.append(" character set gbk collate gbk_chinese_ci");

        Connection cn = null;

        try {
            cn =this.conn;
            Statement stmt = cn.createStatement();
            try {
                stmt.execute(SQL.toString());
                stmt.close();
                stmt=null;
            } catch (Exception ex) {
            	if(log.isDebugEnabled())
            		log.debug(ex);
            	stmt.close();
            	stmt=null;
            	//获得原来表的结构
            	if(log.isInfoEnabled())
            		log.info("Alter table" + htmlform.getTableName());
                String sql="select  * from "+htmlform.getTableName()+" where id ='000'";
                Statement st1=cn.createStatement();
                ResultSet rst=st1.executeQuery(sql);
                ResultSetMetaData meta=rst.getMetaData();
                int cnt=meta.getColumnCount();
                for(int num=1;num<=cnt;num++)
                {
                    String tmp1=meta.getColumnName(num).toLowerCase();
                    if(!tmp1.startsWith("ext_")){
                    	String tmp2="";
                    	switch(meta.getColumnType(num))
                        {
                        	case Types.BIGINT:
                        	case Types.INTEGER:
                        	case Types.SMALLINT:
                        	case Types.TINYINT:
                        	{
                        	    tmp2="5";
                        	    break;
                        	}	
                        	case Types.DECIMAL:
                        	case Types.DOUBLE:
                        	case Types.FLOAT:
                        	case Types.NUMERIC:
                        	case Types.REAL:
                        	{
                        		tmp2="2";
                        		if(meta.getScale(num)==0)
                        			tmp2="5";
                        	    break;
                        	}
                        	case Types.DATE:
                        	case Types.TIME:
                        	case Types.TIMESTAMP:
                        	{
                        	    tmp2="3";
                        	    break;
                        	}
                        	case Types.CHAR:
                        	case Types.VARCHAR:
                        	{
                        	    tmp2="1";
                        	    break;
                        	}case Types.CLOB:
                        	{
                        		tmp2="4";
                        	    break;
                        	}
                        		
                        }
                        if(tmp2.length()!=0)
                        {
                            oldfields.add(tmp1+":"+tmp2);
                        }
                    }                    
                }
                rst.close();
                st1.close();
                //
                AlterTable(oldfields,newfields,cn,htmlform);
            }
            
            //建立sequence
            
            Iterator it = htmlform.getFields().iterator();
            try{
	            stmt = cn.createStatement();
	            while (it.hasNext()) {
	            	TemplateField field = (TemplateField) it.next();
	            	if(field.isAutoIncrease()){
	            		try{
	            			stmt.execute("CREATE SEQUENCE seq_f"+htmlform.getFormId()+"_"+field.getFieldname()+" START WITH 1 MINVALUE 1 NOCYCLE NOCACHE NOORDER");
	            		}catch(Exception ex){
	            			if(log.isDebugEnabled())
	            				log.debug(ex);
	            		}
	            	}
	            }
            }catch(Exception ex){
            }finally{
            	try{if(stmt!=null)stmt.close();}catch(Exception ex){}
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
        }

        return;
    }
    
    void AlterTable(Vector oldfields,Vector newfields,Connection cn,HtmlFormTemplate htmlform) throws Exception
    {
        String sqladd="";
        String sqldelete="";
        String sqldrop="";
        Vector modify=new Vector();
        
        Iterator it=newfields.iterator();
        Iterator currit=null;
        String delv=" ";
        if(Global.DBTYPE.equals("mysql"))
            delv=",";
        while(it.hasNext())
        {
            String tmp=(String)it.next();
            String aa[]=tmp.split(":");
            switch(isNewField(oldfields,tmp,currit))
            {
            	case 0:
            	{
            	    sqladd=sqladd+"add "+buildFieldSQL(tmp)+delv;
            	    break;
            	}
            	case 1:
            	{
            	    //sqladd=sqladd+"add "+buildFieldSQL(tmp)+delv;
            	    sqldelete=sqldelete +aa[0]+"=null,";
            	    modify.add(tmp);
            	    //sqldrop=sqldrop+aa[0]+",";
            	    
            	    break;
            	}
            	case 2:
            	{
            	    //currit.remove();
            	    break;
            	}
            }
        }
        
        /*
         * 2011/11/16 暂时先去掉删除数据库字段的功能，避免数据删除掉，多余的字段需要自己手动处理
        Iterator it1=oldfields.iterator();
        while(it1.hasNext())
        {
            String tmp1=(String)it1.next();
            String tmp2[]=tmp1.split(":");
            if(tmp2[0].toLowerCase().startsWith("field")){
            	sqldelete=sqldelete +tmp2[0]+"=null,";
            	sqldrop=sqldrop+tmp2[0]+",";
            }
        }*/
        Statement st1=null;
        try{
	        st1=cn.createStatement();
	        //将要修改类型的列数据清空
	        if(sqldelete.length()!=0){
	            sqldelete="update "+htmlform.getTableName() +" set "+sqldelete.substring(0,sqldelete.length()-1);
	            st1.execute(sqldelete);
	        }
	        //修改数据类型
	        if(modify.size()!=0){
	        	for(int num=0;num<modify.size();num++){
	        		String tmp=(String)modify.get(num);
	        		String sss="alter table "+htmlform.getTableName()+" MODIFY("+buildFieldSQL(tmp)+")";
	        		st1.execute(sss);
	        	}
	        }
	        
	        if(sqldrop.length()!=0){
	            
	            String sqldrop1="alter table "+htmlform.getTableName();
	            String[] flds=sqldrop.substring(0,sqldrop.length()-1).split(",");
	            for(int fldcnt=0;fldcnt<flds.length;fldcnt++){
	                String sqldrop2=sqldrop1+" drop column "+flds[fldcnt];
	                //if(fldcnt!=flds.length-1)
	                //    sqldrop1=sqldrop1+",";
	                st1.execute(sqldrop2);
	            }
	            
	            //st1.execute(sqldrop1);
	        }
	        
	        if(sqladd.length()!=0){
	            sqladd="alter table "+htmlform.getTableName()+" "+sqladd.substring(0,sqladd.length()-1);
	            st1.execute(sqladd);
	        }
	        
        }catch(Exception e)
        {
            throw e;
        }finally{
        	try{
        		st1.close();
        	}catch(Exception ex){}
        }
        
        return;
    }
    String buildFieldSQL(String field)
    {
        String retval="";
        String aa[]=field.split(":");
        String tmp=getTypeName("1")+"(200)";
        if(aa[1].equals("2"))
            tmp=getTypeName("2")+"(18,5)";
        else if(aa[1].equals("3"))
            tmp=getTypeName("3");
        else if(aa[1].equals("4"))
            tmp=getTypeName("4");
        else if(aa[1].equals("5"))
            tmp=getTypeName("5")+"(18,0)";
        
        if(aa[0].startsWith("_"))
        	retval=aa[0].substring(1)+" "+tmp;
        else	
        	retval=aa[0]+" "+tmp;
        return retval;
    }
    int isNewField(Vector oldfields,String newfield,Iterator currit)
    {
        //0  new field
        //1  edit field
        //2  no change
        int retval=0;
        String bb[]=newfield.split(":");
        Iterator it=oldfields.iterator();
        while(it.hasNext())
        {
            String tmp=(String)it.next();
            tmp=tmp.toLowerCase();
            if(tmp.equals(newfield.toLowerCase()))
            {
                retval=2;
                it.remove();
                currit=it;
                break;
            }else
            {
                String aa[] =tmp.split(":");
                if(aa[0].toLowerCase().equals(bb[0].toLowerCase()))
                {
                    retval=1;
                    currit=it;
                    break;
                }    
            }
                
        }
        return retval;
    }
    
    private String getTypeName(String type){
        String retval=null;
        if(Global.DBTYPE.equals("oracle")){
            retval="varchar2";
            if(type!=null){
                if(type.equals("2")||type.equals("N"))
                    retval="number";
                else if(type.equals("3")||type.equals("D"))
                    retval="date";
                else if(type.equals("4"))
                    retval="clob";
                else if(type.equals("5")||type.equals("I"))
                    retval="number";
            }
        }else if(Global.DBTYPE.equals("mysql")){
            retval="varchar";
            if(type!=null){
                if(type.equals("5")||type.equals("2")||type.equals("N"))
                    retval="numeric";
                else if(type.equals("3")||type.equals("D"))
                    retval="datetime";
                else if(type.equals("4"))
                    retval="mediumtext";
                
            }   
        }else{
            retval="varchar";
            if(type!=null){
                if(type.equals("5")||type.equals("2")||type.equals("N"))
                    retval="numeric";
                else if(type.equals("3")||type.equals("D"))
                    retval="datetime";
                else if(type.equals("4"))
                    retval="text";
            } 
        }
        return retval;
    }
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
    
}
