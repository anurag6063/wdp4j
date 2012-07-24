/*
 * Created on 2005-10-12
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.hiromsoft.hiromform;

import java.util.Iterator;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.util.NodeList;

import com.hiromsoft.utils.HtmlUtil;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TemplateParser {
    
    private int fieldcnt=0;
    private Vector exitsField=null;
    private HtmlFormTemplate htmlform=null;
    
    public void parse(HtmlFormTemplate htmlform,Vector exitsFields) throws Exception
    {
    	this.htmlform=htmlform;
        this.exitsField=exitsFields;
        Parser parser=new Parser();
        parser.setEncoding("GBK");
        if(htmlform.getContent()!=null&&htmlform.getContent().length()>5)
        {
        	String b=htmlform.getContent().substring(0,5).toLowerCase();
        	if(!b.equals("<body")){
        		parser.setInputHTML("<BODY>"+htmlform.getContent()+"</BODY>");
        	}else{
        		parser.setInputHTML(htmlform.getContent());
        	}
        }else{	
        	parser.setInputHTML(htmlform.getContent());
        }
        
        TagNameFilter filter=new TagNameFilter();
        filter.setName("BODY");
        NodeList list=parser.parse(filter);
        if(list.size()!=0)
        {
            Vector fields=htmlform.getFields();
            Tag tag=(Tag)list.elementAt(0);
            tag.toHtml();
            internalParse(tag,fields);
            htmlform.setContent(tag.toHtml());
            
        }
        
        Iterator it=htmlform.getFields().iterator();
        Vector names=new Vector();
        while(it.hasNext())
        {
            TemplateField field=(TemplateField)it.next();
            String fieldname=field.getFieldname();
            if(fieldname!=null&&fieldname.length()!=0){
	           // if(fieldname.indexOf("field")==-1){
	                if(!hasName(names,fieldname)){
	                    names.add(fieldname);
	                    if(field.isKey())
	                    	htmlform.setKeyFieldName(fieldname);
	                }else
	                    it.remove();
	           // }
            }
            
        }
        
        
    }
    void internalParse(Tag tag,Vector fields) throws Exception
    {
        try{
	        String tagName=tag.getTagName().toLowerCase();
	        TemplateField htmltag=null;
	        if(tagName.equals("input"))
	        {
	            String type=tag.getAttribute("type");
	            if(type!=null)
	            {
	                type=type.toLowerCase().trim();
	                if(type.equals("text")||type.equals("textbox"))
	                {
	                    htmltag= buildField(tag);
	                    //String title=tag.getAttribute("fieldtitle");
	                   // if(title==null||title.length()==0)
	                        tag.setAttribute("value","{"+tag.getAttribute("fieldname")+"}");
	                }else if(type.equals("password")){
	                    htmltag= buildField(tag);
	            	}else if(type.equals("checkbox")){
	            	    htmltag= buildField(tag);
	            	}else if(type.equals("radio")){
	            	    htmltag= buildField(tag);
	            	}else if(type.equals("image")){
	            		htmltag= buildField(tag);
	            	}
	            }else
	            {
	                tag.setAttribute("type","textbox");
	                htmltag= buildField(tag);
	                //String title=tag.getAttribute("fieldtitle");
                   // if(title==null||title.length()==0)
                        tag.setAttribute("value","{"+tag.getAttribute("fieldname")+"}");
	            }
	        }else if(tagName.equals("select"))
	        {
	            htmltag= buildField(tag);
	        }else if(tagName.equals("textarea"))
	        {
	            htmltag= buildField(tag);
	            String title=tag.getAttribute("fieldtitle");
                if(title==null||title.length()==0){
                    try{
                        String fldname=tag.getAttribute("fieldname");
                        TextareaTag tag3=(TextareaTag)tag;
                        tag3.setText("{"+fldname+"}");
                    }catch(Exception e){}
                }
	        }
	        if(htmltag!=null)
	            fields.add(htmltag);
        }catch(Exception e)
        {
            throw e;
        }
       
        NodeList list= tag.getChildren();
        if(list!=null){
	        for(int num=0;num<list.size();num++)
	        {
	            Node node=list.elementAt(num);
	            if(node instanceof Tag)
	            {
	                internalParse((Tag)node,fields);
	            }
	        }
        }
        
        
        
    }
    TemplateField buildField(Tag tag)
    {
        TemplateField retval=null;
        if(tag==null) return retval;
        
        String fieldname=tag.getAttribute("fieldname");
        //if(fieldname!=null&&Pattern.matches(".+\\_f\\d+\\z", fieldname)) return retval;
        
        tag.setAttribute("formid",Integer.toString(htmlform.getFormId()));
        
        retval=new TemplateField();
        
        if(fieldname!=null){
            if(fieldname.length()==0){
               //||fieldname.toLowerCase().indexOf("field")!=-1 
                fieldname=getNewFieldName();
                tag.setAttribute("fieldname",fieldname);
            }
        }else
        {
            fieldname=getNewFieldName();
            tag.setAttribute("fieldname",fieldname);
        }
        
        retval.setFieldname(fieldname);
        retval.setPropname(fieldname);
        
        String datatype=null;
        String iskey=tag.getAttribute("iskey");
        if(iskey!=null&&iskey.equals("1")){
        	retval.setKey(true);
        }
        String datadict=tag.getAttribute("sqlDataSource");
        if(datadict!=null&&datadict.length()!=0){
        	retval.setDatadict(HtmlUtil.HTMLDecode(datadict));
        }
        String formstate=tag.getAttribute("formstate");
        if(formstate!=null&&formstate.length()!=0){
        	retval.setFieldstate(formstate);
        }
        String qianZheng=tag.getAttribute("qianZheng");
        if(qianZheng!=null&&qianZheng.equals("1")){
        	datatype="clob";
        }else{
        	datatype=tag.getAttribute("fieldtype");
        	if(datatype==null) datatype="char";
        }
        retval.setDatatype(datatype);
        
        
        String title=tag.getAttribute("fieldtitle");
        if(title==null) title="";
        retval.setTitle(Utils.deleteKuoHao(title));
        retval.setName(tag.getAttribute("name"));
        
        String noinsert=tag.getAttribute("noinsert");
        if(noinsert!=null&&noinsert.equals("1"))
        	retval.setNoInsert(true);
        
        String noupdate=tag.getAttribute("noupdate");
        if(noupdate!=null&&noupdate.equals("1"))
        	retval.setNoUpdate(true);
        
        String autoincrease=tag.getAttribute("autoincrease");
        if(autoincrease!=null&&autoincrease.equals("1"))
        	retval.setAutoIncrease(true);
        
        String required=tag.getAttribute("required");
        String validations="";
        if(required!=null&&required.equals("1"))
        {
            validations="required,";
        }
        String maxlength=tag.getAttribute("maxlength");
        if(maxlength!=null)
        {
            validations=validations+"maxlength="+maxlength+",";
        }
        
        String min=tag.getAttribute("min");
        if(min!=null)
        {
            
            String max=tag.getAttribute("max");
            if(max==null) max="9999999999";
            validations=validations+"range="+min+"#"+max+",";
                
        }else
        {
            min="0";
            String max=tag.getAttribute("max");
            if(max!=null){
                validations=validations+"range="+min+"#"+max+",";
            }
        }
                   
        
        if(datatype!=null)
        {
        	retval.setPropname(getPropName(retval));
            if(datatype.equals("numeric")||datatype.equals("int")){
            	String dv=tag.getAttribute("dv2");
            	if(dv==null||dv.length()==0)
            		validations=validations+"numeric,";
            }else if(datatype.equals("date")){
            	String dv=tag.getAttribute("dv2");
            	if(dv==null||dv.length()==0)
            		validations=validations+"date,";
            }   
        }
        
        if(validations.length()!=0)
            retval.setValidations(validations.substring(0,validations.length()-1));
   
        return retval;
    }
    public static String getPropName(TemplateField tag)
    {
        String retval=tag.getFieldname();
        String fieldtype=tag.getDatatype();
        retval=getPropName(fieldtype,retval);
        return retval;
    }
    public static String getPropName(String fieldtype,String fieldname)
    {
        return fieldname;
    	/*
        String retval=fieldname;
  
        if(fieldtype!=null)
        {
        	if(fieldtype.equals("numeric")){
                retval=retval+"_float";
            }else if(fieldtype.equals("int")){
            	retval=retval+"_int";
            }else if(fieldtype.equals("date")){
            	retval=retval+"_date";
            }else if(fieldtype.equals("datetime")){
            	retval=retval+"_datetime";
            }
        }
        return retval;*/
    }
    
    boolean hasName(Vector names,String newname)
    {
        boolean retval=false;
        Iterator it=names.iterator();
        while(it.hasNext())
        {
            String tmp=(String)it.next();
            if(tmp!=null)
            {
                if(tmp.equalsIgnoreCase(newname)){
                    retval=true;
                    break;
                }
            }
        }
        return retval;
    }
    private String getNewFieldName(){
        String field=null;
        while(true){
            field="field"+fieldcnt;
            fieldcnt++;
	        if(!hasName(this.exitsField,field)){
	            break;
	        }
        }
        return field;
    }

}
