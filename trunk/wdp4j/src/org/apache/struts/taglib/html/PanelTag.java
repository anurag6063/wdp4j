/*
 * Created on 2005-7-28
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.struts.taglib.html;

import javax.servlet.jsp.JspException;

import org.apache.struts.util.ResponseUtils;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PanelTag extends BaseHandlerTag {
    
    
    public int doStartTag() throws JspException {
        
        boolean isauthor=false;
        try{
            isauthor=this.isPropertyAuthor();
        }catch(Exception e){
            throw new JspException(e);
        }
        
        StringBuffer results = new StringBuffer();
        if(isauthor)
            results.append("<span ");
        else
            results.append("<span style=\"display:none\" ");
        
        results.append(prepareEventHandlers());
        results.append(prepareStyles());
        results.append(">");
        
        ResponseUtils.write(pageContext, results.toString());

        return (EVAL_BODY_TAG);

    }
    
    public int doEndTag() throws JspException {

         // Generate an HTML element
        StringBuffer results = new StringBuffer();
        results.append("</span>");
        ResponseUtils.write(pageContext, results.toString());

        // Evaluate the remainder of this page
        return (EVAL_PAGE);

    }
    
    public void release() {

        super.release();
        
    }

}
