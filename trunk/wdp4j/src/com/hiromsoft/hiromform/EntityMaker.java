/*
 * Created on 2005-10-12
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.hiromsoft.hiromform;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EntityMaker {
    private String folder;
    
    
    /**
     * @return Returns the folder.
     */
    public String getFolder() {
        return folder;
    }
    /**
     * @param folder The folder to set.
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void buildEntity(HtmlFormTemplate htmlform) throws  Exception
    {
        buildEntity(htmlform,"uuid");
    }
    public void buildEntity(HtmlFormTemplate htmlform,String generator) throws  Exception
    {
    
        StringBuffer xml=new StringBuffer("<?xml version=\"1.0\" encoding=\"gb2312\"?>\r\n"+
                "<!DOCTYPE hibernate-mapping PUBLIC \r\n"+ 
                "\t\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" \r\n"+
                "\t\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\r\n"+
                "<hibernate-mapping>\r\n"+
                "\t<class entity-name=\""+htmlform.getEntityName()+"\" table=\""+htmlform.getTableName()+"\">\r\n");
        StringBuffer xml2=new StringBuffer("<?xml version=\"1.0\" encoding=\"gb2312\"?>\r\n"+
                "<!DOCTYPE hibernate-mapping PUBLIC \r\n"+ 
                "\t\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" \r\n"+
                "\t\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\r\n"+
                "<hibernate-mapping>\r\n"+
                "\t<class entity-name=\""+htmlform.getEntityName()+"_clob\" table=\""+htmlform.getTableName()+"\">\r\n");
        
        String keyFieldName=htmlform.getKeyFieldName();
        if(keyFieldName==null||keyFieldName.length()==0)
        	keyFieldName="ID";
        
        xml.append("\t\t<id name=\"ID\" column=\""+keyFieldName+"\" type=\"string\" unsaved-value=\"0\">\r\n"+
                "\t\t\t<generator class=\""+generator+"\"/>\r\n"+
                "\t\t</id>\r\n");
        xml2.append("\t\t<id name=\"ID\" column=\""+keyFieldName+"\" type=\"string\" unsaved-value=\"0\">\r\n"+
                "\t\t\t<generator class=\""+generator+"\"/>\r\n"+
                "\t\t</id>\r\n");
        //是否有长字段
        boolean hasclob=false;
        
        if(htmlform.getFields()!=null){
        	Vector tmp=new Vector();
        	tmp.addAll(htmlform.getFields());
        	tmp.addAll(Utils.getSystemFields());
        	
            Iterator it=tmp.iterator();
            while(it.hasNext())
            {
                TemplateField htmltag=(TemplateField)it.next();
                String fieldname=htmltag.getFieldname();
                if(keyFieldName.equalsIgnoreCase(fieldname))
                	continue;
                String propname=htmltag.getPropname();
                if(propname==null||propname.length()==0)
                    propname=fieldname;
                
                String fieldtype=htmltag.getDatatype();
                String datatype="string";
                boolean ignorethis=false;
                /**
                 * 兼容以下划线(_)开头的字段名
                 */
                String attr="";
                if(htmltag.isNoInsert())
                	attr=" insert=\"false\" ";
                if(htmltag.isNoUpdate())
                	attr=attr+" update=\"false\" ";
                
                String tmpfield=fieldname;
                //这样的字段是子表单的字段
                if(Pattern.matches(".+\\_f\\d+\\z", tmpfield)) continue; 
                if(tmpfield.startsWith("_")) tmpfield=tmpfield.substring(1);
                if(fieldtype!=null)
                {
                    if(fieldtype.equals("numeric")){
                        datatype="float";
                    }else if(fieldtype.equals("int")){
                        datatype="long";
                    }else if(fieldtype.equals("date")||fieldtype.equals("datetime")){
                        datatype="timestamp";
                    }else if(fieldtype.equals("clob")){
                    	datatype="clob";
                    	ignorethis=true;
                    	hasclob=true;
   	                	xml2.append("\t\t<property name=\""+propname+"\" column=\""+tmpfield+"\" type=\""+datatype+"\" "+attr+"/>\r\n");
                    	
                    }
                }
                if(!ignorethis){
                	xml.append("\t\t<property name=\""+propname+"\" column=\""+tmpfield+"\" type=\""+datatype+"\" "+attr+"/>\r\n");
                }
                
            }
        }
        xml.append("\t</class>\r\n</hibernate-mapping>");
        xml2.append("\t</class>\r\n</hibernate-mapping>");

        if(folder!=null&&folder.length()!=0){
            File objfolder=new File(folder);
            if(objfolder.exists())
            {
                FileOutputStream fileout=new FileOutputStream(new File(folder+System.getProperty("file.separator")+htmlform.getEntityName()+".hbm.xml"));
                fileout.write(xml.toString().getBytes());
                fileout.close();
                
                if(hasclob){
                	fileout=new FileOutputStream(new File(folder+System.getProperty("file.separator")+htmlform.getEntityName()+"_clob.hbm.xml"));
                    fileout.write(xml2.toString().getBytes());
                    fileout.close();
                }
                
            }
        }
        
               
        return;
    }
}
