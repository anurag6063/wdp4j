package com.hiromsoft.web.filter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;


import com.hiromsoft.utils.DatabaseUtil;
import com.hiromsoft.utils.Global;
import com.hiromsoft.web.action.LogonAction;
import com.hiromsoft.web.modal.User;

/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AuthFilter implements Filter {

    private String allowedurl=null;
    private String ignoredurl=null;
    
    /**
     *  
     */
    public AuthFilter() {
        super();
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub
        allowedurl=arg0.getInitParameter("allowed");
        ignoredurl=arg0.getInitParameter("ignored");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {


        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        User user=(User)session.getAttribute(Global.HIROM_USER);
        String url = req.getRequestURL().toString().toLowerCase();

        if (user == null) {
            if (isAllowed(url, req.getContextPath().toLowerCase()) ||isIgnored(url))
                chain.doFilter(request, response);
            else {
            	if(restoreSessions((HttpServletRequest)request,(HttpServletResponse)response)){
            		chain.doFilter(request, response);
            	}else{
            		//不能恢复session,则重新登录
	                request.getRequestDispatcher("/index.jsp").forward(request,
	                        response);
            	}
            }
        } else{
            if(isHome(url,req.getContextPath().toLowerCase()))
                ((HttpServletResponse)response).sendRedirect(((HttpServletResponse)response).encodeRedirectURL(Global.HOME_URL.substring(1)));
            else
                chain.doFilter(request, response);
        }
            

    }
    protected boolean restoreSessions(HttpServletRequest request,HttpServletResponse response){
    	boolean retval=false;
    	Cookie[] cookies=request.getCookies();
    	if(cookies==null) return retval;
    	String userId=null;
    	for(int num=0;num<cookies.length;num++){
    		if(cookies[num].getName().equals("USERID")){
    			userId=(String)cookies[num].getValue();
    			break;
    		}
    	}
    	if(userId==null||userId.length()==0) return retval;

    	PreparedStatement pst=null;
    	Session dbsession=null;
    	try{
    		dbsession=DatabaseUtil.getHibernateSession();
   			new LogonAction().setSessions(request,response,dbsession,userId);
			retval=true;
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(pst!=null)
			{
				try{pst.close();}catch(Exception ex){}
			}
			
			if(dbsession!=null)
			{
				try{dbsession.close();}catch(Exception ex){}
			}
		}
    	
    	
    	
    	return retval;
    }

    protected boolean isIgnored(String url) {
        String regex=".+\\.js|.+\\.gif|.+\\.jpg|.+\\.png|.+\\.css|.+\\.htc|.+\\.cab|.+\\.xml|.+\\.xsl|.+\\.doc|.+\\.xslx|.+\\.docx|.+\\.dot";
        if(this.ignoredurl!=null&&this.ignoredurl.length()!=0)
            regex=regex +"|" + this.ignoredurl;
        return Pattern.matches(regex,url); 
    }

    protected boolean isAllowed(String url, String context) {
        boolean retval = false;
        if (url.indexOf("license") != -1
                || url.indexOf(context + "/index") != -1
                || url.indexOf(context + "/logon") != -1
                || url.indexOf(context + "/logoff") != -1
                || url.indexOf("userrandomcode") != -1){
            retval = true;
        }else{
            if(this.allowedurl!=null&&this.allowedurl.length()!=0){
                String[] urls=this.allowedurl.split("\\|");
                for(int num=0;num<urls.length;num++){
                    if(url.indexOf(urls[num])!=-1){
                        retval=true;
                        break;
                    }
                }
            }
        }
        return retval;
    }
    protected boolean isHome(String url,String context){
        boolean retval=false;
        if(url.endsWith(context)||url.endsWith(context+"/")||url.endsWith(context+"/index.jsp"))
            retval=true;
        return retval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
        this.allowedurl=null;
        this.ignoredurl=null;
    }

}