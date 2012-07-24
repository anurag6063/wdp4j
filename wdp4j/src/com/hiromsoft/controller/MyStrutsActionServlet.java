package com.hiromsoft.controller;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.SecurityChecker;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyStrutsActionServlet extends ActionServlet {
	private static final long serialVersionUID = 3969695245013259409L;
	//	Initialize global variables
   	private static boolean registerstate=false;
    
    public static void setRegisterState(boolean value){
        registerstate=value;
    }
    public static boolean getRegisterState(){
        return registerstate;
    }
   
    public void init() throws ServletException {

    	boolean ok=false;
        boolean debug=true;
        
        if(debug){
            ok=true;
            MyStrutsActionServlet.setRegisterState(ok);
        }else{
	        Properties prop=new Properties();
	        try{
	            InputStream in=this.getClass().getResourceAsStream("hiromsoft.license");
	            if(in==null) in =Thread.currentThread().getContextClassLoader().getResourceAsStream("hiromsoft.license");          
	            if(in!=null){
		            prop.load(in);
		            String username=prop.getProperty("username");
		            String serialnumber=prop.getProperty("serialnumber");
		            SecurityChecker security=new SecurityChecker();
		            if(security.validateRegister(username,serialnumber)){
		                ok=true;
		                MyStrutsActionServlet.setRegisterState(ok);
		            }
		            //security=null;
		            in.close();
		            in=null;
	            }
	        }catch(Exception e){ 
	            e.printStackTrace();
	        }
	        prop=null;
        }
        if(ok){
	        try{
	        	Configuration cfg=new Configuration();
	        	String sysPath=Global.BASEPATH;
	        	String sep=System.getProperty("file.separator");
	        	String mappingFilesDirPath=sysPath+sep+"WEB-INF"+sep+"mappings";
	        	
	        	if(new File(mappingFilesDirPath+sep+"hiromforms").exists())
	        		cfg.addDirectory(new File(mappingFilesDirPath+sep+"hiromforms"));
	        	cfg.addDirectory(new File(mappingFilesDirPath+sep+"system"));
	        		        		
	            cfg.configure().buildSessionFactory();
	            String sessionFactoryJNDIName=cfg.getProperty(Environment.SESSION_FACTORY_NAME);
	            if(sessionFactoryJNDIName==null||sessionFactoryJNDIName.length()<1)
	            	throw new Exception("在Hibernate.cfg.xml文件中未配置session-factory的name属性");
	            DatabaseUtil.SESSION_FACTORY_JNDI_NAME=sessionFactoryJNDIName;
	            
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	            
	        super.init();
        }
    }
    public void destroy()
    {
        try{
            System.out.println("回收系统资源......");
            InitialContext initCTX = new InitialContext(); 
            SessionFactory sessionFactory=(SessionFactory)initCTX.lookup(DatabaseUtil.SESSION_FACTORY_JNDI_NAME);
            if(sessionFactory!=null)
            {
                sessionFactory.close();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        super.destroy();
    }

}
