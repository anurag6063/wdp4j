package com.hiromsoft.hiromform.action.beforesave.system;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.hiromsoft.hiromform.DBTableMaker;
import com.hiromsoft.hiromform.EntityMaker;
import com.hiromsoft.hiromform.TemplateParser;
import com.hiromsoft.hiromform.HtmlFormTemplate;
import com.hiromsoft.hiromform.TemplatePageMaker;
import com.hiromsoft.hiromform.TemplateField;
import com.hiromsoft.hiromform.UserDataForm;
import com.hiromsoft.hiromform.Utils;
import com.hiromsoft.hiromform.action.BaseOnSaveAction;
import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.RequestUtil;
import com.hiromsoft.utils.UserViewUtil;

public class ParseHtmlTemplate extends BaseOnSaveAction {

	public void execute(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception {
		// TODO Auto-generated method stub
		SetFormId sfi=new SetFormId();
		sfi.execute(request, response, dbsession, userDataForm);
		HtmlFormTemplate form=this.doParse(request, response, dbsession, userDataForm);
		UserViewUtil viewUtil=new UserViewUtil();
		viewUtil.saveViewTemplate(dbsession,form);
	}
	public HtmlFormTemplate doParse(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			HtmlFormTemplate htmlform) throws Exception{
		
		new TemplateParser().parse(htmlform,getField(dbsession.connection(),htmlform.getTableName()));
        
        TemplatePageMaker jspbuilder=new TemplatePageMaker();
		String jsppath=Global.BASEPATH+RequestUtil.getRealPath("/jsp/hiromform/_genareted");
		jspbuilder.setConn(dbsession.connection());
		jspbuilder.setFolder(jsppath);
		jspbuilder.setRequest(request);
		jspbuilder.buildJsp(htmlform);
		
		deleteChildFields(htmlform);
//		create Hibernate entity configuration 
		EntityMaker entityBuilder=new EntityMaker();
		String entitypath=Global.BASEPATH+RequestUtil.getRealPath("WEB-INF/mappings/hiromforms");
		entityBuilder.setFolder(entitypath);
		entityBuilder.buildEntity(htmlform);
		
		//create the table for the form
		DBTableMaker tablebuilder=new DBTableMaker();
		tablebuilder.setConn(dbsession.connection());
		tablebuilder.buildTable(htmlform);
		
		registerForms(Global.BASEPATH+RequestUtil.getRealPath("WEB-INF/mappings/hiromforms/"),htmlform.getEntityName());
		saveFields(htmlform,dbsession.connection());

		return htmlform;
	}
	private void deleteChildFields(HtmlFormTemplate htmlform) throws Exception{
		if(htmlform.getFields()==null) return;
		for(int num=htmlform.getFields().size()-1;num>=0;num--){
			TemplateField field=(TemplateField)htmlform.getFields().get(num);
  		    if(!Pattern.matches(".+\\_f\\d+\\z", field.getFieldname())) continue; 
			htmlform.getFields().remove(num);
		}
	}
	public HtmlFormTemplate doParse(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm) throws Exception{
		HtmlFormTemplate htmlform=new HtmlFormTemplate();
		htmlform.setContent(userDataForm.getStringValue("content"));
        htmlform.setEntityName(userDataForm.getStringValue("entityname"));
        htmlform.setName(userDataForm.getStringValue("name"));
        htmlform.setTableName(userDataForm.getStringValue("tablename"));
        htmlform.setVersion("1.0");
        htmlform.setFormId(userDataForm.getIntValue("fid"));
        htmlform.setID(userDataForm.getStringValue("ID"));
        this.doParse(request, response, dbsession, htmlform);
		userDataForm.setValue("content", htmlform.getContent());
		return htmlform;
	}
	private void saveFields(HtmlFormTemplate htmlform,Connection cn) throws Exception{
//		/将表单的字段信息加入到sys_forms_fields表中
		Iterator it=htmlform.getFields().iterator();
		PreparedStatement pst=null;
		try{
			pst=cn.prepareStatement("delete from sys_forms_fields where sjkbnbmc=? and fid=?");
			pst.setString(1,htmlform.getTableName());
			pst.setInt(2, htmlform.getFormId());
			pst.execute();
			pst.close();
			
			pst=cn.prepareStatement("insert into sys_forms_fields(sjkbnbmc,wbzdmc,nbzdmc,zdlx,datadict,fid) values (?,?,?,?,?,?)");
			while(it.hasNext()){
			    TemplateField htmltag = (TemplateField) it.next();
	            String fieldname = htmltag.getFieldname();
	            String fieldtype = htmltag.getDatatype();
	            String datatype = "字符型";
	            if (fieldtype != null) {
	                if (fieldtype.equals("numeric")){
	                    datatype = "数值型";
	                }else if (fieldtype.equals("date")){
	                    datatype = "日期型";
	                }
	            }
	            String displayName=Utils.deleteKuoHao(htmltag.getTitle());
	            if(displayName==null||displayName.length()==0)
	                displayName=fieldname;
	            pst.setString(1,htmlform.getTableName());
	            pst.setString(2,displayName);
	            pst.setString(3,fieldname);
	            pst.setString(4,datatype);
	            pst.setString(5,htmltag.getDatadict());
	            pst.setInt(6, htmlform.getFormId());
	            pst.execute();
	                
			}
			pst.close();
			pst=null;
		}finally{
			if(pst!=null) try{pst.close();}catch(Exception ex){}
		}
	}
	private void registerForms(String folder,String entityName) throws Exception{
		InitialContext ctx = new InitialContext();
        SessionFactory sessionFactory = (SessionFactory) ctx
                .lookup(DatabaseUtil.SESSION_FACTORY_JNDI_NAME);
        if (sessionFactory != null) {
            Configuration config = new Configuration();
            String mappingfile = folder+ entityName + ".hbm.xml";
            FileInputStream mappingfilein=new FileInputStream(new File(mappingfile));
            config.addInputStream(mappingfilein);
            
            FileInputStream mappingfilein2=null;
            String mappingfile2 = folder+ entityName + "_clob.hbm.xml";
            File file2=new File(mappingfile2);
            if(file2.exists()){
                mappingfilein2=new FileInputStream(file2);
                config.addInputStream(mappingfilein2);
            }
            
            config.reBuildSessionFactory(sessionFactory);
            
            try{
            	mappingfilein.close();
            }catch(Exception ex){ex.printStackTrace();}
            try{
            	if(mappingfilein2!=null)
            		mappingfilein2.close();
            }catch(Exception ex){ex.printStackTrace();}
        }
		
	}
	/**
     * 获取当前表单所对应的数据库表的现有字段
     * @param cn
     * @param table
     * @return
     * @throws Exception
     */
    private Vector getField(Connection cn,String table) throws Exception{
        Vector fields=new Vector();
        String sql="select * from "+table+" where id='00'";
        PreparedStatement pst=null;
        try{
            pst=cn.prepareStatement(sql);
            ResultSet rst=pst.executeQuery();
            for(int num=1;num<=rst.getMetaData().getColumnCount();num++){
                fields.add(rst.getMetaData().getColumnName(num));
            }
            rst.close();
            pst.close();
            pst=null;
        }catch(Exception ex){
    	}finally{
        	if(pst!=null) try{pst.close();}catch(Exception ex){}
        }
        return fields;
    }

}
