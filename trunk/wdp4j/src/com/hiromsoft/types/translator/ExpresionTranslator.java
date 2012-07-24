package com.hiromsoft.types.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpresionTranslator extends BaseTranslator{

	public static ExpresionTranslator instance=null;
	
	public static String regex1="([^\\+\\-\\*\\/]+)([\\*\\/]([^\\+\\-\\*\\/]+))+";
	public static String regex2="([\\*\\/])([^\\+\\-\\*\\/]+)";
	public static String regex3="([^\\+\\-\\*\\/]+)([\\+\\-]([^\\+\\-\\*\\/]+))+";
	public static String regex4="([\\+\\-])([^\\+\\-\\*\\/]+)";
	public static String regex5="\\([^\\(\\)]+\\)";
	
	private HashMap memory=new HashMap();
	private int num=0;
	private String expresion=null;
	private HashMap fields=null;

	public static BaseTranslator getInstance(){
		return instance;
	}
	public ExpresionTranslator(){
		
	}
	public ExpresionTranslator(String expresion){
		this.expresion=expresion;
	}
	
	public String getExpresion() {
		return expresion;
	}
	public void setExpresion(String expresion) {
		this.expresion = expresion;
	}
	public Object Translate(Map datas,String propName){
		Object value = datas.get(propName);
		if(value==null) return null;
		String strvalue = value.toString();
		if(strvalue.indexOf("${")==-1) return value;
		if(strvalue.length()==0) {
			datas.put(propName, null);
			return null;
		}
		Pattern pttn=Pattern.compile(regex5);
		Pattern pttn1=Pattern.compile(regex1);
		Pattern pttn1_1=Pattern.compile(regex2);
		Pattern pttn3=Pattern.compile(regex3);
		Pattern pttn3_1=Pattern.compile(regex4);
		
		StringBuffer varName=null;
		try{
			varName=this.exp(pttn, pttn1, pttn1_1, pttn3, pttn3_1, strvalue.substring(2, strvalue.length()-1));
			if(varName!=null&&varName.length()!=0){
				Double value2=(Double)memory.get(varName);
				datas.put(propName, value2);
				return value2;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return value;
	}
	
	private StringBuffer exp(Pattern pttn,Pattern pttn1,Pattern pttn1_1,Pattern pttn3,Pattern pttn3_1,String exp)throws Exception{
		StringBuffer replacedSQL=new StringBuffer();
		Matcher matcher=pttn.matcher(exp);
		//if(!matcher.matches())
		//	return exp1(exp);
		boolean found=false;
        String varName="";
        while(matcher.find()){
            varName=matcher.group();
            String tmp=varName.substring(1, varName.length()-1);
            varName= exp1(pttn1,pttn1_1,pttn3,pttn3_1,tmp).toString();
            matcher.appendReplacement(replacedSQL,varName);
            found=true;
        }
        matcher.appendTail(replacedSQL);
        if(!found)
        	return exp1(pttn1,pttn1_1,pttn3,pttn3_1,replacedSQL.toString());
        else
        	replacedSQL=exp(pttn,pttn1,pttn1_1,pttn3,pttn3_1,replacedSQL.toString());
       	return replacedSQL;
 
	}
	private StringBuffer exp1(Pattern pttn1,Pattern pttn1_1,Pattern pttn3,Pattern pttn3_1,String exp)throws Exception{
		StringBuffer retval=new StringBuffer();
		String exp1=exp;
		calculate(pttn1,pttn1_1,retval,exp1);
		System.out.println(retval);
		
		exp1=retval.toString();
		retval.delete(0, retval.length());
		calculate(pttn3,pttn3_1,retval,exp1);
		System.out.println(retval);
		
		return retval;
	}
	public void calculate(Pattern pattern1,Pattern pattern2, StringBuffer retval,String exp)throws Exception{
		Matcher matcher=pattern1.matcher(exp);
        String varName="";
        while(matcher.find()){
            varName=matcher.group(0);
            String a=matcher.group(1);
            String tmp=varName.substring(a.length(),varName.length());
            Matcher matcher2=pattern2.matcher(tmp);
            double __ic=0;
            double __ia=getVarValue(a);
            double __ib=0;
            while(matcher2.find()){
            	String calculator=matcher2.group(1);
            	__ib=getVarValue(matcher2.group(2));
            	if(calculator.trim().equals("*"))
            		__ic=__ia*__ib;
            	else if(calculator.trim().equals("/"))
            		__ic=__ia/__ib;
            	else if(calculator.trim().equals("+"))
            		__ic=__ia+__ib;
            	else if(calculator.trim().equals("-"))
            		__ic=__ia-__ib;
            	__ia=__ic;
            }
            matcher2=null;
            String d=varName+"="+__ic;
            varName="__var"+num;
            memory.put(varName, new Double(__ic));
            num++;
            System.out.println(varName+"="+d);
            matcher.appendReplacement(retval,varName);
        }
        matcher.appendTail(retval);
	}
	
	
	public double getVarValue(String a) throws Exception{
		double c=0;
		if(a.startsWith("__var")){
			c=((Double)memory.get(a)).doubleValue();
			return c;
		}
		try{
			c=Double.parseDouble(a.trim());
			return c;
		}catch(Exception ex){
			Object tmp=fields.get(a);
			if(tmp==null) return 0;
			String strvalue=tmp.toString();
			if(strvalue.length()==0) return 0;
			try{
				c=Double.parseDouble(strvalue.trim());
			}catch(Exception ex2){
				throw new Exception("字段"+a+"的值不是有效数字");
			}
		}
		return c;
	}
	
}
