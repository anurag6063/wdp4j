package com.hiromsoft.hiromform;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import sql4j.parser.Column;
import sql4j.parser.SQL;
import sql4j.parser.SelectStatement;

import com.hiromsoft.hiromview.action.ShowListViewAction;
import com.hiromsoft.utils.Global;
import com.hiromsoft.utils.HtmlUtil;


/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TemplatePageMaker {
    
    private String folder;
    private String sep=null;
    private StringBuffer scripts=new StringBuffer("");
    private StringBuffer childformshtml=new StringBuffer("");
    private HtmlFormTemplate htmlform;
    private List multiboxFieldList=new Vector();
    private Connection conn;
    private String fieldname_suffix="";
    private HttpServletRequest request=null;
    
    public TemplatePageMaker(){
        sep=System.getProperty("file.separator");
    }
    
    public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void buildJsp(HtmlFormTemplate htmlform) throws Exception
    {
    	this.htmlform = htmlform;
        if(folder!=null&&folder.length()!=0)
        {
            File file=new File(folder);
            if(file.exists())
            {
            	//此处代码执行顺序不能改变
                writeJsp(htmlform,null,"request",null);
                writeJspForPrint(htmlform);
                writeJspForReadonly(htmlform);
                writeSearchBar(htmlform);
                this.fieldname_suffix="_f"+htmlform.getFormId();
                this.scripts=new StringBuffer();
                writeJspForGridEditor(htmlform,null,"request","_grideditor");
                this.writeJspForChildForm(htmlform,null,"request","_childform");
            }
            
        }
        return;
    }
    private void writeSearchBar(HtmlFormTemplate htmlform) throws Exception{
        StringBuffer jsp=buildJspForSearchbar(htmlform);
        String filename="template"+htmlform.getFormId()+"_searchbar.jsp";
        File jspfile=new File(folder+sep+filename);
        FileOutputStream out=new FileOutputStream(jspfile);
        String header="<%@ page language=\"java\" contentType=\"text/html;charset=GBK\"%>\r\n"+
        				"<%@ taglib uri=\"http://jakarta.apache.org/struts/tags-html\" prefix=\"html\" %>\r\n"+
        			   "<jsp:useBean id=\"userDataForm\" scope=\"request\" class=\"com.hiromsoft.hiromform.UserDataForm\"/>\r\n"+
        			  "<%\r\n"+
        			  		"\tString path = request.getContextPath();\r\n"+
        			  		"\tString basePath = request.getScheme()+\"://\"+request.getServerName()+\":\"+request.getServerPort()+path+\"/\";\r\n"+
        			  		"\tString tmp = null;\r\n"+
        			  		"\tString[] tmparray = null;\r\n"+
        			  		"\tString formstate=(String)userDataForm.getValue(\"__formstate\");\r\n"+
        			  "%>\r\n";
        			  
        out.write(header.getBytes("GBK"));
        out.write(("<html:hidden name=\"userDataForm\" property=\"value(sft_"+htmlform.getTableName()+")\"/>\r\n").getBytes("GBK"));
        //out.write(makeHiddenText("userDataForm").getBytes("GBK"));
        out.write(jsp.toString().getBytes("GBK"));
        out.close();
    }
    private StringBuffer buildJspForSearchbar(HtmlFormTemplate htmlform)throws Exception{
    	StringBuffer retval=new StringBuffer();
    	String compares1[]={"like","not like","=","!=",">",">=","<","<=","between"};
		String compares2[]={"含有","不含","&nbsp;＝","&nbsp;≠","&nbsp;＞","&nbsp;≥","&nbsp;＜","&nbsp;≤","位于"};
		String code1[]=",1,2,3,4,5".split(",");
		String name1[]="&nbsp;,1,2,3,4,5".split(",");
		String code2[]="count,sum,avg,max,min".split(",");
		String name2[]="&nbsp;个数&nbsp;,&nbsp;求和&nbsp;,平均数,最大数,最小数".split(",");
		
		retval.append("<table class=\"search-panel\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" style=\"border:none;border-collapse: collapse; font-size: 14px\" bordercolor=\"#E1E1E1\">");
		retval.append("\r\n<tr class=\"search-header\" ><td   align=\"center\" rowspan=\"2\"  colspan=\"3\" nowrap>查询条件</td><td  align=\"center\" colspan=\"1\" nowrap rowspan=\"2\">分组</td>");
		retval.append("<td  colspan=\""+name2.length+"\" align=\"center\" nowrap >汇总</td><td nowrap align=\"center\" colspan=\"3\">显示与顺序</td></tr>");		
		retval.append("<tr class=\"search-header\" >");
		for(int num2=0;num2<name2.length;num2++){
			retval.append("<td >" +name2[num2]+"</td>");
		}
		retval.append("<td  align=\"center\"  nowrap >字段名称</td>");
		retval.append("<td align=\"center\" nowrap >显示</td>");
		retval.append("<td align=\"center\" nowrap >序号</td>");
	
		retval.append("</tr>");
		
		try{
			for(int num=0;num<htmlform.getFields().size();num++){
	    		TemplateField field=(TemplateField)htmlform.getFields().get(num);
	    		if(Pattern.matches(".+\\_f\\d+\\z", field.getFieldname())) continue; 
	    		String[][] choiceitem=null;
	    		try{choiceitem=getChoiceItem(field);}catch(Exception ex){}	    		
	    		boolean has1=false;
	  		  	boolean has2=false;
	  		  	String tmp="";
	  		  	String tmp2="";
	  		  	if(field.getFieldstate()!=null&&field.getFieldstate().length()!=0){
	  		  		tmp=this.getFormStateStr(field.getFieldstate(), TemplateField.FST_READONLY);   //readonly
	  		  		tmp2=this.getFormStateStr(field.getFieldstate(), TemplateField.FST_DISVISIBLE); 
	  		  	}
				if(tmp.length()!=0){
					//只读的处理
					retval.append("\r\n<%"+tmp+"%>\r\n");
					this.createSearchItem(retval, field, compares1, compares2, code1, name1, code2, name2,true,choiceitem);
					retval.append("\r\n<%}else{%>\r\n");
						has1=true;
				}
				if(tmp2.length()!=0){
					//不可见的处理
					retval.append("\r\n<%"+tmp2+" int a=1;%>\r\n");
					retval.append("\r\n<%}else{%>\r\n");
						has2=true;
				}
	    		this.createSearchItem(retval, field, compares1, compares2, code1, name1, code2, name2,false,choiceitem);
	    		if(has1&&has2){
	            	retval.append("\r\n<%}}%>\r\n");
	            }else{
	            	if(has1||has2){
	            		retval.append("\r\n<%}%>\r\n");
	            	}
	            }
	    	}
		}finally{}
    	retval.append("\r\n<tr class=\"search-header\" height=\"30\"><td colspan=\"2\" style=\"padding-left:10px\">");
    	retval.append("<html:checkbox   name=\"userDataForm\" value=\"1\" property=\"value(jt_"+htmlform.getTableName()+")\"/>").append("全部数据(左连接)");
    	String vals=htmlform.getName();
    	retval.append("<html:hidden name=\"userDataForm\" value=\""+vals+"\" property=\"value(forminfo_").append(htmlform.getTableName()).append(")\"/>");
    	retval.append("</td><td colspan=\"10\" align=\"center\" style=\"padding-left:10px\"><input type=\"button\"  style=\"width:100px\" class=\"mybutton\" value=\"立即查询\" onclick=\"doSearch()\"></td></tr>");
    	retval.append("\r\n</table>");
    	return retval;
    }
    private String[][] getChoiceItem(TemplateField field) throws Exception{
    	String[][] retval=null;
    	if(field.getDatadict()==null||field.getDatadict().length()==0)
    		return retval;
    	String datadict=field.getDatadict();
    	boolean ok=false;
		String mm[]=datadict.split(";");
		datadict=mm[0];
		if(mm.length==1){
			datadict=Utils.escapeMySQLVars(datadict).toString();
			SQL dictsql=new SQL(datadict);
			SelectStatement bb=dictsql.getSelectStatement();
			if(bb!=null){
				Vector aa =bb.getColumns().toVector();
				if(aa.size()>=2){
					String key1=((Column)aa.get(0)).getName();
					String key2=((Column)aa.get(1)).getName();
					if(!key1.equals(key2)){
						ok=true;
					}
				}
			}
		}
		if(!ok) return retval;

    	PreparedStatement pst=null;
    	try{
    		datadict=Utils.unescapeMySQLVars(datadict).toString();
    		pst=new ShowListViewAction().createStatementAndParameters(this.request, this.conn, datadict);
    		//this.conn.prepareStatement(datadict);
    		ResultSet rst=pst.executeQuery();
    		int cnt=0;
    		retval=new String[3][50];
    		int maxlen=0;
    		while(rst.next()&&cnt<retval[0].length){
    			retval[0][cnt]=rst.getString(1);
    			retval[1][cnt]=rst.getString(2);
    			if(retval[1][cnt].length()>maxlen)
    				maxlen=retval[1][cnt].length();
    			cnt++;
    		}
    		retval[2][0]=""+maxlen;
    		rst.close();
    		pst.close();
    		pst=null;
    	}finally{
    		if(pst!=null)
    			try{pst.close();}catch(Exception ex){}
    	}
    	return retval;
    }
    private void createSearchItem(StringBuffer retval,TemplateField field,String compares1[],String compares2[],
    		String code1[],String name1[],String code2[],String name2[],boolean readonly,String[][] choiceitem) throws Exception{
    	String strreadonly="";
    	if(readonly)
    		strreadonly=" readonly ";
    	
    	retval.append("\r\n<tr onmouseover=\"this.className='list-row-selected'\" onmouseout=\"this.className='list-row-unselected'\">");
    	
    	if(choiceitem!=null&&choiceitem[0][0]!=null){
    		if(readonly)
    			retval.append("<td align=\"right\" class=\"search-cell\" rowspan=\"1\" width=\"160\">");
    		else
    			retval.append("<td align=\"right\"  class=\"search-cell1\"  rowspan=\"2\" width=\"160\">");
    	}else
    		retval.append("<td align=\"right\"  class=\"search-cell\"  width=\"160\">");
    	
		if(field.getTitle()!=null&&field.getTitle().length()!=0)
			retval.append(field.getTitle()).append("：</td>");
		else
			retval.append(field.getFieldname()).append("：</td>");
    	
		if(choiceitem!=null&&choiceitem[0][0]!=null){
			
			retval.append("<td align=\"left\" colspan=\"2\" class=\"search-choice search-cell\">");
			this.buildFieldInfo(htmlform, field, retval);
			retval.append("<html:hidden name=\"userDataForm\" value=\"=\" property=\"value(fc_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append(")\"/>");
			if(!readonly)
				retval.append("请从下面列表中选择:");
			retval.append("</td>");
		}else{
			retval.append("<td align=\"center\" class=\"search-cell\">");
			this.buildFieldInfo(htmlform, field, retval);
			if(field.getDatatype()==null||field.getDatatype().equals("char")){
				this.buildSelectForSearchbar(htmlform, field, retval, compares2, compares1, "fc",0,1);
			}else 
				this.buildSelectForSearchbar(htmlform, field, retval, compares2, compares1, "fc",2,10);
			retval.append("</td>");
			retval.append("<td align=\"center\" width=\"200px\" class=\"search-cell\">");
			if(field.getDatatype()==null||field.getDatatype().equals("date")){
				retval.append("<% tmp=(String)userDataForm.getValue(\"fv_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append("\");");
				retval.append("if(tmp!=null)tmparray=tmp.split(\"\t\");%>");
				retval.append("<input "+strreadonly+" style=\"width:85px\" onclick=\"try{return showCalendar2(this, '%Y-%m-%d', '24');}catch(e){}\" class=\"searchinput3\" name=\"arrayValue(fv_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append(")\" value=\"<%=(tmparray!=null&&tmparray[0]!=null)?tmparray[0]:\"\"%>\">");
				retval.append("&nbsp;至&nbsp;<input "+strreadonly+" style=\"width:85px\" onclick=\"try{return showCalendar2(this, '%Y-%m-%d', '24');}catch(e){}\" class=\"searchinput3\" name=\"arrayValue(fv_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append(")\" value=\"<%=(tmparray!=null&&tmparray.length>=2&&tmparray[1]!=null)?tmparray[1]:\"\"%>\">");
				
			}else{
				retval.append("<% tmp=(String)userDataForm.getValue(\"fv_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append("\");%>");
				retval.append("<input "+strreadonly+" class=\"searchinput3\" name=\"value(fv_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append(")\" value=\"<%=(tmp==null)?\"\":tmp%>\">");
			}
			retval.append("</td>");
		}
		
		retval.append("<td align=\"center\" class=\"search-cell\">");
		
		if(field.getDatatype()==null||field.getDatatype().equals("char"))
			this.buildSelectForSearchbar(htmlform, field, retval, name1, code1, "grpidx",0,10);
		
		for(int num2=0;num2<code2.length;num2++){
			retval.append("</td><td align=\"center\" class=\"search-cell\">");
			if(num2==0||(field.getDatatype()!=null&&(field.getDatatype().equals("numeric")||field.getDatatype().equals("int"))))
				retval.append("<html:multibox   name=\"userDataForm\" value=\""+code2[num2]+"\" property=\"arrayValue(cal_"+htmlform.getTableName()).append("__").append(field.getFieldname()+")\"/>");
			retval.append("</td>");
		}
		
		
		if(field.getTitle()!=null&&field.getTitle().length()!=0)
			retval.append("<td  class=\"search-cell\" align=\"right\" width=\"160\">").append(field.getTitle()).append("</td>");
		else
			retval.append("<td  class=\"search-cell\" align=\"right\" width=\"160\">").append(field.getFieldname()).append("</td>");
		
		retval.append("<td align=\"center\" class=\"search-cell\">");
		retval.append("<html:checkbox  style=\"width:20px;height:20px;\" name=\"userDataForm\" value=\"1\" property=\"value(dsp_"+htmlform.getTableName()).append("__").append(field.getFieldname()+")\"/>");
		retval.append("</td>");
		
		retval.append("<td class=\"search-cell\">");
		retval.append("<% tmp=(String)userDataForm.getValue(\"dspidx_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append("\");%>");
		retval.append("<input class=\"searchinput3\" style=\"width:40px\" name=\"value(dspidx_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append(")\" value=\"<%=(tmp==null)?\"\":tmp%>\">");
		retval.append("</td>");
		
	
		
		retval.append("</tr>");
	
		if(choiceitem!=null&&choiceitem[0][0]!=null){
			if(readonly)
	    		strreadonly=" style=\"display:none\"";
			int maxlen=Integer.parseInt(choiceitem[2][0]);
			if(maxlen<4) maxlen=4;
			retval.append("<tr "+strreadonly+" onmouseover=\"this.className='list-row-selected'\" onmouseout=\"this.className='list-row-unselected'\"><td colspan=\"11\"  class=\"search-cell2\"><table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">");
			boolean hadend=false;
			int cnt=0;
			int col=48/maxlen;
			if(col==0) col=1;
						
			for(int num=0;num<choiceitem[0].length;num++)
			{
				
				if(cnt%col==0){ 
					retval.append("<tr>");
					hadend=false;
				}
				cnt++;
				if(choiceitem[0][num]==null){
					retval.append("<td nowrap class=\"search-choice\"><html:multibox style=\"width:16px\" onclick=\"checkall(this)\"  name=\"userDataForm\" value=\"\" property=\"arrayValue(fv_"+htmlform.getTableName()).append("__").append(field.getFieldname()+")\"/>");
					retval.append("以上全部");
					retval.append("</td>");
					break;
				} 
				retval.append("<td nowrap class=\"search-choice\"><html:multibox  style=\"width:16px\" name=\"userDataForm\" value=\""+choiceitem[0][num]+"\" property=\"arrayValue(fv_"+htmlform.getTableName()).append("__").append(field.getFieldname()+")\"/>");
				retval.append(choiceitem[1][num]).append("&nbsp;");
				retval.append("</td>");
				
				if(cnt%col==0) {
					retval.append("</tr>");
					hadend=true;
				}
			}
			if(!hadend)
				retval.append("</tr>");
			retval.append("</table></td></tr>");
		}
		
    }
    
    private void buildSelectForSearchbar(HtmlFormTemplate htmlform,TemplateField field,StringBuffer retval,String[] names,
    		String[] codes,String prefix,int start,int end) throws Exception{
    	retval.append("<select class=\"search-select\" name=\"value("+prefix+"_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append(")\">");
		retval.append("<% tmp=(String)userDataForm.getValue(\""+prefix+"_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append("\");%>");
		for(int num2=start;num2<names.length&num2<=end;num2++){
			retval.append("<option ").append(" <% if(tmp!=null&&\""+codes[num2]+"\".equals(tmp)) out.print(\"selected\"); %> ");
			retval.append(" value=\""+HtmlUtil.HTMLEncode(codes[num2])+"\">").append(names[num2]).append("</option>");
		}
		retval.append("</select>");
    }
    private void buildFieldInfo(HtmlFormTemplate htmlform,TemplateField field,StringBuffer retval) throws Exception{
    	retval.append("<input type=\"hidden\" name=\"value(fi_").append(htmlform.getTableName()).append("__").append(field.getFieldname()).append(")\" value=\"");
    	if(field.getTitle()!=null&&field.getTitle().length()!=0)
			retval.append(field.getTitle()).append(";");
		else
			retval.append(field.getFieldname()).append(";");
    	if("numeric".equals(field.getDatatype())||"int".equals(field.getDatatype())){
    		retval.append("N;");
    	}else if("date".equals(field.getDatatype())||"datetime".equals(field.getDatatype())){
    		retval.append("D;");
    	}else
    		retval.append("C;");
    	String datadict=field.getDatadict();
    	try{
	    	if(datadict!=null&&datadict.length()!=0){
				String mm[]=datadict.split(";");
				datadict=mm[0];
				if(mm.length==1){
					datadict=Utils.escapeMySQLVars(datadict).toString();
					SQL dictsql=new SQL(datadict);
					Vector aa =dictsql.getSelectStatement().getColumns().toVector();
					if(aa.size()>=2){
						String key1=((Column)aa.get(0)).getName();
						String key2=((Column)aa.get(1)).getName();
						String tn=""+dictsql.getSelectStatement().getTables().toVector().get(0);
						if(!key1.equals(key2)){
							retval.append(tn).append(";").append(key1).append(";").append(key2).append(";");
							if(dictsql.getSelectStatement().getWhereClause()!=null)
								retval.append(dictsql.getSelectStatement().getWhereClause().toString()).append(";");
						}
					}
				}
	    	}
    	}catch(Exception ex){}
    	retval.append("\">");
    }
    
    private void writeJspForPrint(HtmlFormTemplate htmlform) throws Exception{
        StringBuffer jsp=buildJspForPrint(htmlform);
        String filename="template"+htmlform.getFormId()+"_print.jsp";
        File jspfile=new File(folder+sep+filename);
        FileOutputStream out=new FileOutputStream(jspfile);
        String header="<%@ page language=\"java\" contentType=\"text/html;charset=GBK\"%>\r\n"+
        				"<%@ taglib uri=\"http://jakarta.apache.org/struts/tags-html\" prefix=\"html\" %>\r\n"+
        				"<jsp:directive.page import=\"com.hiromsoft.hiromform.eventhandler.IInitFormHandler\"/>\r\n"+
        				"<jsp:directive.page import=\"com.hiromsoft.hiromview.action.ShowListViewAction\"/>\r\n"+
        				"<jsp:directive.page import=\"com.hiromsoft.hiromform.action.SearchUserDataFormAction\"/>\r\n"+
        			  "<jsp:useBean id=\"userDataForm\" scope=\"request\" class=\"com.hiromsoft.hiromform.UserDataForm\"/>\r\n"+
        			  "<%\r\n"+
        			  		"\tString path = request.getContextPath();\r\n"+
        			  		"\tString basePath = request.getScheme()+\"://\"+request.getServerName()+\":\"+request.getServerPort()+path+\"/\";\r\n"+
        			  		"\tcom.hiromsoft.hiromform.ViewWriter viewWriter=new com.hiromsoft.hiromform.ViewWriter();\r\n"+
        			  		"\tcom.hiromsoft.hiromform.Utils optionsAction=new com.hiromsoft.hiromform.Utils();\r\n"+
        			  		"\tShowListViewAction showListViewAction=null;\r\n"+
        			  		"\tSearchUserDataFormAction finder=null;\r\n"+
        			  		scripts+"\r\n"+ 
        			  "%>\r\n"+
        			  "<html>\r\n<head>\r\n<title>打印</title>\r\n</head>\r\n"+
        			  "<style>\r\n"+
        				"table{\r\n"+
        				"	border:0.2mm solid black;\r\n"+
        				"}\r\n"+
        				"td{\r\n"+
        				"	font-size:12px;\r\n"+
        				"	padding:2px;\r\n"+
        				"}\r\n"+
        				".cell{\r\n"+
        				"	border-top:none;\r\n"+
        				"	border-left:none;\r\n"+
        				"	border-right:0.2mm solid;\r\n"+
        				"	border-bottom:0.2mm solid;\r\n"+
        				"}\r\n"+
        				".cell2{\r\n"+
        				"	border-top:0.5mm solid;\r\n"+
        				"	border-left:none;\r\n"+
        				"	border-right:0.2mm solid;\r\n"+
        				"	border-bottom:0.2mm solid;\r\n"+
        				"}\r\n"+
        			"</style>\r\n"+
        			"<script>\r\n"+
        			"	function window.onload(){\r\n"+
        			"		var tables=document.getElementsByTagName(\"table\");\r\n"+
        			"		for(var num=0;num<tables.length;num++){\r\n"+
        			"			//alert(tables[num].border);\r\n"+
        			"			if(tables[num].border==\"1\"){\r\n"+
        			"				for(var col=0;col<tables[num].cells.length;col++){\r\n"+
        			"                   var tr=tables[num].cells[col].parentElement;\r\n"+
					"                   var before=tr.style.pageBreakBefore;\r\n"+
					"                   if(before==\"always\"){\r\n"+
					"	                     tables[num].cells[col].className=\"cell2\";\r\n"+
					"                   }else{\r\n"+
					"	                     if(tr.rowIndex>1){\r\n"+
					"		                     var tr=tables[num].rows[tr.rowIndex-1];\r\n"+
					"		                     var after=tr.style.pageBreakAfter;\r\n"+
					"		                     if(after==\"always\"){\r\n"+
					"			                       tables[num].cells[col].className=\"cell2\";\r\n"+
					"		                     }else\r\n"+
					"			                       tables[num].cells[col].className=\"cell\";\r\n"+
					"	                     }else{\r\n"+
					"		                     tables[num].cells[col].className=\"cell\";\r\n"+
					"	                     }\r\n"+
					"                   }\r\n"+
        			"				}\r\n"+
        			"			}\r\n"+
        			"		}\r\n"+
        			"	}\r\n"+
        			"</script>\r\n"+
        			  "<body>\r\n";
        			  
        out.write(header.getBytes("GBK"));
        out.write(this.childformshtml.toString().getBytes("GBK"));
        out.write(jsp.toString().getBytes("GBK"));
        out.write("</body>\r\n</html>".getBytes("GBK"));
        out.close();
    }
    private void writeJspForReadonly(HtmlFormTemplate htmlform) throws Exception{
        StringBuffer jsp=buildJspForPrint(htmlform);
        String filename="template"+htmlform.getFormId()+"_readonly.jsp";
        File jspfile=new File(folder+sep+filename);
        FileOutputStream out=new FileOutputStream(jspfile);
        String header="<%@ page language=\"java\" contentType=\"text/html;charset=GBK\"%>\r\n"+
        				"<%@ taglib uri=\"http://jakarta.apache.org/struts/tags-html\" prefix=\"html\" %>\r\n"+
        				"<jsp:directive.page import=\"com.hiromsoft.hiromform.eventhandler.IInitFormHandler\"/>\r\n"+
        				"<jsp:directive.page import=\"com.hiromsoft.hiromview.action.ShowListViewAction\"/>\r\n"+
        				"<jsp:directive.page import=\"com.hiromsoft.hiromform.action.SearchUserDataFormAction\"/>\r\n"+
        			  "<jsp:useBean id=\"userDataForm\" scope=\"request\" class=\"com.hiromsoft.hiromform.UserDataForm\"/>\r\n"+
        			  "<%\r\n"+
        			  		"\tString path = request.getContextPath();\r\n"+
        			  		"\tString basePath = request.getScheme()+\"://\"+request.getServerName()+\":\"+request.getServerPort()+path+\"/\";\r\n"+
        			  		"\tcom.hiromsoft.hiromform.ViewWriter viewWriter=new com.hiromsoft.hiromform.ViewWriter();\r\n"+
        			  		"\tcom.hiromsoft.hiromform.Utils optionsAction=new com.hiromsoft.hiromform.Utils();\r\n"+
        			  		"\tShowListViewAction showListViewAction=null;\r\n"+
        			  		"\tSearchUserDataFormAction finder=null;\r\n"+
        			  		scripts+"\r\n"+ 
        			  "%>\r\n";
        			  
        out.write(header.getBytes("GBK"));
        out.write(makeHiddenText("userDataForm").getBytes("GBK"));
        out.write(this.childformshtml.toString().getBytes("GBK"));
        out.write(jsp.toString().getBytes("GBK"));
        out.close();
    }
    private String makeHiddenText(String formname){
    	String retval="<html:hidden name=\""+formname+"\" property=\"value(ID"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__url"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__hirompage"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__viewid"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__viewcontext"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__readonly"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__formstate"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__formacl"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__templateId"+this.fieldname_suffix+")\"/>\r\n"+
		  "<html:hidden name=\""+formname+"\" property=\"value(__tablename"+this.fieldname_suffix+")\" value=\""+this.htmlform.getTableName()+"\"/>\r\n"+
    	  "<html:hidden name=\""+formname+"\" property=\"value(__hirom_token"+this.fieldname_suffix+")\"/>\r\n";
    	
    	
    	Vector aa=Utils.getSystemFields();
    	for(int num=0;num<aa.size();num++){
    		TemplateField field=(TemplateField)aa.get(num);
    		retval=retval+"<html:hidden name=\""+formname+"\" property=\"value("+field.getPropname()+this.fieldname_suffix+")\"/>\r\n";
    	}
    	return retval;
    }
    private void writeJspForGridEditor(HtmlFormTemplate htmlform,String formname,String scope,String sufix) throws Exception{
    	
        if(formname==null) formname="userDataForm";
        if(sufix==null) sufix="";
        if(scope==null) scope="session";
        
        StringBuffer jsp=this.buildJspBody(htmlform, formname, true);
        
        String filename="template"+htmlform.getFormId()+sufix+".jsp";
        File jspfile=new File(folder+sep+filename);
        FileOutputStream out=new FileOutputStream(jspfile);
     
        
        String header="<%@ page language=\"java\" contentType=\"text/html;charset=GBK\"%>\r\n"+
        			  "<%@ taglib uri=\"http://jakarta.apache.org/struts/tags-html\" prefix=\"html\" %>\r\n"+
        			  "<jsp:directive.page import=\"com.hiromsoft.hiromform.eventhandler.IInitFormHandler\"/>\r\n"+
        			  "<jsp:useBean id=\""+formname+"\" scope=\""+scope+"\" class=\"com.hiromsoft.hiromform.UserDataForm\"/>\r\n"+
        			  "<%\r\n"+
        			  		"\tString path = request.getContextPath();\r\n"+
        			  		"\tString basePath = request.getScheme()+\"://\"+request.getServerName()+\":\"+request.getServerPort()+path+\"/\";\r\n"+
        			  		"\tcom.hiromsoft.hiromform.ViewWriter viewWriter=new com.hiromsoft.hiromform.ViewWriter();\r\n"+
        			  		"\tcom.hiromsoft.hiromform.Utils optionsAction=new com.hiromsoft.hiromform.Utils();\r\n"+
        			  		"\tString formstate=(String)"+formname+".getValue(\"__formstate\");\r\n"+
        			 scripts+"\r\n"+ 
        			 "%>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(ID"+this.fieldname_suffix+")\"/>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(__entityname"+this.fieldname_suffix+")\" value=\""+htmlform.getEntityName()+"\"/>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(__formid"+this.fieldname_suffix+")\" value=\""+htmlform.getFormId()+"\"/>\r\n";
        
        out.write(header.getBytes("GBK"));
        out.write(jsp.toString().getBytes("GBK"));
        String script=createScript(htmlform);
        out.write(script.getBytes("GBK"));
        out.write(registerFormFieldEditors().getBytes("GBK"));
        out.close();
    }
    private void writeJspForChildForm(HtmlFormTemplate htmlform,String formname,String scope,String sufix) throws Exception{
    	
        if(formname==null) formname="userDataForm";
        if(sufix==null) sufix="";
        if(scope==null) scope="session";
                
        String filename="template"+htmlform.getFormId()+sufix+".jsp";
        File jspfile=new File(folder+sep+filename);
        FileOutputStream out=new FileOutputStream(jspfile);
     
        
        String header="<%@ page language=\"java\" contentType=\"text/html;charset=GBK\"%>\r\n"+
        			  "<%@ taglib uri=\"http://jakarta.apache.org/struts/tags-html\" prefix=\"html\" %>\r\n"+
        			  "<jsp:directive.page import=\"com.hiromsoft.hiromform.eventhandler.IInitFormHandler\"/>\r\n"+
        			  "<jsp:useBean id=\""+formname+"\" scope=\""+scope+"\" class=\"com.hiromsoft.hiromform.UserDataForm\"/>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(ID"+this.fieldname_suffix+")\"/>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(__entityname"+this.fieldname_suffix+")\" value=\""+htmlform.getEntityName()+"\"/>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(__formid"+this.fieldname_suffix+")\" value=\""+htmlform.getFormId()+"\"/>\r\n";
        
        out.write(header.getBytes("GBK"));
        //String script=createScript(htmlform);
        //out.write(script.getBytes("GBK"));
        out.write(registerFormFieldEditors().getBytes("GBK"));
        out.close();
    }
    private String registerFormFieldEditors(){
    	String retval="<script>if(!window.formFieldEditors) window.formFieldEditors=[];\r\nvar tmp_f"+htmlform.getFormId()+"=[];\r\n";
    	Iterator it=htmlform.getFields().iterator();
    	while(it.hasNext())
    	{
    		TemplateField htmltag=(TemplateField)it.next();
    		String propname=TemplateParser.getPropName(htmltag.getDatatype(), htmltag.getFieldname());
    		retval=retval+"tmp_f"+htmlform.getFormId()+"['"+htmltag.getFieldname().toLowerCase()+"']='"+propname+this.fieldname_suffix+"';\r\n";
    	}
    	retval=retval+"window.formFieldEditors['f"+htmlform.getFormId()+"']=tmp_f"+htmlform.getFormId()+";\r\n</script>";
    	return retval;
    	
    }
    private void writeJsp(HtmlFormTemplate htmlform,String formname,String scope,String sufix) throws Exception{
        if(formname==null) formname="userDataForm";
        if(sufix==null) sufix="";
        if(scope==null) scope="session";
        
        StringBuffer jsp=buildJspBody(htmlform,formname);
        
        String filename="template"+htmlform.getFormId()+sufix+".jsp";
        File jspfile=new File(folder+sep+filename);
        FileOutputStream out=new FileOutputStream(jspfile);
     
        
        String header="<%@ page language=\"java\" contentType=\"text/html;charset=GBK\"%>\r\n"+
        			  "<%@ taglib uri=\"http://jakarta.apache.org/struts/tags-html\" prefix=\"html\" %>\r\n"+
        			  "<jsp:directive.page import=\"com.hiromsoft.hiromform.eventhandler.IInitFormHandler\"/>\r\n"+
        			  "<jsp:directive.page import=\"com.hiromsoft.hiromview.action.ShowListViewAction\"/>\r\n"+
        			  "<jsp:directive.page import=\"com.hiromsoft.hiromform.action.SearchUserDataFormAction\"/>\r\n"+
        			  "<jsp:useBean id=\""+formname+"\" scope=\""+scope+"\" class=\"com.hiromsoft.hiromform.UserDataForm\"/>\r\n"+
        			  "<%\r\n"+
        			  		"\tString path = request.getContextPath();\r\n"+
        			  		"\tString basePath = request.getScheme()+\"://\"+request.getServerName()+\":\"+request.getServerPort()+path+\"/\";\r\n"+
        			  		"\tcom.hiromsoft.hiromform.ViewWriter viewWriter=new com.hiromsoft.hiromform.ViewWriter();\r\n"+
        			  		"\tcom.hiromsoft.hiromform.Utils optionsAction=new com.hiromsoft.hiromform.Utils();\r\n"+
        			  		"\tString formstate=(String)"+formname+".getValue(\"__formstate\");\r\n"+
        			  		"\tShowListViewAction showListViewAction=null;\r\n"+
        			  		"\tSearchUserDataFormAction finder=null;\r\n"+
        			 scripts+"\r\n"+ 
        			 "%>\r\n"+
        			  "<html:form styleId=\"F"+htmlform.getFormId()+"\" method=\"post\" action=\"saveExcel\" formTypeProperty=\"value(formtagtype)\" lockProperty=\"value(lock)\">\r\n"+
        			  makeHiddenText(formname)+
        			  "<html:hidden name=\""+formname+"\" property=\"value(__entityname"+this.fieldname_suffix+")\" value=\""+htmlform.getEntityName()+"\"/>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(__formid"+this.fieldname_suffix+")\" value=\""+htmlform.getFormId()+"\"/>\r\n"+
        			  "<html:hidden name=\""+formname+"\" property=\"value(print_template"+this.fieldname_suffix+")\" value=\"template"+htmlform.getFormId()+"_print\"/>\r\n";
        
        out.write(header.getBytes("GBK"));
        /*
        String print="<hr><div align=\"left\"><img src=\"<%=path%>/image/button_dayin.gif\""+
        " onclick=\"window.open('<%=path%>/printShenQingBiao.do?form="+
        htmlform.getEntityName()+"&ID=<%="+formname+".getValue(\"ID\")%>&template="+"template"+
        htmlform.getFormId()+"_print"+"')\"></img></div>";
        out.write(print.getBytes("GBK"));
        */
        out.write(this.childformshtml.toString().getBytes("GBK"));
        out.write(jsp.toString().getBytes("GBK"));
        out.write("</html:form>".getBytes("GBK"));
        String script=createScript(htmlform);
        out.write(script.getBytes("GBK"));
        
        out.close();
    }
    
    
    private String createScript(HtmlFormTemplate htmlform) throws Exception{
        Vector required=new Vector();
        Vector range=new Vector();
        Vector lenth=new Vector();
        Vector number=new Vector();
        Vector dt=new Vector();
        
        Iterator it=htmlform.getFields().iterator();
        while(it.hasNext())
        {
            TemplateField htmltag=(TemplateField)it.next();
            if(htmltag.getValidations()!=null&&htmltag.getValidations().length()!=0)
            {
                String[] valids=htmltag.getValidations().split(",");
                for(int num=0;num<valids.length;num++)
                {
                    if(valids[num].length()!=0){
                        String tmp[]=valids[num].split("=");
                        if(tmp.length!=0)
                        {
                            if(tmp[0].equals("required"))
                                required.add(htmltag);
                            else if (tmp[0].equals("maxlength"))
                                lenth.add(htmltag);
                            else if (tmp[0].equals("numeric"))
                                number.add(htmltag);
                            else if (tmp[0].equals("date"))
                                dt.add(htmltag);
                            else if (tmp[0].equals("range"))
                                range.add(htmltag);
                        }
                    }
                }
            }
        }
        
        String valids="";
        
        String funcrequired="";
        //String funcrange="";
        //String funclenth="";
        String funcnumber="";
        String funcdt="";
        
        Iterator it2=required.iterator();
        int varnum=0;

        while(it2.hasNext())
        {
            varnum++;
            TemplateField htmltag=(TemplateField)it2.next();
            String propname=htmltag.getPropname();
            String prefix="this";
            funcrequired=funcrequired+"\t"+prefix+"."+getVarName(varnum)+
            " = new Array(\"value("+propname+this.fieldname_suffix+
            ")\", \"请输入"+Utils.deleteKuoHao(htmltag.getTitle())+"\", new Function (\"varName\", \" return this[varName];\"));\r\n";
        }
        if(varnum!=0)
        {
        	String funcname="required_f"+htmlform.getFormId();
            valids=valids+"validateRequired(form,new "+funcname+"())&&";
           	funcrequired="\r\nfunction "+funcname+"(){\r\n"+funcrequired+"}\r\n";
        }
        
        
        Iterator it3=dt.iterator();
        varnum=0;
        while(it3.hasNext())
        {
            
            varnum++;
            TemplateField htmltag=(TemplateField)it3.next();
            String propname=htmltag.getPropname();
            String prefix="this";
            funcdt=funcdt+"\t"+prefix+"."+getVarName(varnum)+
            " = new Array(\"value("+propname+this.fieldname_suffix+
            ")\", \""+Utils.deleteKuoHao(htmltag.getTitle())+"的日期格式不正确(格式为:2005-10-01)\", new Function (\"varName\", \"this.datePatternStrict='yyyy-MM-dd'; return this[varName];\"));\r\n";
        }
        if(varnum!=0)
        {
        	String funcname="DateValidations_f"+htmlform.getFormId();
            valids=valids+"validateDate(form,new "+funcname+"())&&";
           	funcdt="\r\nfunction "+funcname+"(){\r\n"+funcdt+"}\r\n";
        }
        
        Iterator it4=number.iterator();
        varnum=0;
        while(it4.hasNext())
        {
            varnum++;
            TemplateField htmltag=(TemplateField)it4.next();
            String propname=htmltag.getPropname();
            String prefix="this";
            funcnumber=funcnumber+"\t"+prefix+"."+getVarName(varnum)+
            " = new Array(\"value("+propname+this.fieldname_suffix+
            ")\", \"请输入有效数字("+Utils.deleteKuoHao(htmltag.getTitle())+")\", new Function (\"varName\", \"this.datePatternStrict='yyyy-MM-dd'; return this[varName];\"));\r\n";
        }
        if(varnum!=0)
        {
        	String funcname="FloatValidations_f"+htmlform.getFormId();	
            valids=valids+"validateFloat(form,new "+funcname+"())&&";
           	funcnumber="\r\nfunction "+funcname+"(){\r\n"+funcnumber+"}\r\n";
        }
        
        
        if(valids.length()!=0)
            valids=valids.substring(0,valids.length()-2);
        else
            valids="true";

        String script="\r\n<script>\r\n"+
        		 "if(!window.validateHiromFormFuncs) window.validateHiromFormFuncs=[];\r\n"+
    			 "validateHiromFormFuncs['f"+htmlform.getFormId()+"']=function(form){\r\n"+
    			 "\treturn "+valids+"\r\n"+
    			 "}\r\n"+funcrequired+"\r\n"+
    			 funcnumber+"\r\n"+
    			 funcdt+"\r\n"+
    			 "</script>\r\n";
        
        
        return script;
        
    }
    
   
    
    
    String getVarName(int val)
    {
        String retval="";
        char aa=(char)(val/26+97);
        char bb= (char)(val % 26+97);
        
        retval=String.valueOf(aa)+String.valueOf(bb);
        return retval;
    }
    private StringBuffer buildJspBody(HtmlFormTemplate htmlform,String formname) throws Exception {
    	return this.buildJspBody(htmlform, formname, false);
    }
    private StringBuffer buildJspBody(HtmlFormTemplate htmlform,String formname,boolean onlyEditor) throws Exception {
        StringBuffer jsp=new StringBuffer("");
        Parser parser=new Parser();
        parser.setEncoding("GBK");
        parser.setInputHTML(htmlform.getContent());
        TagNameFilter filter=new TagNameFilter("body");
        NodeList list=parser.parse(filter);
        if(list.size()!=0)
        {
            Node node=list.elementAt(0);
            Tag body=(Tag)node;
            NodeList list1=body.getChildren();
            for(int num=0;num<list1.size();num++)
            {
                Node nd=list1.elementAt(num);
                if(nd instanceof Tag)
                {
                    internalBuildJsp((Tag)nd,jsp,formname,onlyEditor);
                }else
                {
                    if(onlyEditor) continue;
                	String sss=nd.toHtml();
                    jsp.append(sss);
                }
            }
        }
        return jsp;
    }
    
    void internalBuildJsp(Tag tag,StringBuffer jsp,String formname,boolean onlyEditor) throws Exception 
    {
        	
            String tagName=tag.getTagName().toLowerCase();
            if(tagName.equals("input"))
	        {
	            jsp.append(buildInputField(tag,formname));
	        }else if(tagName.equals("select"))
	        {
	            jsp.append(buildSelectField(tag,formname));
	        }else if(tagName.equals("textarea"))
	        {
	            jsp.append(buildTextareaField(tag,formname));
	        }else if(tagName.equals("table"))
	        {
	        	String hiromviewid=tag.getAttribute("hiromviewid");
	        	if(hiromviewid==null||hiromviewid.length()==0){
	        		jsp.append(buildOtherTag(tag,formname,onlyEditor));
	        	}else{
	        		jsp.append(buildHiromview(tag,hiromviewid,false));
	        	}
	        }else if(tagName.equals("div"))
	        {
	        	String childformid=tag.getAttribute("childformid");
	        	if(childformid==null||childformid.length()==0){
	        		jsp.append(buildOtherTag(tag,formname,onlyEditor));
	        	}else{
	        		buildChildForm(tag,childformid);
	        	}
	        }else
	        {
	            jsp.append(buildOtherTag(tag,formname,onlyEditor));
	        }
      
    }
    private StringBuffer buildChildForm(Tag tag,String childformid) throws Exception{
    	
    	String viewid=tag.getAttribute("searchviewid");
    	if(viewid==null||viewid.length()==0) return childformshtml;
    	childformshtml.append("<").append(tag.getText()).append(">");
    	String url="/jsp/hiromform/_genareted/template"+childformid+"_childform.jsp";
    	
    	childformshtml.append("<%if(finder==null) finder=new SearchUserDataFormAction();\r\n");
    	childformshtml.append("userDataForm.setValue(\"ID_f"+childformid+"\",\"0\");\r\n");
    	childformshtml.append("userDataForm.setValue(\"__viewid\",\""+viewid+"\");\r\n");
    	childformshtml.append("userDataForm.setValue(\"__ischildform\",\"1\");\r\n");
    	//childformshtml.append("request.getParameterMap().clear();\r\n");
    	childformshtml.append("finder.doFindAction(null,userDataForm,request,response,\""+viewid+"\".split(\",\"),false,false,true);%>\r\n");
    	childformshtml.append("<jsp:include flush=\"true\" page=\""+url+"\"></jsp:include>\r\n");
    	childformshtml.append("<%userDataForm.setValue(\"__ischildform\",null);%>");
    	childformshtml.append("</div>");
    	return childformshtml;
    }
    private StringBuffer buildHiromview(Tag tag,String hiromviewid,boolean readonly) throws Exception{
    	StringBuffer jsp=new StringBuffer("");
    	String pagename=tag.getAttribute("hiromviewpagename");
    	if(pagename==null||pagename.length()==0) pagename="/jsp/hiromview/_system/list_childlist.jsp";
    	
    	jsp.append("<%if(showListViewAction==null) showListViewAction=new ShowListViewAction();");
    	jsp.append("userDataForm.setValue(\"id\",\""+hiromviewid+"\");");
    	jsp.append("userDataForm.setValue(\"pagesize\",\"300\");");
    	if(readonly)
    		jsp.append("request.setAttribute(\""+Global.HIROM_READONLY+"\",\"1\");");
    	jsp.append("request.getParameterMap().clear();");
    	jsp.append("showListViewAction.doSearchAction(null,userDataForm,request,response);%>\r\n");
    	jsp.append("<jsp:include flush=\"true\" page=\""+pagename+"\"></jsp:include>\r\n");
    	return jsp;
    }
    private StringBuffer buildOtherTag(Tag tag,String formname,boolean onlyEditor) throws Exception{
    	StringBuffer jsp=new StringBuffer("");
    	if(!onlyEditor)
    		jsp.append("<"+tag.getText()+">");
        NodeList list=tag.getChildren();
        if(list!=null){
            if(list.size()!=0)
            {
                
                NodeList list1=list;
                for(int num=0;num<list1.size();num++)
                {
                    Node nd=list1.elementAt(num);
                    if(nd instanceof Tag)
                    {
                        internalBuildJsp((Tag)nd,jsp,formname,onlyEditor);
                    }else
                    {
                        if(onlyEditor) continue;
                    	jsp.append(nd.toHtml());
                    }
                }
            }
        }
        if(tag.getEndTag()!=null){
        	if(!onlyEditor)
        		jsp.append("<"+tag.getEndTag().getText()+">");
        }
        
        return jsp;
    }
    private StringBuffer buildOtherTag(Tag tag,String formname,String id,boolean onlyEditor) throws Exception{
    	StringBuffer jsp=new StringBuffer("");
    	if(!onlyEditor)
    		jsp.append("<"+tag.getText()+" id=\""+id+"\">");
        NodeList list=tag.getChildren();
        if(list!=null){
            if(list.size()!=0)
            {
                
                NodeList list1=list;
                for(int num=0;num<list1.size();num++)
                {
                    Node nd=list1.elementAt(num);
                    if(nd instanceof Tag)
                    {
                        internalBuildJsp((Tag)nd,jsp,formname,onlyEditor);
                    }else
                    {
                    	if(onlyEditor) continue;
                        jsp.append(nd.toHtml());
                    }
                }
            }
        }
        if(tag.getEndTag()!=null){
        	if(!onlyEditor)
        		jsp.append("<"+tag.getEndTag().getText()+">");
        }
        
        return jsp;
    }
    private StringBuffer buildJspForPrint(HtmlFormTemplate htmlform) throws Exception{
        StringBuffer jsp=new StringBuffer("");
        Parser parser=new Parser();
        parser.setEncoding("GBK");
        parser.setInputHTML(htmlform.getContent());
        TagNameFilter filter=new TagNameFilter("body");
        NodeList list=parser.parse(filter);
        if(list.size()!=0)
        {
            Node node=list.elementAt(0);
            Tag body=(Tag)node;
            NodeList list1=body.getChildren();
            for(int num=0;num<list1.size();num++)
            {
                Node nd=list1.elementAt(num);
                if(nd instanceof Tag)
                {
                    internalBuildJspForPrint((Tag)nd,jsp);
                }else
                {
                    String sss=nd.toHtml();
                    jsp.append(sss);
                }
            }
        }
        return jsp;
    }
    
    void internalBuildJspForPrint(Tag tag,StringBuffer jsp) throws Exception 
    {
        	
            String tagName=tag.getTagName().toLowerCase();
            String type=tag.getAttribute("type");
            if(type==null) type="";
            String title=tag.getAttribute("fieldtitle");
            if(title==null) title="";
            
            if((tagName.equals("input")&&!type.equalsIgnoreCase("checkbox"))||tagName.equals("textarea")||tagName.equals("select"))
	        {
                String fieldtype=tag.getAttribute("fieldtype");
                String propname=tag.getAttribute("fieldname");
                String style=tag.getAttribute("style");
                if(style==null) style="";
                if(fieldtype!=null)
                {
                	propname=TemplateParser.getPropName(fieldtype, propname);
                }
                if(tagName.equals("select")){
                	jsp.append("<% viewWriter.writeTextForSelect(out,userDataForm,\""+propname+this.fieldname_suffix+"\",request,\""+title+"\","+this.htmlform.getFormId()+",\"content-readonly\",\""+style+"\"); %>");
                }else
                	jsp.append("<% viewWriter.writeText(out,userDataForm,\""+propname+this.fieldname_suffix+"\",\""+title+"\","+this.htmlform.getFormId()+",\"content-readonly\",\""+style+"\"); %>");
	        }else{
	            if((tagName.equals("input")&&type.equalsIgnoreCase("checkbox"))){
	            	String propname=tag.getAttribute("fieldname");
	            	if(title==null||title.length()==0) title=propname;
	            	String val=tag.getAttribute("value");
	            	
	            	if(isMultibox( tag, propname)){
	            		jsp.append("<% viewWriter.writeCheckbox(out,userDataForm,\""+propname+this.fieldname_suffix+"_"+val+"\",\""+title+"\","+this.htmlform.getFormId()+"); %>");
	            		jsp.append("<html:multibox "+getAdditionAttr(tag)+" name=\"userDataForm\" value=\""+val+"\" property=\"arrayValue("+propname+this.fieldname_suffix+")\"/>");
	            		
	            	}else{
	            		jsp.append("<% viewWriter.writeCheckbox(out,userDataForm,\""+propname+this.fieldname_suffix+"_"+val+"\",\""+title+"\","+this.htmlform.getFormId()+"); %>");
	            		jsp.append("<html:checkbox "+getAdditionAttr(tag)+" name=\"userDataForm\" value=\""+val+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
	            		
	            	}
	            }else{
	            	if(tagName.equals("table")){
	            		String hiromviewid=tag.getAttribute("hiromviewid");
	    	        	if(hiromviewid==null||hiromviewid.length()==0){
	    	        		buildOthersForPrint(tag,jsp);
	    	        	}else{
	    	        		jsp.append(this.buildHiromview(tag, hiromviewid, true));
	    	        	}	
	            	}else{
	            		buildOthersForPrint(tag,jsp);
	            	}
	            }
	        }
      
    }
    private void buildOthersForPrint(Tag tag,StringBuffer jsp) throws Exception{
    	 jsp.append("<"+tag.getText()+">");
         NodeList list=tag.getChildren();
         if(list!=null){
	            if(list.size()!=0)
	            {
	                NodeList list1=list;
                 for(int num=0;num<list1.size();num++)
                 {
                     Node nd=list1.elementAt(num);
                     if(nd instanceof Tag)
                     {
                         internalBuildJspForPrint((Tag)nd,jsp);
                     }else
                     {
                         jsp.append(nd.toHtml());
                     }
                 }
	            }
         }
         if(tag.getEndTag()!=null)
             jsp.append("<"+tag.getEndTag().getText()+">");
    }
    private String getFormStateStr(String code,int type){
    	//type=0 get editable state
    	//type=1 get readonly state
    	//type=2 get disvisable state
    	String retval="";
    	String aaa[]=code.split(";");
    	if(type==1){
    		retval="";
    		for(int num=0;num<aaa.length;num++){
    			
    			if(aaa[num].length()>=2&&aaa[num].charAt(0)=='1'){
    				retval=retval+"\""+(num+1)+"\".equals(formstate)||";
    			}
    		}
    		if(retval.length()!=0) {
    			retval=retval.substring(0,retval.length()-2);
    			retval="if(formstate!=null&&("+retval+")){";
    		}
    	}else if(type==2){
    		retval="";
    		for(int num=0;num<aaa.length;num++){
    			
    			if(aaa[num].length()>=2&&aaa[num].charAt(1)=='1'){
    				retval=retval+"\""+(num+1)+"\".equals(formstate)||";
    			}
    		}
    		if(retval.length()!=0) {
    			retval=retval.substring(0,retval.length()-2);
    			retval="if(formstate!=null&&("+retval+")){";
    		}
    	} 
    	
    	return retval;
    }
    
    StringBuffer buildInputField(Tag tag,String formname) throws Exception
    {
        StringBuffer retval=new StringBuffer("");
        
        String type=tag.getAttribute("type");
        String formstate=tag.getAttribute("formstate");
        if(type!=null)
        {
            type=type.toLowerCase().trim();
            if(type.equals("text")||type.equals("textbox"))
            {
                String fieldtype=tag.getAttribute("fieldtype");
                String propname=tag.getAttribute("fieldname");
               
                if(fieldtype!=null)
                {
                    propname=TemplateParser.getPropName(fieldtype, propname);
                }
                dealWithDefaultValueAttribute(tag,formname,propname,scripts);
                this.dealWithFormSetting(tag, formname, propname, scripts);
                
                String postBack=(String)tag.getAttribute("postBack");                
                if(postBack!=null&&postBack.equalsIgnoreCase("true"))
                	postBack="onkeydown=\"javascript:postback()\" ";
                else postBack=" ";
                
                String att=getAdditionAttr(tag);
                boolean has1=false;
        		boolean has2=false;
                if(formstate!=null&&formstate.length()!=0){
                	String tmp=this.getFormStateStr(formstate, 1);
                	String tmp2=this.getFormStateStr(formstate, 2);
                	if(tmp.length()!=0||tmp2.length()!=0){
                		
                		//如果设置了不同状态的条件，则产生相关代码
                		if(tmp.length()!=0){
                			//只读的处理
                			retval.append("<%"+tmp+"%>\r\n");
                			//只读代码，与可编辑状态就差一点,注意getAdditionAttr
                			if(fieldtype!=null&&fieldtype.equals("date")&&att.indexOf("readonly=\"true\"")<0)
        	                	retval.append("<html:text "+postBack+getAdditionAttr(tag,true)+" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        	                else {
        	                	retval.append("<html:text "+postBack+getAdditionAttr(tag,true)+" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        	                }
                			//只读代码块结束
                			retval.append("<%}else{%>\r\n");
                			has1=true;
                		}
                		if(tmp2.length()!=0){
//                			不可见的处理,
                			retval.append("<%"+tmp2+"%>\r\n");
                			if(fieldtype!=null&&fieldtype.equals("date")&&att.indexOf("readonly=\"true\"")<0)
        	                	retval.append("<html:text "+postBack+getAdditionAttr(tag,true,false,true)+" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        	                else {
        	                	retval.append("<html:text "+postBack+getAdditionAttr(tag,true,false,true)+" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        	                }
                			retval.append("<%}else{%>\r\n");
                			has2=true;
                		}
                	}
                }
                	
	        	////编辑状态的代码
	            if(fieldtype!=null&&fieldtype.equals("date")&&att.indexOf("readonly=\"true\"")<0)
	            	retval.append("<html:text "+postBack+getAdditionAttr(tag)+" onclick=\"try{return showCalendar(this.name, '%Y-%m-%d', '24');}catch(e){}\" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
	            else {
	            	retval.append("<html:text "+postBack+getAdditionAttr(tag)+" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
	            	String dataSource=tag.getAttribute("sqlDataSource");
	            	if(dataSource!=null&&dataSource.length()!=0){
	            		retval.append("<%com.hiromsoft.hiromform.DropListUtil.GenareteDropList(request,out,\"value("+propname+this.fieldname_suffix+")\",\""+HtmlUtil.HTMLDecode(dataSource)+"\",\""+this.fieldname_suffix+"\");%>");
	            	}
	            }
	            //编辑状态的代码结束
	            if(has1&&has2){
	            	retval.append("<%}}%>\r\n");
	            }else{
	            	if(has1||has2){
	            		retval.append("<%}%>\r\n");
	            	}
	            }
              
                
            }else if(type.equals("password")){
            	
            	boolean has1=false;
        		boolean has2=false;
                if(formstate!=null&&formstate.length()!=0){
                	String tmp=this.getFormStateStr(formstate, 1);
                	String tmp2=this.getFormStateStr(formstate, 2);
                	if(tmp.length()!=0||tmp2.length()!=0){
                		
                		//如果设置了不同状态的条件，则产生相关代码
                		if(tmp.length()!=0){
                			//只读的处理
                			retval.append("<%"+tmp+"%>\r\n");
                			//只读代码，与可编辑状态就差一点,注意getAdditionAttr
                       		retval.append("<html:password "+getAdditionAttr(tag,true)+" name=\""+formname+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\"/>");
                			//只读代码块结束
                			retval.append("<%}else{%>\r\n");
                			has1=true;
                		}
                		if(tmp2.length()!=0){
//                			不可见的处理,
                			retval.append("<%"+tmp2+"%>\r\n");
                       		retval.append("<html:password "+getAdditionAttr(tag,true,false,true)+" name=\""+formname+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\"/>");
                			retval.append("<%}else{%>\r\n");
                			has2=true;
                		}
                	}
                }
            	//正常编辑状态
           		retval.append("<html:password "+getAdditionAttr(tag)+" name=\""+formname+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\"/>");
            	
//            	编辑状态的代码结束
	            if(has1&&has2){
	            	retval.append("<%}}%>\r\n");
	            }else{
	            	if(has1||has2){
	            		retval.append("<%}%>\r\n");
	            	}
	            }
            	
        	}else if(type.equals("checkbox")){
        		String propname=tag.getAttribute("fieldname");
        		dealWithDefaultValueAttribute(tag,formname,scripts);
        		this.dealWithFormSetting(tag, formname, propname, scripts);
        		String onlyone=tag.getAttribute("onlyone");
        		if(onlyone!=null&&onlyone.equals("1")){
        			onlyone=" onclick=\"javascript:setOnlyOne(this)\" ";
        		}else{
        			onlyone="";
        		}
        		

        		boolean has2=false;
                if(formstate!=null&&formstate.length()!=0){
                	String tmp2=this.getFormStateStr(formstate, 2);
                	if(tmp2.length()!=0){
//                			不可见的处理,
            			retval.append("<%"+tmp2+"%>\r\n");
//            			\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
                		if(isMultibox( tag, propname)){
                			retval.append("<html:multibox "+onlyone+" style=\"width:20px;height:20px;\" "+getAdditionAttr(tag,false,false,false)+" name=\""+formname+"\" value=\""+tag.getAttribute("value")+"\" property=\"arrayValue("+propname+this.fieldname_suffix+")\"/>");
                		}else{ 
                			retval.append("<html:checkbox "+onlyone+" style=\"width:20px;height:20px;\" "+getAdditionAttr(tag,false,false,false)+" name=\""+formname+"\" value=\""+tag.getAttribute("value")+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
                		}
                		/////////////////////////////
            			retval.append("<%}else{%>\r\n");
            			has2=true;
                	}
                }
        		//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        		if(isMultibox( tag, propname)){
        			retval.append("<html:multibox "+onlyone+" style=\"width:20px;height:20px;\" "+getAdditionAttr(tag)+" name=\""+formname+"\" value=\""+tag.getAttribute("value")+"\" property=\"arrayValue("+propname+this.fieldname_suffix+")\"/>");
        		}else{ 
        			retval.append("<html:checkbox "+onlyone+" style=\"width:20px;height:20px;\" "+getAdditionAttr(tag)+" name=\""+formname+"\" value=\""+tag.getAttribute("value")+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        		}
        		/////////////////////////////
        		if(has2){
            		retval.append("<%}%>\r\n");
            	}
        		
        	}else if(type.equals("radio")){
        		dealWithDefaultValueAttribute(tag,formname,scripts);
        		this.dealWithFormSetting(tag, formname, tag.getAttribute("fieldname"), scripts);
        		boolean has2=false;
                if(formstate!=null&&formstate.length()!=0){
                	String tmp2=this.getFormStateStr(formstate, 2);
                	if(tmp2.length()!=0){
//                			不可见的处理,
            			retval.append("<%"+tmp2+"%>\r\n");
//            			\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
               			retval.append("<html:radio style=\"width:20px;height:20px;\" "+getAdditionAttr(tag,false,false,false)+" name=\""+formname+"\" value=\""+tag.getAttribute("value")+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\"/>");
                		/////////////////////////////
            			retval.append("<%}else{%>\r\n");
            			has2=true;
                	}
                }
        		//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
       			retval.append("<html:radio style=\"width:20px;height:20px;\" "+getAdditionAttr(tag)+" name=\""+formname+"\" value=\""+tag.getAttribute("value")+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\"/>");
        		/////////////////////////////////////////////////////////////
        		if(has2){
            		retval.append("<%}%>\r\n");
            	}
        	}else if(type.equals("image")){
        		String isQianzheng=tag.getAttribute("qianZheng");
        		if(isQianzheng!=null&&isQianzheng.equals("1"))
        		{
        			String fieldname=tag.getAttribute("fieldname");
        			retval.append(buildOtherTag(tag,formname,fieldname,false));
        			String bindFields=tag.getAttribute("bindFields");
        			if(bindFields!=null&&bindFields.length()!=0){
        				String field1=bindFields.split(",")[0];
        				/*retval.append(
        						"<script>" +
        						"    var zhangdata_"+fieldname+"=document.getElementsByName(\"value("+field1+")\")[0];"+
        						"    if(zhangdata_"+fieldname+".value!=\"\"){"+
        						"    	var zhang_"+fieldname+"=document.getElementById(\""+fieldname+"\");"+
        						"    	var tmp=zhang_"+fieldname+".src;"+
        						"    	var idx=tmp.lastIndexOf(\"/\");"+
        						"    	tmp=tmp.substring(0,idx);"+
        						"    	zhang_"+fieldname+".src=tmp+\"/\"+zhangdata_"+fieldname+".value;"+
        						"    	zhang_"+fieldname+".style.display=\"\";"+
        						"    	zhang_"+fieldname+".style.width=\"150px\";"+
        						"    	zhang_"+fieldname+".style.height=\"150px\";"+
        						"       var left=zhangdata_"+fieldname+".offsetLeft+zhangdata_"+fieldname+".clientWidth/2;"+
        						"       var top=zhangdata_"+fieldname+".offsetTop+zhangdata_"+fieldname+".clientHeight/2;"+
        						"    	zhang_"+fieldname+".runtimeStyle.left=left-75;"+
        						"    	zhang_"+fieldname+".runtimeStyle.top=top-75;"+
        							     						"   }"+
        						"</script>");*/
        				retval.append("<script>setDataForSeal(\""+fieldname+"\",\""+field1+"\");</script>");
        			}
        		}else{
        			retval.append(buildOtherTag(tag,formname,false));
        		}
        	}else{
        		retval.append(buildOtherTag(tag,formname,false));
        	}
        }
        return retval;
    }
    
    StringBuffer buildSelectField(Tag tag,String formname)
    {
        StringBuffer retval=new StringBuffer();
        dealWithDefaultValueAttribute(tag,formname,scripts);
        dealWithDataSourceAttribute(tag,formname,scripts);
        this.dealWithFormSetting(tag, formname, tag.getAttribute("fieldname"), scripts);
        String postBack=(String)tag.getAttribute("postBack");
        String formstate=tag.getAttribute("formstate");
        
        boolean has1=false;
		boolean has2=false;
        if(formstate!=null&&formstate.length()!=0){
        	String tmp=this.getFormStateStr(formstate, 1);
        	String tmp2=this.getFormStateStr(formstate, 2);
        	if(tmp.length()!=0||tmp2.length()!=0){
        		
        		//如果设置了不同状态的条件，则产生相关代码
        		if(tmp.length()!=0){
        			//只读的处理
        			retval.append("<%"+tmp+"%>\r\n");
        			//只读代码，
               		retval.append("<html:hidden name=\""+formname+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\"/>");
        			retval.append("<% viewWriter.writeTextForSelect(out,userDataForm,\""+tag.getAttribute("fieldname")+this.fieldname_suffix+"\",request,\""+tag.getAttribute("fieldname")+this.fieldname_suffix+"\","+this.htmlform.getFormId()+",\"form-input-readonly\"); %>");
        			//只读代码块结束
        			retval.append("<%}else{%>\r\n");
        			has1=true;
        		}
        		if(tmp2.length()!=0){
//        			不可见的处理,
        			retval.append("<%"+tmp2+"%>\r\n");
               		retval.append("<html:hidden name=\""+formname+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\"/>");
        			retval.append("<%}else{%>\r\n");
        			has2=true;
        		}
        	}
        }
        
        //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        this.internalBuildSelect(retval, tag, formname, postBack);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if(has1&&has2){
        	retval.append("<%}}%>\r\n");
        }else{
        	if(has1||has2){
        		retval.append("<%}%>\r\n");
        	}
        }
        
        return retval;
    } 
    private void internalBuildSelect(StringBuffer retval,Tag tag,String formname,String postBack){
    	if(postBack!=null&&postBack.equalsIgnoreCase("true")){
        	postBack="onchange=\"javascript:postback()\"";
        	String tmp=(String)tag.getAttribute("onchange");
        	if(tmp!=null&&tmp.length()!=0)
        	{
        		postBack="";
        	}
       		retval.append("<html:select "+postBack+" " +getAdditionAttr(tag,false,true,true)+ "name=\""+formname+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\">");
        }else{
       		retval.append("<html:select " +getAdditionAttr(tag,false,true,true)+ "name=\""+formname+"\" property=\"value("+tag.getAttribute("fieldname")+this.fieldname_suffix+")\">");
        }
        	
        
        NodeList list=tag.getChildren();
        if(list!=null){
	        if(list.size()!=0)
	        {
	        	if("1".equals(tag.getAttribute("showemptyoption"))) {
	        		retval.append("<option value=\"\"></option>");
	        	}
	            for(int num=0;num<list.size();num++)
	            {
	                Node aa=list.elementAt(num);
	                if(aa instanceof Tag)
	                {
	                    Tag bb=(Tag)aa;
	                    if(bb.getTagName().toLowerCase().equals("option"))
	                    {
	                        retval.append("<html:option value=\""+bb.getAttribute("value")+"\" >");
	                        NodeList list2=bb.getChildren();
	                        if(list2!=null){
	                            for(int num1=0;num1<list2.size();num1++){
	                                retval.append(list2.elementAt(num1).toPlainTextString());
	                            }
	                        }
	                        retval.append("</html:option>");
	                    }
	                }
	            }
	        }else{
	        	if("1".equals(tag.getAttribute("showemptyoption"))) {
	        		retval.append("<option value=\"\"></option>");
	        	}
	        	retval.append("<html:options name=\""+tag.getAttribute("fieldname")+this.fieldname_suffix+"values\" labelName=\""+tag.getAttribute("fieldname")+this.fieldname_suffix+"labels\"/>");
	        }
        }else{
        	if("1".equals(tag.getAttribute("showemptyoption"))) {
        		retval.append("<option value=\"\"></option>");
        	}
        	retval.append("<html:options name=\""+tag.getAttribute("fieldname")+this.fieldname_suffix+"values\" labelName=\""+tag.getAttribute("fieldname")+this.fieldname_suffix+"labels\"/>");
        }
        retval.append("</html:select>");
    }
    
    StringBuffer buildTextareaField(Tag tag,String formname)
    {
        StringBuffer retval=new StringBuffer("");
        String rows=tag.getAttribute("rows");
        String cols=tag.getAttribute("cols");
        dealWithDefaultValueAttribute(tag,formname,scripts);
        if(rows==null) rows="5";
        if(cols==null) cols="100";
        String propname=tag.getAttribute("fieldname");
        
        String formstate=tag.getAttribute("formstate");
        boolean has1=false;
		boolean has2=false;
        if(formstate!=null&&formstate.length()!=0){
        	String tmp=this.getFormStateStr(formstate, 1);
        	String tmp2=this.getFormStateStr(formstate, 2);
        	if(tmp.length()!=0||tmp2.length()!=0){
        		
        		//如果设置了不同状态的条件，则产生相关代码
        		if(tmp.length()!=0){
        			//只读的处理
        			retval.append("<%"+tmp+"%>\r\n");
        			//只读代码，
        			retval.append("<html:textarea "+getAdditionAttr(tag,true)+" rows=\""+rows+"\" cols=\""+cols+"\" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        			//只读代码块结束
        			retval.append("<%}else{%>\r\n");
        			has1=true;
        		}
        		if(tmp2.length()!=0){
//        			不可见的处理,
        			retval.append("<%"+tmp2+"%>\r\n");
        			retval.append("<html:textarea "+getAdditionAttr(tag,true,false,true)+" rows=\""+rows+"\" cols=\""+cols+"\" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        			retval.append("<%}else{%>\r\n");
        			has2=true;
        		}
        	}
        }
        //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        retval.append("<html:textarea "+getAdditionAttr(tag)+" rows=\""+rows+"\" cols=\""+cols+"\" name=\""+formname+"\" property=\"value("+propname+this.fieldname_suffix+")\"/>");
        //////////////////////////////////////////////
        if(has1&&has2){
        	retval.append("<%}}%>\r\n");
        }else{
        	if(has1||has2){
        		retval.append("<%}%>\r\n");
        	}
        }
        
        return retval;
    }
    private void dealWithDefaultValueAttribute(Tag tag,String formname,StringBuffer scripts){
    	dealWithDefaultValueAttribute(tag,formname,null,scripts);
    }
    private void dealWithDefaultValueAttribute(Tag tag,String formname,String propname,StringBuffer scripts){
    	String fieldname=propname;
    	if(fieldname==null)
    		fieldname=tag.getAttribute("fieldname");
 
    	String autoincrease=tag.getAttribute("autoincrease");
        if(autoincrease!=null&&autoincrease.equals("1")){
        	scripts.append("\tif("+formname+".getValue(\""+fieldname+"\")==null||"+formname+".getValue(\""+fieldname+"\").toString().length()==0){\r\n");
        	scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",Integer.toString(optionsAction.getSequenceValue(\"seq_f"+this.htmlform.getFormId()+"_"+fieldname+"\")));\r\n");
        	scripts.append("\t}\r\n");
        	return;
        }
    	String defaultValue=tag.getAttribute("dv");
    	if(defaultValue==null||defaultValue.length()==0)
    		defaultValue=tag.getAttribute("dv2");
    	
    	defaultValue=HtmlUtil.HTMLDecode(defaultValue);
    	
    	fieldname=fieldname+this.fieldname_suffix;
    	
        if(defaultValue!=null){
        	boolean hasExpr=false;
        	scripts.append("\ttry{if("+formname+".getValue(\""+fieldname+"\")==null||"+formname+".getValue(\""+fieldname+"\").toString().length()==0){\r\n");
        	if((defaultValue.startsWith("{")&&defaultValue.endsWith("}"))){
        		createDefaultScript(formname,fieldname,scripts,"","",defaultValue.substring(1, defaultValue.length()-1));
        	}else if((defaultValue.startsWith("'")&&defaultValue.endsWith("'"))||
        			(defaultValue.startsWith("\"")&&defaultValue.endsWith("\""))){
        		scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",\""+defaultValue.substring(1, defaultValue.length()-1)+"\");\r\n");
        		hasExpr=true;
        	}else if(defaultValue.equals("sysdate")){
        		createDefaultScript(formname,fieldname,scripts,"","",defaultValue);
        	}else{
        		if(Pattern.matches("\\$\\{[^\\s&&[^\\$\\{\\}]]+\\}", defaultValue)){
        			String prop=defaultValue.substring(2,defaultValue.length()-1);
        			createDefaultScript(formname,fieldname,scripts,"","",prop);
        		}else if (Pattern.matches("^[^\\.]*\\$\\{[^\\s&&[^\\$\\{\\}]].+\\}[^\\.]*$", defaultValue)){
        			int idx=defaultValue.indexOf("${");
        			String str1="";
        			String str2="";
        			if(idx!=0)
        				str1=defaultValue.substring(0,idx);
        			int idx2=defaultValue.indexOf("}");
        			if(idx2!=defaultValue.length()-1)
        				str2=defaultValue.substring(idx2+1);
        			String prop=defaultValue.substring(idx+2,idx2);
        			createDefaultScript(formname,fieldname,scripts,str1,str2,prop);
        		}else
        			scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",\""+defaultValue+"\");\r\n");
        	}
        	scripts.append("\t}}catch(Exception ex){ex.printStackTrace();}\r\n");
        	if(hasExpr)
        		scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormDataTypeTranslator(\""+this.htmlform.getFormId()+"\",\""+tag.getAttribute("fieldname")+"\",\""+defaultValue.substring(1, defaultValue.length()-1)+"\",com.hiromsoft.types.translator.ContextTranslator.getInstance(),1);\r\n");
        }
        defaultValue=tag.getAttribute("dv3");
        if(defaultValue!=null&&defaultValue.length()!=0)
        	scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormDataTypeTranslator(\""+this.htmlform.getFormId()+"\",\""+tag.getAttribute("fieldname")+"\",\""+HtmlUtil.HTMLDecode(defaultValue)+"\",com.hiromsoft.types.translator.NullContextTranslator.getInstance(),2);\r\n");
    }
    private void createDefaultScript(String formname,String fieldname,StringBuffer scripts,
    		String prefix,String suffix,String propName){
    	if(propName.equals("sysdate")){
    		scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",new java.util.Date());\r\n");
    	}else if(propName.equals("GUID")){
    		scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",com.hiromsoft.hiromform.Utils.GenerateUUID());\r\n");
    	}else if(propName.startsWith("user.")){
    		scripts.append("\t\t"+"com.hiromsoft.web.modal.User user=(com.hiromsoft.web.modal.User)request.getSession().getAttribute(com.hiromsoft.utils.Global.HIROM_USER);\r\n");
    		scripts.append("\t\t"+"if(user!=null)\r\n");
    		scripts.append("\t\t\t"+formname+".setValue(\""+fieldname+"\",org.apache.commons.beanutils.PropertyUtils.getProperty(user,\""+propName.substring(5)+"\"));\r\n");
    	}else if(propName.startsWith("request.")){	
    		scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",request.getAttribute(\""+propName.substring(8)+"\"));\r\n");	
    	}else if(propName.startsWith("session.")){	
    		scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",session.getAttribute(\""+propName.substring(8)+"\"));\r\n");	
    	}else{
    		String props[]=propName.split("\\.");
    		if(props.length==1){
    			scripts.append("\t\t"+formname+".setValue(\""+fieldname+"\",\""+prefix+"\"+(String)"+formname+".getValue(\""+props[0]+"\")+\""+suffix+"\");\r\n");
    		}else{
    			if(props[0].equals("eventHandler")){
    				String tmp="";
    				for(int num=1;num<props.length;num++){
    					tmp=tmp+props[num];
    					if(num!=props.length-1)
    						tmp=tmp+".";
    				}
    				scripts.append("\t\t"+"String className_"+fieldname+"=\"com.hiromsoft.hiromform.eventhandler."+tmp+"\";\r\n");
    				scripts.append("\t\ttry{\r\n"+
    				"\t\t\tIInitFormHandler handler_"+fieldname+"=(IInitFormHandler)( Class.forName(className_"+fieldname+").newInstance());\r\n"+
    				"\t\t\thandler_"+fieldname+".execute(userDataForm, request, response);\r\n"+
    				"\t\t}catch(Exception ex){}\r\n");	
    			}else{
    				String tmp= "\t\tString tmp_"+fieldname+"=\""+propName+"\";\r\n"+
    							"\t\tString props_"+fieldname+"[]=tmp_"+fieldname+".split(\"\\\\.\");\r\n"+
    							"\t\tObject value_"+fieldname+"=userDataForm;\r\n"+
    							"\t\tfor(int num_dv=0;num_dv<props_"+fieldname+".length;num_dv++){\r\n"+
    								"\t\t\tif(value_"+fieldname+" instanceof com.hiromsoft.hiromform.UserDataForm)\r\n"+
    									"\t\t\t\tvalue_"+fieldname+"=((com.hiromsoft.hiromform.UserDataForm)value_"+fieldname+").getValue(props_"+fieldname+"[num_dv]);\r\n"+
    								"\t\t\telse if(value_"+fieldname+" instanceof java.util.Map)\r\n"+
    									"\t\t\t\tvalue_"+fieldname+"=((java.util.Map)value_"+fieldname+").get(props_"+fieldname+"[num_dv]);\r\n"+
    								"\t\t\telse\r\n"+
    									"\t\t\t\tvalue_"+fieldname+"=org.apache.commons.beanutils.PropertyUtils.getProperty(value_"+fieldname+", props_"+fieldname+"[num_dv]);\r\n"+
    							"\t\t}\r\n";
    				scripts.append(tmp);
    				scripts.append("\t\tif(value_"+fieldname+" instanceof String)\r\n");
    				scripts.append("\t\t\t"+formname+".setValue(\""+fieldname+"\",\""+prefix+"\"+(String)value_"+fieldname+"+\""+suffix+"\");\r\n");
    				scripts.append("\t\telse\r\n");
    				scripts.append("\t\t\t"+formname+".setValue(\""+fieldname+"\",value_"+fieldname+");\r\n");
    			}
    		}
    	}
    } 
    
    private void dealWithDataSourceAttribute(Tag tag,String formname,StringBuffer scripts){
    	String defaultValue=tag.getAttribute("sqlDataSource");
    	defaultValue=HtmlUtil.HTMLDecode(defaultValue);
    	
    	String fieldname=tag.getAttribute("fieldname");
        if(defaultValue!=null){
        	Pattern pattern=Pattern.compile("\\#[^#]+\\#");
        	StringBuffer sql=new StringBuffer();
        	Vector paras=new Vector();        	
        	Matcher matcher=pattern.matcher(defaultValue);
        	while(matcher.find()){
        		String para=matcher.group().substring(1,matcher.group().length()-1);
        		paras.add(para+this.fieldname_suffix);
        		matcher.appendReplacement(sql,"?");
        	}
        	matcher.appendTail(sql);
        	
        	fieldname=fieldname+this.fieldname_suffix;
        	
        	String dataloader=tag.getAttribute("dataloader");
        	if(dataloader==null||dataloader.length()==0)
        		dataloader="InitOptions";
       	
        	if(paras.size()==0){
        		scripts.append("\toptionsAction."+dataloader+"(\""+sql+"\",\""+fieldname+"values\",\""+fieldname+"labels\",request,null);\r\n");
        	}else{
        		scripts.append("\r\n\tString g_"+fieldname+"=\"\";\r\n");
        		for(int num=0;num<paras.size();num++){
        			String tmp=(String)paras.get(num);
        			scripts.append("\tif(true){String m_"+tmp+"=(String)"+formname+".getValue(\""+tmp+"\");\r\n");
        			//scripts.append("\tm_"+tmp+"=null;\r\n");
        			//scripts.append("\tif(m_"+tmp+"==null){\r\n");
        			scripts.append("\tjava.util.Vector vec=(java.util.Vector)session.getAttribute(\""+tmp+"values\");\r\n");
        			scripts.append("\tif(vec!=null&&vec.size()>0)\r\n"); 
        			scripts.append("\t{\r\n");
        			scripts.append("\t\tif(m_"+tmp+"==null||!vec.contains(m_"+tmp+"))\r\n");
        			scripts.append("\t\t\tm_"+tmp+"=(String)vec.elementAt(0);\r\n");
        			scripts.append("\t}\r\n");
        			//scripts.append("\t}\r\n");
        			scripts.append("\tif(m_"+tmp+"==null) m_"+tmp+"=\"00\";\r\n");
        			scripts.append("\tg_"+fieldname+"=g_"+fieldname+"+m_"+tmp+"+\",\";}\r\n");
        		}
        		scripts.append("\tg_"+fieldname+"=g_"+fieldname+".substring(0,g_"+fieldname+".length()-1);\r\n");
        		scripts.append("\toptionsAction."+dataloader+"(\""+sql+"\",\""+fieldname+"values\",\""+fieldname+"labels\",request,g_"+fieldname+".split(\",\"));\r\n");
        	}
        }
    }
    
    private void dealWithFormSetting(Tag tag,String formname,String propname,StringBuffer scripts){
    	String jiyi=tag.getAttribute("jiyi");
    	if(jiyi!=null&&"1".equals(jiyi))
    		scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormSetting(\""+this.htmlform.getFormId()+"\",\""+propname+this.fieldname_suffix+"\",\"jiyi\",\"1\");\r\n");
    	
    	String dizeng=tag.getAttribute("dizeng");
    	if(dizeng!=null&&"1".equals(dizeng))
    		scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormSetting(\""+this.htmlform.getFormId()+"\",\""+propname+this.fieldname_suffix+"\",\"dizeng\",\"1\");\r\n");
    	
    	String datatype=tag.getAttribute("fieldtype");
    	
    	String pname=propname;
    	String fid=this.htmlform.getFormId()+"";
    	if(Pattern.matches(".+\\_f\\d+\\z", pname)){
    		int idx=pname.lastIndexOf("_");
    		fid=pname.substring(idx+2);
    		pname=pname.substring(0, idx);
    	}
    	
    	if(datatype!=null&&datatype.equals("int")){
    		scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormDataTypeTranslator(\""+fid+"\",\""+pname+"\",com.hiromsoft.types.translator.LongTranslator.getInstance(),0);\r\n");
    	}else if(datatype!=null&&datatype.equals("numeric")){
    		scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormDataTypeTranslator(\""+fid+"\",\""+pname+"\",com.hiromsoft.types.translator.FloatTranslator.getInstance(),0);\r\n");
    	}else if(datatype!=null&&datatype.equals("date")){
    		scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormDataTypeTranslator(\""+fid+"\",\""+pname+"\",com.hiromsoft.types.translator.DateTranslator.getInstance(),0);\r\n");
    	}else if(datatype!=null&&datatype.equals("datetime")){
    		scripts.append("\tcom.hiromsoft.utils.RuntimeSetting.addFormDataTypeTranslator(\""+fid+"\",\""+pname+"\",com.hiromsoft.types.translator.DateTimeTranslator.getInstance(),0);\r\n");
    	}
    	
    }String getAdditionAttr(Tag tag,boolean readonly,boolean visible,boolean autosetclass){
    	String retval=" ";
    	String tmp="";
    	if(visible){
	        tmp=tag.getAttribute("style");
	        if(tmp!=null)
	            retval=retval+"style=\""+tmp+"\" ";
    	}else{
    		 retval=retval+"style=\"display:none\" ";
    	}
    	if(visible){	
	        tmp=tag.getAttribute("class");
	        if(tmp!=null&&tmp.length()!=0){
	        	if(readonly)
	        		retval=retval+"styleClass=\""+tmp+"-readonly\" ";
	        	else
	        		retval=retval+"styleClass=\""+tmp+"\" ";
	        }else if(autosetclass){
	        	if(readonly)
	        		retval=retval+"styleClass=\"form-"+tag.getTagName().toLowerCase()+"-readonly\" ";
	        	else
	        		retval=retval+"styleClass=\"form-"+tag.getTagName().toLowerCase()+"\" ";
	        }
    	}
        if(readonly){
        	retval=retval+"readonly=\"true\" ";
        }else{
	        Attribute attr=tag.getAttributeEx("readOnly");
	        if(attr!=null){
	        	retval=retval+"readonly=\"true\" ";
	        }
        }
        
        String[] atts="onclick,onmouseover,onmouseout,onchange,onblur,onfocus,onkeydown,onkeyup".split(",");
        for(int num=0;num<atts.length;num++){
	        tmp=tag.getAttribute(atts[num]);
	        if(tmp!=null)
	            retval=retval+""+atts[num]+"=\""+tmp+"\" ";
        }
        
        return retval;
    }
    String getAdditionAttr(Tag tag,boolean readonly){
    	return getAdditionAttr(tag,readonly,true,false);
    }
    
    String getAdditionAttr(Tag tag)
    {
        return getAdditionAttr(tag,false,true,false);
    }
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
    
    private boolean isMultibox(Tag tag,String formName){
    	boolean retval=false;
    	if(this.multiboxFieldList.contains(formName))
    		retval=true;
    	else {
    		String value=tag.getAttribute("multibox");
    		if(value!=null&&value.equals("1")){
    			this.multiboxFieldList.add(formName);
    			retval=true;
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
