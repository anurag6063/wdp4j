package com.hiromsoft.testcase;

import com.hiromsoft.utils.RequestUtil;

public class UrlEncodingTest {
	
	public static void main(String[] args){
		
		System.out.println("A${code}".indexOf("${"));
		
		System.out.println((int)'A');
		System.out.println((int)'a');
		
		try{
		System.out.println(java.net.URLEncoder.encode("Œ“","utf-8"));
		}catch(Exception ex){}
		
		String aaa="\\/";
		String bbn="/";
		System.out.println("a/b/c".replaceAll(aaa, bbn));
		
		System.out.println(RequestUtil.getRealPath("a/b/c"));
	}

}
