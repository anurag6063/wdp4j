package com.hiromsoft.types;

import java.text.SimpleDateFormat;

public class Date extends java.util.Date {


	private static final long serialVersionUID = -6684052975782638193L;

	public Date(long date) {
        super(date);
    }
    public Date(java.util.Date date){
        super(date.getTime());
    }
    
    public String toString(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(this);
    }
    public static void main(String[] args) {
    }
}
