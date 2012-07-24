package com.hiromsoft.testcase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sql4j.parser.SQL;

import com.hiromsoft.hiromform.Utils;

public class SqlParserTestCase {

	public static void main(String[] args){
		/*
		System.out.println(Pattern.matches(".+\\_f\\d+\\z", "a_f18"));
		
		StringBuffer replacedSQL=new StringBuffer();
		Pattern pttn=Pattern.compile("\\_f[0-9]+\\z");
        Matcher matcher=pttn.matcher("aaa{aab}_f1_f134340");
        String varName="";
        while(matcher.find()){
            varName=matcher.group();
            System.out.println(varName);
            matcher.appendReplacement(replacedSQL,"22");
        }
        matcher.appendTail(replacedSQL);
        System.out.println(replacedSQL);
		return;*/
		
		String sql2=Utils.escapeMySQLVars("select a,b from table1 where a>${__khbh}").toString();
		System.out.println(sql2);
		sql2=Utils.unescapeMySQLVars(sql2).toString();
		System.out.println(sql2);
		
		SQL sql=new SQL(sql2);
		if(sql.getSelectStatement().getWhereClause()!=null)
			System.out.println(sql.getSelectStatement().getWhereClause().toString());
	}
	
}
