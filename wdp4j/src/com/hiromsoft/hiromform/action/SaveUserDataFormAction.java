package com.hiromsoft.hiromform.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.MapProxy;


import com.hiromsoft.hiromform.HiromTokenProcessor;
import com.hiromsoft.hiromform.UserDataForm;

import com.hiromsoft.types.translator.BaseTranslator;
import com.hiromsoft.types.translator.ContextTranslator;
import com.hiromsoft.types.translator.NullContextTranslator;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.LobUtils;
import com.hiromsoft.utils.RuntimeSetting;

public class SaveUserDataFormAction extends Action {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) {
		
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		
		String _msg_error="数据保存失败。";
		UserDataForm designedForm = (UserDataForm) form;
		
		try{
			if(HiromTokenProcessor.getInstance().isTokenValid(request, response, true)){
				return this.doSaveAction(mapping, designedForm, request, response);
			}else{
				request.setAttribute(Global.HIROM_MSG_ERROR, "数据已经保存，本次刷新未保存新数据");
			}
		}catch(Exception ex){
			ex.printStackTrace();
			request.setAttribute(Global.HIROM_MSG_ERROR, _msg_error+"<br>"+ex.getMessage());
		}
		return this.getActionForward(mapping, designedForm, request, false, false);
		
	}
	public ActionForward doSaveAction(ActionMapping mapping,
			UserDataForm designedForm,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		
		
		String onsave=request.getParameter("aftersave");
		String beforesave=request.getParameter("beforesave");
		
		String _msg_error="数据保存失败。";
		String _msg_sucsess="数据成功保存。";
		
		boolean isNew=false;
		boolean OK=false;
		
		String formname=(String)designedForm.getValue("__entityname");
		
		this.TranslateDataType(designedForm.getMap(), request, designedForm);
		//}catch(Exception ex){ex.printStackTrace();}
		
		
		ISaveFormActionExecutor beforeSaveExecutor=null;
		ISaveFormActionExecutor afterSaveExecutor=null;
	
		Session dbsession = null;
		Transaction trans = null;
		try {
			if(beforesave!=null&&beforesave.length()!=0){
				beforeSaveExecutor =(ISaveFormActionExecutor)(Class.forName("com.hiromsoft.hiromform.action.beforesave."+beforesave).newInstance());
			}
			if(beforeSaveExecutor==null) beforeSaveExecutor=new BaseOnSaveAction();
			beforeSaveExecutor.setService(this);
			
			if(onsave!=null&&onsave.length()!=0){
				afterSaveExecutor=(ISaveFormActionExecutor)(Class.forName("com.hiromsoft.hiromform.action.aftersave."+onsave).newInstance());
				afterSaveExecutor.setService(this);
			}
			
			
			dbsession = com.hiromsoft.utils.DatabaseUtil.getHibernateSession();
			trans = dbsession.beginTransaction();
			
			designedForm.setValue("__service", this);
			
			this.parseChildObjects(designedForm, request, response, beforeSaveExecutor, dbsession);
			
			if("0".equals(designedForm.getMap().get("ID"))){
				isNew=true;
				designedForm.setValue("__isnewobj", "1");
			}
			
			
			beforeSaveExecutor.execute(request, response, dbsession, designedForm);
			
			if(beforeSaveExecutor.canGoon()){
				if(formname!=null&&formname.length()!=0){
					dbsession.saveOrUpdate(formname,designedForm.getMap());
					this.saveOrUpdateClobContent(dbsession, formname, designedForm);
					this.saveChildObjects(request, response, dbsession, designedForm, beforeSaveExecutor);
				}
				if(afterSaveExecutor!=null){
					afterSaveExecutor.execute(request, response, dbsession, designedForm);
				}
			}
			//加载表单的状态检查器
			if(!isNew){
				try{
					IFormStateLoader loader=(IFormStateLoader)(Class.forName("com.hiromsoft.hiromform.action.formstateloader."+formname+"_loader").newInstance());
					if(loader!=null){
						loader.load(request, response, dbsession, designedForm);
					}
				}catch(java.lang.ClassNotFoundException ex){}
			}
			trans.commit();
			request.setAttribute(Global.HIROM_MSG_SECSESS, _msg_sucsess);
			OK=true;
		}catch(Exception e)
		{
			e.printStackTrace();
			if(trans!=null)
			{
				try{
					trans.rollback();
				}catch(Exception ex)
				{}
			}
			e.printStackTrace();
			request.setAttribute(Global.HIROM_MSG_ERROR, _msg_error+"<br>"+e.getMessage());
		}finally{
			if(dbsession!=null)
			{
				try{
					dbsession.close();
				}catch(Exception ex)
				{
				}
			}
		}
		
		return this.getActionForward(mapping, designedForm, request, isNew, OK);
	}
	
	public void saveChildObjects(HttpServletRequest request,
			HttpServletResponse response, Session dbsession,
			UserDataForm userDataForm,ISaveFormActionExecutor onsaver) throws Exception{
		Vector objs=userDataForm.getChildObjects();
		if(objs==null) return;
		for(int num=0;num<objs.size();num++){
			HashMap obj=(HashMap)objs.get(num);
			onsaver.saveChildObjects(request, response, dbsession, userDataForm, obj, (String)obj.get("__action"));
		}
		
		
	}
	
	private Vector parseChildObjects(UserDataForm designedForm, HttpServletRequest request,HttpServletResponse response,
			ISaveFormActionExecutor onsaver,Session dbsession) throws Exception{
		String child=(String)designedForm.getValue("__hiromchildlist");
		if(child==null||child.length()==0) return null;
		String[] strobjs=child.split("<objs>");
		Vector retval=new Vector();
		for(int num=0;num<strobjs.length;num++){
			if(strobjs[num].length()==0) continue;
			String[] tmp=strobjs[num].split("<para>");
			if(tmp.length<3) continue;
			if(tmp[1].length()==0) continue;
			if(tmp[2].length()==0) continue;
			String[] props=tmp[1].split("\t");
			String rows[]=tmp[2].split("<br>");
			for(int row=0;row<rows.length;row++){
				HashMap obj=new HashMap();
				obj.put("__entityname", tmp[0]);
				String rowdata=rows[row];
				if(rowdata.length()==0) continue;
				retval.add(obj);
				String datas[]=rowdata.split("\t");
				obj.put("__action", datas[0]);
				obj.put("ID",datas[1] );
				onsaver.parseChildObjects(request, response, dbsession, designedForm, props, datas, obj, datas[0]);
			}
			
		}
		designedForm.setChildObjects(retval);
		return retval;
		
	}
	
	public void saveOrUpdateClobContent(Session dbsession,String formname,UserDataForm designedForm ) throws Exception{
		//若表单含有大文本区域，则需要调用该函数进行存储
		ClassMetadata meta=dbsession.getSessionFactory().getClassMetadata(formname+"_clob");
		if(meta!=null){
			dbsession.flush();
			String rid=(String)designedForm.getMap().get("ID");
			String fields[]=meta.getPropertyNames();
			MapProxy clobobj=(MapProxy)dbsession.load(formname+"_clob", rid);
			for(int num=0;num<fields.length;num++){
				String tmp=fields[num].toLowerCase();
				if(!tmp.equals("id")){
					clobobj.put(fields[num], Hibernate.createClob("1"));
				}
			}
			dbsession.update(clobobj);
			dbsession.flush();
			
			dbsession.refresh(clobobj, LockMode.UPGRADE);
			
			for(int num=0;num<fields.length;num++){
				String tmp=fields[num].toLowerCase();
				if(!tmp.equals("id")){
					if(com.hiromsoft.utils.Global.DBTYPE.equalsIgnoreCase("oracle")) {
						LobUtils.setContent(clobobj, fields[num], (String)(designedForm.getMap().get(fields[num])));
					}
				}
			}
			dbsession.flush();
		}
		
	}
	private ActionForward getActionForward(ActionMapping mapping,
			UserDataForm designedForm,
			HttpServletRequest request,boolean isNew,boolean OK){
		ActionForward retval=null;
		String forward="";
		String view=request.getParameter("view");
		if(view!=null&&view.length()!=0)
	    	retval=new ActionForward(view);
	    if(forward.length()!=0)
	    	retval=mapping.findForward(forward);
	    if(retval==null){
	    	if(isNew){
	    		if(OK){
	    			String editable=request.getParameter("editable");
	    			if("1".equals(editable)){
	    				String pageName=new OpenUserDataFormAction().getPageName(designedForm, request);//new ActionForward(new CreateUserDataFormAction().getPageName(designedForm));
	    				retval=new ActionForward(pageName);
	    			}else{
	    				designedForm.setValue("__gooncreate", "1");
	    				retval=new ActionForward("/createExcel.sv");
	    			}
	    		}else{
	    			designedForm.setValue("ID", "0");
	    			retval=new ActionForward(new CreateUserDataFormAction().getPageName(designedForm));
	    		}
	    	}else{
	    		
	    		String pageName=new OpenUserDataFormAction().getPageName(designedForm, request);
	    		retval=new ActionForward(pageName);
	    	}
	    }
	    return retval;
	}
	
	public void TranslateDataType(HashMap datas,HttpServletRequest request,UserDataForm userDataForm) throws Exception {
	    BaseTranslator tp=ContextTranslator.getInstance();
		((ContextTranslator)tp).TranslateSystemProperties(datas, request, userDataForm);

		String fid=(String)datas.get("__templateId");
        if(fid==null||fid.length()==0){
        	 String entityname=(String)datas.get("__entityname");
        	 fid=entityname.replaceAll("designer_userform", "");
        }
        if(fid==null||fid.length()==0) return;
        HashMap trans=RuntimeSetting.getFormDataTypeTranslator(fid);
        if(trans==null) return;
        Vector NullContextTrans=new Vector(1);
        Iterator it=trans.entrySet().iterator();
        Map.Entry entity=null;
        while(it.hasNext()){
        	entity=(Map.Entry)it.next();
        	Object tmp=entity.getValue();
        	BaseTranslator translator=null;
        	String expr=null;
        	if(tmp instanceof BaseTranslator)
        		translator=(BaseTranslator)tmp;
        	else if(tmp instanceof Vector){
        		Vector tmp2=(Vector)tmp;
        		translator=(BaseTranslator)tmp2.get(1);
        		expr=(String)tmp2.get(0);
        	}	
        	String propName=entity.getKey().toString();
        	propName=propName.substring(0, propName.length()-2);
        	if(translator instanceof NullContextTranslator){
        		NullContextTrans.add(translator);
        		NullContextTrans.add(propName);
        		NullContextTrans.add(expr);
        		continue;
        	}
        	if(translator instanceof ContextTranslator){
        		((ContextTranslator)translator).Translate(datas, propName, expr,request, userDataForm);
        	}else{
        		translator.Translate(datas, propName);
        	}
        }
        for(int num=0;num<NullContextTrans.size();num+=3){
        	NullContextTranslator translator=(NullContextTranslator)NullContextTrans.get(num);
        	translator.Translate(request, userDataForm,datas, (String)NullContextTrans.get(num+1), (String)NullContextTrans.get(num+2));
        }
        
    }

}