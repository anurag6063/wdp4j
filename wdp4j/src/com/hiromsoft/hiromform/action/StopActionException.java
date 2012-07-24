package com.hiromsoft.hiromform.action;

public class StopActionException extends Exception {

	private static final long serialVersionUID = 1L;
	private String msg=null;
	
	public StopActionException(String msg){
		super(msg);
		this.msg=msg;
	}
	public String getMessage(){
		return msg;
	}
}
