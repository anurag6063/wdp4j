package com.hiromsoft.hiromform;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HiromTokenProcessor {

    private static HiromTokenProcessor instance = new HiromTokenProcessor();

    public static HiromTokenProcessor getInstance() {
        return instance;
    }
    protected HiromTokenProcessor() {

    }
    public synchronized boolean isTokenValid(HttpServletRequest request,HttpServletResponse response) {
        return this.isTokenValid(request,response, false);
    }
    public synchronized boolean isTokenValid(
        HttpServletRequest request,HttpServletResponse response,
        boolean reset) {

    	Cookie[] cookies=request.getCookies();
    	if(cookies==null) return false;
    	String saved=null;
    	for(int num=0;num<cookies.length;num++){
    		if(cookies[num].getName().equals("TOKEN")){
    			saved=(String)cookies[num].getValue();
    			break;
    		}
    	}
    	if(saved==null||saved.length()==0) return false;

    	UserDataForm udf=(UserDataForm)request.getAttribute("userDataForm");
        if(udf==null) return false;
        
        String token = udf.getStringValue("__hirom_token");
        if (token == null) {
            return false;
        }
        if (reset) {
            this.saveToken(request,response);
        }
        return saved.equals(token);
    }
    public synchronized void saveToken(HttpServletRequest request,HttpServletResponse response) {

        
        String token = generateToken(request);
        Cookie cookie=new Cookie("TOKEN",token);
		cookie.setPath(request.getContextPath());
		response.addCookie(cookie);
		
		UserDataForm udf=(UserDataForm)request.getAttribute("userDataForm");
        if(udf==null) return;
        
        udf.setValue("__hirom_token",token);
   

    }
    public String generateToken(HttpServletRequest request) {

        HttpSession session = request.getSession();
        try {
            byte id[] = session.getId().getBytes();
            byte now[] = new Long(System.currentTimeMillis()).toString().getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(id);
            md.update(now);
            return this.toHex(md.digest());

        } catch (IllegalStateException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

    }

    public String toHex(byte buffer[]) {
        StringBuffer sb = new StringBuffer();
        String s = null;
        for (int i = 0; i < buffer.length; i++) {
            s = Integer.toHexString((int) buffer[i] & 0xff);
            if (s.length() < 2) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

}

