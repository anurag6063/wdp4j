package com.hiromsoft.utils;


public class SecurityChecker
{
    static{
	    try{
	        System.loadLibrary("HironsoftLicenseService");
	        
	    }catch(Exception e ){
	        e.printStackTrace();
	    }
	}
	private native boolean CheckRegInfo(String userRegName,String SerialNumber);
	private native String  GetUserUniqueCode(String userRegName);
	
	public SecurityChecker(){
	    
	}
	
	public boolean validateRegister(String username,String serialnumber){
		return CheckRegInfo(username,serialnumber);
	}
	public String createUniqueCode(String username){
		return GetUserUniqueCode(username);
	}

	public static void main(String[] args)
	{
		SecurityChecker secu=new SecurityChecker();
		String username="teng hailong";
		System.out.println(secu.createUniqueCode(username));
		boolean ok=secu.validateRegister(username,"a654c689b1490f71747215ca52ed6a29");
		System.out.println(ok);

	}

	
};