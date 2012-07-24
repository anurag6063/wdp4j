/*
 * Created on 2006-10-21
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.hiromsoft.hiromform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.apache.struts.util.ResponseUtils;

import com.hiromsoft.hiromform.UserDataForm;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ViewWriter {
    
    private SimpleDateFormat sdf=null;
    /**
     * 
     */
    public ViewWriter() {
        super();
        // TODO Auto-generated constructor stub
    }
    public void writeText(JspWriter out,UserDataForm shenqingbiao,String property) {
    	this.writeText(out, shenqingbiao, property, property,0,"content-readonly",null);
    }
    public void writeText(JspWriter out,UserDataForm shenqingbiao,String property,String title){
    	this.writeText(out, shenqingbiao, property, title, 0,"content-readonly",null);
    }
    public void writeCheckbox(JspWriter out,UserDataForm shenqingbiao,String property,String title,int fid) {
        String formId=Integer.toString(fid);
        if(fid==0){
	        Object aaa=shenqingbiao.getValue("__entityname");
	        if(aaa!=null)
	        	formId=aaa.toString().replaceAll("designer_userform", "");
        }
        try{
            out.print("<span spantype=\"checkbox\" propname=\"F"+formId+"_"+property+"\" proptitle=\""+title+"\" style=\"display:none\"></span>");
        }catch(Exception e){e.printStackTrace();}
        
    }
    public void writeText(JspWriter out,UserDataForm shenqingbiao,String property,String title,int fid,String className) {
    	this.writeText(out, shenqingbiao, property, title, fid, className,null);
    }
    public void writeText(JspWriter out,UserDataForm shenqingbiao,String property,String title,int fid,String className,String style) {
    	String tmp="";
        Object val= shenqingbiao.getValue(property);
        String formId=Integer.toString(fid);
        if(fid==0){
	        Object aaa=shenqingbiao.getValue("__entityname");
	        if(aaa!=null)
	        	formId=aaa.toString().replaceAll("designer_userform", "");
        }
        String styletext="";
        if(style!=null&&style.length()!=0)
        	styletext=" style=\""+style+"\" ";
        if(val!=null){
            if(val instanceof Date)
            {
                if(sdf==null)
                    sdf=new SimpleDateFormat("yyyy��MM��dd��");
                tmp=sdf.format((Date)val);
            }else
                tmp=val.toString();
            
            try{
                out.print("<span propname=\"F"+formId+"_"+property+"\" proptitle=\""+title+"\" class=\""+className+"\" "+styletext+">"+ResponseUtils.filter(tmp).replaceAll("\r\n", "<br>").replaceAll(" ", "&nbsp;")+"</span>");
            }catch(Exception e){e.printStackTrace();}
        }else{
            try{
                out.print("<span propname=\"F"+formId+"_"+property+"\" proptitle=\""+title+"\" class=\""+className+"\" "+styletext+">&nbsp;</span>");
            }catch(Exception e){}
        }
    }
    
    public void writeText(JspWriter out,UserDataForm shenqingbiao,String property,String title,int fid) {
        this.writeText(out, shenqingbiao, property, title, fid, "content-readonly",null);
    }
    public void writeTextForSelect(JspWriter out,UserDataForm shenqingbiao,
    		String property,Object session) {
    	this.writeTextForSelect(out, shenqingbiao, property, session, property, 0,"content-readonly",null);
    }
    public void writeTextForSelect(JspWriter out,UserDataForm shenqingbiao,
    		String property,Object session,String title){
    	this.writeTextForSelect(out, shenqingbiao, property, session, title, 0,"content-readonly",null);
    }
    public void writeTextForSelect(JspWriter out,UserDataForm shenqingbiao,
    		String property,Object session,String title,int fid) {
    	this.writeTextForSelect(out, shenqingbiao, property, session, title, fid,"content-readonly",null);
    }
    public void writeTextForSelect(JspWriter out,UserDataForm shenqingbiao,
    		String property,Object session,String title,int fid,String className){
    	this.writeTextForSelect(out, shenqingbiao, property, session, title, fid, className, null);
    }
    
    public void writeTextForSelect(JspWriter out,UserDataForm shenqingbiao,
    		String property,Object session,String title,int fid,String className,String style) {
        String tmp="";
        String val= (String)shenqingbiao.getValue(property);
        if(val==null) return;
        String formId=Integer.toString(fid);
        if(fid==0){
	        Object aaa=shenqingbiao.getValue("__entityname");
	        if(aaa!=null)
	        	formId=aaa.toString().replaceAll("designer_userform", "");
        }
        Vector codes=null;
        Vector labels=null;
        if(session instanceof HttpSession){
        	codes=(Vector)((HttpSession)session).getAttribute(property+"values");
        	labels=(Vector)((HttpSession)session).getAttribute(property+"labels");
        }else{
        	codes=(Vector)((HttpServletRequest)session).getAttribute(property+"values");
        	labels=(Vector)((HttpServletRequest)session).getAttribute(property+"labels");
        }
        if(codes==null||labels==null) return;
        String label="";
        for(int num=0;num<codes.size();num++){
        	tmp=(String)codes.get(num);
        	if(tmp!=null&&tmp.equals(val)){
        		label=(String)labels.get(num);
        		break;
        	}
        }
        if(label!=null&&label.startsWith("|—")){
        	for(int num=2;num<label.length()-1;num++){
        		if(label.charAt(num)=='—') continue;
        		label=label.substring(num);
        		break;
        	}
        }
        try{
            out.print("<span propname=\"F"+formId+"_"+property+"\" proptitle=\""+title+"\" class=\""+className+"\">"+ResponseUtils.filter(label)+"</span>");
        }catch(Exception e){e.printStackTrace();}
        
    }
    public void writeQianMing(JspWriter out,UserDataForm shenqingbiao,String property,HttpServletRequest request){
        String tmp="";
        Object val= shenqingbiao.getValue(property);
        if(val!=null){
            tmp=val.toString();
            if(tmp.length()!=0){
	            try{
	                out.print("<img src='"+request.getContextPath()+"/image/qianming/"+val+"' style='border:none'></img>");
	            }catch(Exception e){e.printStackTrace();}
            }else{
                try{
                    out.print("&nbsp;");
                }catch(Exception e){}
            }
        }else{
            try{
                out.print("&nbsp;");
            }catch(Exception e){}
        }
    }
    

    public static void main(String[] args) {
    }
}
