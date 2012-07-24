package com.hiromsoft.types;

import java.text.SimpleDateFormat;

public class DateTime extends java.util.Date {


	private static final long serialVersionUID = 1L;

    public DateTime(long date) {
        super(date);
    }
    public DateTime(java.util.Date date){
        super(date.getTime());
    }
    public String toString(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(this);
    }
    public static void main(String[] args) {
    }
}
