package com.hiromsoft.utils;

import javax.naming.*;
import javax.sql.DataSource;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.hibernate.*;
import org.hibernate.util.XMLHelper;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DatabaseUtil {

	public static String SESSION_FACTORY_JNDI_NAME;
	public static String sessionFactoryLock="wait";
	public static String dataSourceLock="wait";
	public static SessionFactory globalSessFactory=null;
	public static DataSource globalDataSource=null; 
	
	
	public  static synchronized Connection  getConn()throws Exception{
		if(globalDataSource!=null)
			return globalDataSource.getConnection();
		else{
			return findConn();
		}
	}
	public static Connection findConn()throws Exception{
		Connection cn=null;
		try{
    		InitialContext rootctx=new InitialContext();
    		javax.sql.DataSource ds=(javax.sql.DataSource)rootctx.lookup("java:comp/env/jdbc/hiromsoft");
    		globalDataSource=ds;
    		cn=ds.getConnection();
    	}catch(Exception e)
		{
    		e.printStackTrace();
    		throw new Exception(e);
    	}
		return cn;
	}
		
	public static synchronized Connection getConn(String name) throws Exception
	{
		Connection cn=null;
		try{
    		InitialContext rootctx=new InitialContext();
    		javax.sql.DataSource ds=(javax.sql.DataSource)rootctx.lookup("java:comp/env/"+name);
    		cn=ds.getConnection();
    	}catch(Exception e)
		{
    		e.printStackTrace();
    		throw new Exception(e);
		}
    	return cn;
	}
	public static synchronized Session getHibernateSession() throws Exception
    {
		if(globalSessFactory!=null)
			return globalSessFactory.openSession();
		else
			return findHibernateSession();
    }
    public static synchronized Session findHibernateSession() throws Exception
    {
        org.hibernate.Session session=null;
        try{
            InitialContext ctx = new InitialContext();
            SessionFactory sessionFactory=(SessionFactory)ctx.lookup(SESSION_FACTORY_JNDI_NAME);
            if(sessionFactory!=null)
            {
            	globalSessFactory=sessionFactory;
            	session=sessionFactory.openSession();
            }else
                throw new Exception("can't get the JNDI bind");
        }catch(Exception e)
        {
            e.printStackTrace();
            throw new Exception(e);
        }
        return session;
    }
    
    public Map getHibernateMappingInfo(String  entityFileName) throws Exception{
        Map retval=new HashMap();
        
        File file=new File(entityFileName);
        if(!file.exists())
            return retval;
        List errors = new ArrayList();
        XMLHelper xmlHelper = new XMLHelper();
        EntityResolver entityResolver = XMLHelper.DEFAULT_DTD_RESOLVER;
        
        FileInputStream xmlInputStream=new FileInputStream(file);
		org.dom4j.Document doc = xmlHelper.createSAXReader( "XML InputStream", errors, entityResolver ).read( new InputSource( xmlInputStream ) );
        Element cls=(Element)doc.getRootElement().elements().get(0);
		Iterator it= cls.elementIterator();
		while(it.hasNext()){
		    Element prop=(Element)it.next();
		    if(prop.getName().equals("property")){
		        String propname=prop.attributeValue("property");
		        String column=prop.attributeValue("column");
		        if(propname!=null&&column!=null){
		            retval.put(propname,column);
		        }
		    }
		}
		doc=null;
		xmlInputStream.close();
        xmlHelper=null;
        entityResolver=null;
        return retval;
    }
    
    public String getFieldName(Map meta,String propname){
        String retval="";
        retval=(String)meta.get(propname);
        if(retval==null){
            int cnt=0;
            while(true){
                if(!meta.containsValue("field"+cnt)){
                    meta.put(propname,"field"+cnt);
                    retval="field"+cnt;
                    break;
                }
                cnt++;
            }
        }
        return retval;
    }
    
    
}