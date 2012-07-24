/*
 * $Id: IncludeTag.java,v 1.3 2006/12/11 02:05:25 tenghl Exp $
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.struts.taglib.bean;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.util.ResponseUtils;

/**
 * Define the contents of a specified intra-application request as a page scope
 * attribute of type <code>java.lang.String</code>. If the current request is
 * part of a session, the session identifier will be included in the generated
 * request, so it will be part of the same session.
 * <p>
 * <strong>FIXME </strong>: In a servlet 2.3 environment, we can use a wrapped
 * response passed to RequestDispatcher.include().
 * 
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2006/12/11 02:05:25 $
 */

public class IncludeTag extends TagSupport {

    // ------------------------------------------------------------- Properties

    /**
     * Buffer size to use when reading the input stream.
     */
    protected static final int BUFFER_SIZE = 256;

    /**
     * The anchor to be added to the end of the generated hyperlink.
     */
    protected String anchor = null;

    public String getAnchor() {
        return (this.anchor);
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * The name of the global <code>ActionForward</code> that contains a path
     * to our requested resource.
     */
    protected String forward = null;

    public String getForward() {
        return (this.forward);
    }

    public void setForward(String forward) {
        this.forward = forward;
    }

    /**
     * The absolute URL to the resource to be included.
     */
    protected String href = null;

    public String getHref() {
        return (this.href);
    }

    public void setHref(String href) {
        this.href = href;
    }

    /**
     * The name of the scripting variable that will be exposed as a page scope
     * attribute.
     */
    protected String id = null;

    public String getId() {
        return (this.id);
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages = MessageResources
            .getMessageResources("org.apache.struts.taglib.bean.LocalStrings");

    /**
     * Deprecated method to set the "name" attribute, which has been replaced by
     * the "page" attribute.
     * 
     * @deprecated use setPage(String) instead
     */
    public void setName(String name) {
        this.page = name;
    }

    /**
     * The context-relative URI of the page or servlet to be included.
     */
    protected String page = null;

    public String getPage() {
        return (this.page);
    }

    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Include transaction token (if any) in the hyperlink?
     */
    protected boolean transaction = false;

    public boolean getTransaction() {
        return (this.transaction);
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    protected String forwardBean = null;

    protected String forwardProperty = null;

    protected String pageBean = null;

    protected String pageProperty = null;
    
    protected String hrefBean=null;
    protected String hrefProperty=null;

    protected boolean output = false;

    /**
     * @return Returns the forwardBean.
     */
    public String getForwardBean() {
        return forwardBean;
    }

    /**
     * @param forwardBean
     *            The forwardBean to set.
     */
    public void setForwardBean(String forwardBean) {
        this.forwardBean = forwardBean;
    }

    /**
     * @return Returns the forwardProperty.
     */
    public String getForwardProperty() {
        return forwardProperty;
    }

    /**
     * @param forwardProperty
     *            The forwardProperty to set.
     */
    public void setForwardProperty(String forwardProperty) {
        this.forwardProperty = forwardProperty;
    }
    

    /**
     * @return Returns the hrefBean.
     */
    public String getHrefBean() {
        return hrefBean;
    }
    /**
     * @param hrefBean The hrefBean to set.
     */
    public void setHrefBean(String hrefBean) {
        this.hrefBean = hrefBean;
    }
    /**
     * @return Returns the hrefProperty.
     */
    public String getHrefProperty() {
        return hrefProperty;
    }
    /**
     * @param hrefProperty The hrefProperty to set.
     */
    public void setHrefProperty(String hrefProperty) {
        this.hrefProperty = hrefProperty;
    }
    /**
     * @return Returns the output.
     */
    public String getOutput() {
        if(output)
            return "true";
        else
            return "false";
    }

    /**
     * @param output
     *            The output to set.
     */
    public void setOutput(String output) {
        if(output!=null&&output.equals("true"))
            this.output =true;
    }

    /**
     * @return Returns the pageBean.
     */
    public String getPageBean() {
        return pageBean;
    }

    /**
     * @param pageBean
     *            The pageBean to set.
     */
    public void setPageBean(String pageBean) {
        this.pageBean = pageBean;
    }

    /**
     * @return Returns the pageProperty.
     */
    public String getPageProperty() {
        return pageProperty;
    }

    /**
     * @param pageProperty
     *            The pageProperty to set.
     */
    public void setPageProperty(String pageProperty) {
        this.pageProperty = pageProperty;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Define the contents returned for the specified resource as a page scope
     * attribute.
     * 
     * @exception JspException
     *                if a JSP error occurs
     */
    public int doStartTag() throws JspException {

        // Set up a URLConnection to read the requested resource
        Map params = RequestUtils.computeParameters(pageContext, null, null,
                null, null, null, null, null, transaction);
        // FIXME - <html:link> attributes
        String urlString = null;
        URL url = null;
        String page1=page;
        String forward1=forward;
        String href1=href;
        try {
            if (forwardProperty != null && forward1 == null
                    && forwardBean != null) {
                try {
                    Object bean = RequestUtils.lookup(this.pageContext,
                            forwardBean, null);
                    if (bean == null) {
                        throw new JspException(messages.getMessage(
                                "getter.bean", forwardBean));
                    }

                    try {
                        forward1 = BeanUtils.getProperty(bean, forwardProperty);
                    } catch (Exception e) {
                        throw new JspException(messages.getMessage(
                                "getter.access", forwardProperty, forwardBean));
                    }
                } catch (Exception ex) {
                    throw new JspException("uncatched exception");
                }
            }
            if (page1 == null && pageBean != null && pageProperty != null) {
                try {
                    Object bean = RequestUtils.lookup(this.pageContext,
                            pageBean, null);
                    if (bean == null) {
                        throw new JspException(messages.getMessage(
                                "getter.bean", pageBean));
                    }

                    try {
                        page1 = BeanUtils.getProperty(bean, pageProperty);
                    } catch (Exception e) {
                        throw new JspException(messages.getMessage(
                                "getter.access", pageProperty, pageBean));
                    }
                } catch (Exception ex) {
                    throw new JspException("uncatched exception");
                }

            }
            if (href1 == null && hrefBean != null && hrefProperty != null) {
                try {
                    Object bean = RequestUtils.lookup(this.pageContext,
                            hrefBean, null);
                    if (bean == null) {
                        throw new JspException(messages.getMessage(
                                "getter.bean", hrefBean));
                    }

                    try {
                        href1 = BeanUtils.getProperty(bean, hrefProperty);
                    } catch (Exception e) {
                        throw new JspException(messages.getMessage(
                                "getter.access", hrefProperty, hrefBean));
                    }
                } catch (Exception ex) {
                    throw new JspException("uncatched exception");
                }

            }
            urlString = RequestUtils.computeURL(pageContext, forward1, href1,
                    page1, params, anchor, false);
            
            if (urlString.indexOf(':') < 0) {
            	HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
                url = new URL(RequestUtils.requestURL(request), urlString);
            } else {
                url = new URL(urlString);
            }
            
        } catch (MalformedURLException e) {
            RequestUtils.saveException(pageContext, e);
            throw new JspException(messages.getMessage("include.url", e
                    .toString()));
        }

        URLConnection conn = null;
        try {
            // Set up the basic connection
            conn = url.openConnection();
            conn.setAllowUserInteraction(false);
            conn.setDoInput(true);
            conn.setDoOutput(false);
            // Add a session id cookie if appropriate
            HttpServletRequest request = (HttpServletRequest) pageContext
            .getRequest();
            if ((conn instanceof HttpURLConnection)
                    && (urlString.startsWith(request.getContextPath())||urlString.indexOf("localhost")!=-1)
                    && (request.getRequestedSessionId() != null)
                    && (request.isRequestedSessionIdFromCookie())) {
                String sb = "JSESSIONID=";
                String sessionid = "";
                Cookie[] cookie = request.getCookies();
                for(int i = 0; i < cookie.length; i++)
                {
                 if(cookie[i].getName().trim().equalsIgnoreCase("JSESSIONID"))
                 {
                  sessionid = cookie[i].getValue().trim();
                  break;
                  //logger.debug("cookie:"+cookie[i].getName() + "=" +cookie[i].getValue());
                 }
                 
                }
                sb += sessionid;
                conn.setRequestProperty("Cookie", sb);
            }
            // Connect to the requested resource
            conn.connect();
        } catch (Exception e) {
            RequestUtils.saveException(pageContext, e);
            throw new JspException(messages.getMessage("include.open", url
                    .toString(), e.toString()));
        }

        // Copy the contents of this URL
        StringBuffer sb = new StringBuffer();
        try {
            BufferedInputStream is = new BufferedInputStream(conn
                    .getInputStream());
            InputStreamReader in = new InputStreamReader(is,"GBK"); // FIXME -
                                                              // encoding
            char buffer[] = new char[BUFFER_SIZE];
            //byte[]
            int n = 0;
            while (true) {
                n = in.read(buffer);
                if (n < 1)
                    break;
                sb.append(buffer, 0, n);
            }
            in.close();
        } catch (Exception e) {
            RequestUtils.saveException(pageContext, e);
            throw new JspException(messages.getMessage("include.read", url
                    .toString(), e.toString()));
        }

        //      Define the retrieved content as a page scope attribute
        if (output)
            ResponseUtils.write(pageContext, sb.toString());
        else
            pageContext.setAttribute(id, sb.toString());

        // Skip any body of this tag
        return (SKIP_BODY);
    }

    /**
     * Release all allocated resources.
     */
    public void release() {
        super.release();
        anchor = null;
        forward = null;
        href = null;
        id = null;
        page = null;
        transaction = false;
        output=false;
        pageBean=null;
        pageProperty=null;
        forwardBean=null;
        forwardProperty=null;
        hrefBean=null;
        hrefProperty=null;
    }

}