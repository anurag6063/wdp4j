package com.hiromsoft.utils;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Hibernate;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDHexGenerator;

import com.hiromsoft.hiromform.UserDataForm;

public class RequestUtil {

	public String getParameterValueFromRequestOrUserDataFrom(HttpServletRequest request,UserDataForm userDataForm,String name){
		String retval=null;
		if(request!=null)
			retval=request.getParameter(name);
		if((retval==null||retval.length()==0)&&userDataForm!=null){
			retval=(String) userDataForm.getValue(name);
		}
		return retval;
	}
	public String[] getParameterValuesFromRequestOrUserDataFrom(HttpServletRequest request,UserDataForm userDataForm,String name){
		String retval[]=null;
		if(request!=null)
			retval=request.getParameterValues(name);
		if((retval==null||retval.length==0)&&userDataForm!=null){
			retval= userDataForm.getArrayValue(name);
			if(retval!=null&&retval.length==1)
				retval=null;
		}
		return retval;
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
	public static String getRealPath(String path){
		String retval=path;
		String sep=System.getProperty("file.separator");
		if(sep.equals("\\")) sep="\\\\";
		if(!sep.equals("/"))
			retval=retval.replaceAll("\\/", sep);
		return retval;
	}
	public static int getListSelection(HttpServletRequest request,String[] ids){
		return getListSelection(request,ids,"rid");
	}
	public static int getListSelection(HttpServletRequest request,String[] ids,String name){
		int rowidx=0;
		int maxrows=ids.length;
		String aa[]=request.getParameterValues(name);
		if(aa!=null&&aa.length!=0){
			for(int num=0;num<aa.length&&rowidx<maxrows;num++){
				ids[rowidx]=aa[num];
				rowidx++;
			}
		}
		
		if(rowidx==0){
			String ID=request.getParameter("id");
			if(ID==null||ID.length()==0)
			{
				ID=request.getParameter("ID");
				if(ID==null||ID.length()==0){
					ID=request.getParameter("Id");
					if(ID==null||ID.length()==0){
						ID=request.getParameter("iD");
					}
				}
			}
		
			if(ID!=null&&ID.length()!=0){
				String bb[]=ID.split(",");
				if(bb!=null&&bb.length!=0){
					for(int num=0;num<bb.length&&rowidx<maxrows;num++){
						ids[rowidx]=bb[num];
						rowidx++;
					}
				}
			}
		}
		return rowidx;
	}
	
	public static void saveUserRandomCode(HttpSession session,String code){
		session.setAttribute(Global.HIROM_USERRANDOMCODE, code);
	}
	public static String getUserRandomCode(HttpSession session){
		return (String)session.getAttribute(Global.HIROM_USERRANDOMCODE);
	}
	
}
