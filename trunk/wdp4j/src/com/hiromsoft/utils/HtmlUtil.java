package com.hiromsoft.utils;


public class HtmlUtil {
	
	public static String safelyGetAlignment(String[] array,int index){
		if(array==null) return " align=\"left\" ";
		if(index>array.length-1) return " align=\"left\" ";
		if(array[index]==null) return " align=\"left\" ";
		String tmp=array[index];
		if(tmp.equals("1"))
			return " align=\"center\" ";
		else if(tmp.equals("2"))
			return " align=\"right\" ";
		return " align=\"left\" ";
	}
	
	public static String safelyGetArrayValue(String[] array,int index){
		if(array==null) return "";
		if(index>array.length-1) return "";
		if(array[index]==null) return "";
		return array[index];
	}
	public static String convertNull(Object val){
		if(val==null)
			return "";
		else{
			return val.toString();
		}
	}
	public static String HTMLEncode(String txt) {
		if(txt==null) return null;
		txt=txt.replaceAll("&","&amp;");
		txt=txt.replaceAll("<","&lt;");
		txt=txt.replaceAll(">","&gt;");
		txt=txt.replaceAll("\"","&quot;");
		txt=txt.replaceAll("'","&#39;");
		return txt;
	}
	public static String HTMLDecode(String txt) {
		if(txt==null) return null;
		txt=txt.replaceAll("&amp;","&");
		txt=txt.replaceAll("&lt;","<");
		txt=txt.replaceAll("&gt;",">");
		txt=txt.replaceAll("&quot;","\"");
		txt=txt.replaceAll("&#39;","'");
		return txt;
	}
	public static String JsStringEncode(String txt) {
		StringBuffer retval=new StringBuffer();
		for(int num=0;num<txt.length();num++){
			char tmp=txt.charAt(num);
			if(tmp=='\''){
				retval.append("\\\'");
			}else if(tmp=='"'){
				retval.append("\\\"");
			}else
				retval.append(tmp);
		}
		return retval.toString();
	}
	
	

}
