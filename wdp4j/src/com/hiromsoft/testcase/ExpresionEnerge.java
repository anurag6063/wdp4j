package com.hiromsoft.testcase;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpresionEnerge {
	public static int num=0;
	public static Pattern pttn1=Pattern.compile("([^\\+\\-\\*\\/]+)([\\*\\/]([^\\+\\-\\*\\/]+))+");
	public static Pattern pttn1_1=Pattern.compile("([\\*\\/])([^\\+\\-\\*\\/]+)");
	
	public static Pattern pttn3=Pattern.compile("([^\\+\\-\\*\\/]+)([\\+\\-]([^\\+\\-\\*\\/]+))+");
	public static Pattern pttn3_1=Pattern.compile("([\\+\\-])([^\\+\\-\\*\\/]+)");
	
	public static HashMap memory=new HashMap();
	
	public static void main(String[] args){
		Pattern pttn=Pattern.compile("\\([^\\(\\)]+\\)");
		System.out.println(Long.MAX_VALUE);
		System.out.println(Float.parseFloat("99992932382838.2838238"));
		System.out.println(Float.MAX_VALUE);
		//System.out.println(Pattern.matches("[^\\+\\-\\*\\/]*\\*[^\\+\\-\\*\\/]*","b*c"));
		String varName=exp(pttn,"1+5*3*6*((10-100)*(2+10)-109/3*10)-127").toString();
        System.out.println(memory.get(varName).toString());
	}
	public static StringBuffer exp(Pattern pttn,String exp){
		StringBuffer replacedSQL=new StringBuffer();
		Matcher matcher=pttn.matcher(exp);
		//if(!matcher.matches())
		//	return exp1(exp);
		boolean found=false;
        String varName="";
        while(matcher.find()){
            varName=matcher.group();
            String tmp=varName.substring(1, varName.length()-1);
            varName= exp1(tmp).toString();
            matcher.appendReplacement(replacedSQL,varName);
            found=true;
        }
        matcher.appendTail(replacedSQL);
        if(!found)
        	return exp1(replacedSQL.toString());
        else
        	replacedSQL=exp(pttn,replacedSQL.toString());
       	return replacedSQL;
 
	}
	public static StringBuffer exp1(String exp){
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
	public static void calculate(Pattern pattern1,Pattern pattern2, StringBuffer retval,String exp){
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
	
	
	public static double getVarValue(String a){
		double c=0;
		if(a.startsWith("__var")){
			c=((Double)memory.get(a)).doubleValue();
			return c;
		}
		try{
			c=Double.parseDouble(a.trim());
			return c;
		}catch(Exception ex){ex.printStackTrace();}
		return c;
	}

}
