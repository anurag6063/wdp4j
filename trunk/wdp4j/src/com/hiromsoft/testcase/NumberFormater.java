package com.hiromsoft.testcase;

import java.text.DecimalFormat;

public class NumberFormater {
	
	public static void  main(String[] args){
		 DecimalFormat df=new DecimalFormat("0.####");
		 double aaa=12028382.982273;
		 System.out.println(df.format(aaa));
		 
	}

}
