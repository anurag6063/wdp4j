/*
 * $Header: /cvs/xzsp-mysql/src/org/apache/struts/taglib/html/BaseHandlerTag.java,v 1.2 2006/11/28 01:08:31 tenghl Exp $
 * $Revision: 1.2 $
 * $Date: 2006/11/28 01:08:31 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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
 */

package org.apache.struts.taglib.html;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.taglib.logic.IterateTag;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;

/**
 * Base class for tags that render form elements capable of including JavaScript
 * event handlers and/or CSS Style attributes. This class does not implement
 * the doStartTag() or doEndTag() methods. Subclasses should provide
 * appropriate implementations of these.
 *
 * @author Don Clasen
 * @author James Turner
 * @version $Revision: 1.2 $ $Date: 2006/11/28 01:08:31 $
 */

public abstract class BaseHandlerTag extends BodyTagSupport {

    /**
     * Commons Logging instance.
     */
    private static Log log = LogFactory.getLog(BaseHandlerTag.class);

    // ----------------------------------------------------- Instance Variables

    /**
     * The default Locale for our server.
     */
    protected static final Locale defaultLocale = Locale.getDefault();

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
        MessageResources.getMessageResources(Constants.Package + ".LocalStrings");

    //  Navigation Management

    /** Access key character. */
    protected String accesskey = null;

    /** Tab index value. */
    protected String tabindex = null;

    //  Indexing ability for Iterate

    /** Whether to created indexed names for fields
      * @since Struts 1.1
      */
    protected boolean indexed = false;

    //  Mouse Events

    /** Mouse click event. */
    private String onclick = null;

    /** Mouse double click event. */
    private String ondblclick = null;

    /** Mouse over component event. */
    private String onmouseover = null;

    /** Mouse exit component event. */
    private String onmouseout = null;

    /** Mouse moved over component event. */
    private String onmousemove = null;

    /** Mouse pressed on component event. */
    private String onmousedown = null;

    /** Mouse released on component event. */
    private String onmouseup = null;

    //  Keyboard Events

    /** Key down in component event. */
    private String onkeydown = null;

    /** Key released in component event. */
    private String onkeyup = null;

    /** Key down and up together in component event. */
    private String onkeypress = null;

    // Text Events

    /** Text selected in component event. */
    private String onselect = null;

    /** Content changed after component lost focus event. */
    private String onchange = null;

    // Focus Events and States

    /** Component lost focus event. */
    private String onblur = null;

    /** Component has received focus event. */
    private String onfocus = null;

    /** Component is disabled. */
    private boolean disabled = false;

    /** Component is readonly. */
    private boolean readonly = false;

    // CSS Style Support

    /** Style attribute associated with component. */
    private String style = null;

    /** Named Style class associated with component. */
    private String styleClass = null;

    /** Identifier associated with component.  */
    private String styleId = null;

    // Other Common Attributes

    /** The alternate text of this element. */
    private String alt = null;

    /** The message resources key of the alternate text. */
    private String altKey = null;

    /** The name of the message resources bundle for message lookups. */
    private String bundle = null;

    /** The name of the session attribute key for our locale. */
    private String locale = Globals.LOCALE_KEY;

    /** The advisory title of this element. */
    private String title = null;

    /** The message resources key of the advisory title. */
    private String titleKey = null;
    
    /**
     * the roles or users who can read(view) this property, delivered by comma;
     * the item is a pair of type=value, type indicate the value's type of (user,role)
     * for example: role=role1,role=role2,user=user1,user=user2
     * if the item is not a pair of type=value,only value,system will set this value to role as default
     * for example: rolea,roleb,user=user1 will equals to role=rolea,role=roleb,user=user1
     * if not set this property, everyone can read it
     */
    private String reader=null;
    
    /**
     * the roles or user who can modify this property,delivered by comma;
     * the item is a pair of type=value, type indicate the value's type of (user,role)
     * for example: role=role1,role=role2,user=user1,user=user2
     * if the item is not a pair of type=value,only value,system will set this value to role as default
     * for example: rolea,roleb,user=user1 will equals to role=rolea,role=roleb,user=user1
     * if not set this property, everyone can modify it;
     * 
     */
    private String author=null;
    
    
    
    // ------------------------------------------------------------- Properties

    //  Navigation Management
    
    public String getLockEnd()
    {
        //return "</td><td width=\"20px\"><img src=\""+ getContext()+"/icon/lock.gif\"></td></tr></table>";
        return "";
    }
    public String getLockBegin()
    {
        //return "<table width=\"100%\" height=\"100%\"><tr><td>";
        return "";
    }
    
    /** Sets the accessKey character. */
    public void setAccesskey(String accessKey) {
        this.accesskey = accessKey;
    }

    /** Returns the accessKey character. */
    public String getAccesskey() {
        return (this.accesskey);
    }

    /** Sets the tabIndex value. */
    public void setTabindex(String tabIndex) {
        this.tabindex = tabIndex;
    }

    /** Returns the tabIndex value. */
    public String getTabindex() {
        return (this.tabindex);
    }

    //  Indexing ability for Iterate [since Struts 1.1]

    /** Sets the indexed value.
      * @since Struts 1.1
      */
    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    /** Returns the indexed value.
      * @since Struts 1.1
      */
    public boolean getIndexed() {
        return (this.indexed);
    }

    // Mouse Events

    /** Sets the onClick event handler. */
    public void setOnclick(String onClick) {
        this.onclick = onClick;
    }

    /** Returns the onClick event handler. */
    public String getOnclick() {
        return onclick;
    }

    /** Sets the onDblClick event handler. */
    public void setOndblclick(String onDblClick) {
        this.ondblclick = onDblClick;
    }

    /** Returns the onDblClick event handler. */
    public String getOndblclick() {
        return ondblclick;
    }

    /** Sets the onMouseDown event handler. */
    public void setOnmousedown(String onMouseDown) {
        this.onmousedown = onMouseDown;
    }

    /** Returns the onMouseDown event handler. */
    public String getOnmousedown() {
        return onmousedown;
    }

    /** Sets the onMouseUp event handler. */
    public void setOnmouseup(String onMouseUp) {
        this.onmouseup = onMouseUp;
    }

    /** Returns the onMouseUp event handler. */
    public String getOnmouseup() {
        return onmouseup;
    }

    /** Sets the onMouseMove event handler. */
    public void setOnmousemove(String onMouseMove) {
        this.onmousemove = onMouseMove;
    }

    /** Returns the onMouseMove event handler. */
    public String getOnmousemove() {
        return onmousemove;
    }

    /** Sets the onMouseOver event handler. */
    public void setOnmouseover(String onMouseOver) {
        this.onmouseover = onMouseOver;
    }

    /** Returns the onMouseOver event handler. */
    public String getOnmouseover() {
        return onmouseover;
    }

    /** Sets the onMouseOut event handler. */
    public void setOnmouseout(String onMouseOut) {
        this.onmouseout = onMouseOut;
    }

    /** Returns the onMouseOut event handler. */
    public String getOnmouseout() {
        return onmouseout;
    }

    // Keyboard Events

    /** Sets the onKeyDown event handler. */
    public void setOnkeydown(String onKeyDown) {
        this.onkeydown = onKeyDown;
    }

    /** Returns the onKeyDown event handler. */
    public String getOnkeydown() {
        return onkeydown;
    }

    /** Sets the onKeyUp event handler. */
    public void setOnkeyup(String onKeyUp) {
        this.onkeyup = onKeyUp;
    }

    /** Returns the onKeyUp event handler. */
    public String getOnkeyup() {
        return onkeyup;
    }

    /** Sets the onKeyPress event handler. */
    public void setOnkeypress(String onKeyPress) {
        this.onkeypress = onKeyPress;
    }

    /** Returns the onKeyPress event handler. */
    public String getOnkeypress() {
        return onkeypress;
    }

    // Text Events

    /** Sets the onChange event handler. */
    public void setOnchange(String onChange) {
        this.onchange = onChange;
    }

    /** Returns the onChange event handler. */
    public String getOnchange() {
        return onchange;
    }

    /** Sets the onSelect event handler. */
    public void setOnselect(String onSelect) {
        this.onselect = onSelect;
    }

    /** Returns the onSelect event handler. */
    public String getOnselect() {
        return onselect;
    }

    // Focus Events and States

    /** Sets the onBlur event handler. */
    public void setOnblur(String onBlur) {
        this.onblur = onBlur;
    }

    /** Returns the onBlur event handler. */
    public String getOnblur() {
        return onblur;
    }

    /** Sets the onFocus event handler. */
    public void setOnfocus(String onFocus) {
        this.onfocus = onFocus;
    }

    /** Returns the onFocus event handler. */
    public String getOnfocus() {
        return onfocus;
    }

    /** Sets the disabled event handler. */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /** Returns the disabled event handler. */
    public boolean getDisabled() {
        return disabled;
    }

    /** Sets the readonly event handler. */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /** Returns the readonly event handler. */
    public boolean getReadonly() {
        return readonly;
    }

    // CSS Style Support

    /** Sets the style attribute. */
    public void setStyle(String style) {
        this.style = style;
    }

    /** Returns the style attribute. */
    public String getStyle() {
        return style;
    }

    /** Sets the style class attribute. */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /** Returns the style class attribute. */
    public String getStyleClass() {
        return styleClass;
    }

    /** Sets the style id attribute.  */
    public void setStyleId(String styleId) {
        this.styleId = styleId;
    }

    /** Returns the style id attribute.  */
    public String getStyleId() {
        return styleId;
    }

    // Other Common Elements

    /** Returns the alternate text attribute. */
    public String getAlt() {
        return alt;
    }

    /** Sets the alternate text attribute. */
    public void setAlt(String alt) {
        this.alt = alt;
    }

    /** Returns the message resources key of the alternate text. */
    public String getAltKey() {
        return altKey;
    }

    /** Sets the message resources key of the alternate text. */
    public void setAltKey(String altKey) {
        this.altKey = altKey;
    }

    /** Returns the name of the message resources bundle to use. */
    public String getBundle() {
        return bundle;
    }

    /** Sets the name of the message resources bundle to use. */
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    /** Returns the name of the session attribute for our locale. */
    public String getLocale() {
        return locale;
    }

    /** Sets the name of the session attribute for our locale. */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /** Returns the advisory title attribute. */
    public String getTitle() {
        return title;
    }

    /** Sets the advisory title attribute. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Returns the message resources key of the advisory title. */
    public String getTitleKey() {
        return titleKey;
    }

    /** Sets the message resources key of the advisory title. */
    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }
    /**
     * @return Returns the author.
     */
    public String getAuthor() {
        return author;
    }
    /**
     * @param author The author to set.
     */
    public void setAuthor(String author) {
        this.author = author;
    }
    /**
     * @return Returns the reader.
     */
    public String getReader() {
        return reader;
    }
    /**
     * @param reader The reader to set.
     */
    public void setReader(String reader) {
        this.reader = reader;
    }
    // --------------------------------------------------------- Public Methods

    /**
     * Release any acquired resources.
     */
    public void release() {

        super.release();
        accesskey = null;
        alt = null;
        altKey = null;
        bundle = null;
        indexed = false;
        locale = Globals.LOCALE_KEY;
        onclick = null;
        ondblclick = null;
        onmouseover = null;
        onmouseout = null;
        onmousemove = null;
        onmousedown = null;
        onmouseup = null;
        onkeydown = null;
        onkeyup = null;
        onkeypress = null;
        onselect = null;
        onchange = null;
        onblur = null;
        onfocus = null;
        disabled = false;
        readonly = false;
        style = null;
        styleClass = null;
        styleId = null;
        tabindex = null;
        title = null;
        titleKey = null;
        reader=null;
        author=null;

    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Return the text specified by the literal value or the message resources
     * key, if any; otherwise return <code>null</code>.
     *
     * @param literal Literal text value or <code>null</code>
     * @param key Message resources key or <code>null</code>
     *
     * @exception JspException if both arguments are non-null
     */
    protected String message(String literal, String key) throws JspException {

        if (literal != null) {
            if (key != null) {
                JspException e = new JspException(messages.getMessage("common.both"));
                RequestUtils.saveException(pageContext, e);
                throw e;
            } else {
                return (literal);
            }
        } else {
            if (key != null) {
                return (RequestUtils.message(pageContext, getBundle(), getLocale(), key));
            } else {
                return null;
            }
        }

    }

    private Class loopTagSupportClass = null;
    private Method loopTagSupportGetStatus = null;
    private Class loopTagStatusClass = null;
    private Method loopTagStatusGetIndex = null;
    private boolean triedJstlInit = false;
    private boolean triedJstlSuccess = false;

    private Integer getJstlLoopIndex () {
	if (!triedJstlInit) {
	    triedJstlInit = true;
	    try {
		loopTagSupportClass = 
		    RequestUtils.applicationClass("javax.servlet.jsp.jstl.core.LoopTagSupport");
		loopTagSupportGetStatus = 
		    loopTagSupportClass.getDeclaredMethod("getLoopStatus", null);
		loopTagStatusClass =
		    RequestUtils.applicationClass("javax.servlet.jsp.jstl.core.LoopTagStatus");
		loopTagStatusGetIndex = 
		    loopTagStatusClass.getDeclaredMethod("getIndex", null);
		triedJstlSuccess = true;
	    }
	    // These just mean that JSTL isn't loaded, so ignore
	    catch (ClassNotFoundException ex) {}
	    catch (NoSuchMethodException ex) {}
	}
	if (triedJstlSuccess) {
	    try {
		Object loopTag = findAncestorWithClass(this, loopTagSupportClass);
		if (loopTag == null)  {
		    return null;
		}
		Object status = loopTagSupportGetStatus.invoke(loopTag, null);
		return (Integer) loopTagStatusGetIndex.invoke(status, null);
	    } 
	    catch (IllegalAccessException ex) {
		log.error(ex.getMessage(), ex);
	    }
	    catch (IllegalArgumentException ex) {
		log.error(ex.getMessage(), ex);
	    }
	    catch (InvocationTargetException ex) {
		log.error(ex.getMessage(), ex);
	    }
	    catch (NullPointerException ex) {
		log.error(ex.getMessage(), ex);
	    }
	    catch (ExceptionInInitializerError ex) {
		log.error(ex.getMessage(), ex);
	    }
	}
	return null;
    }

    /**
     *  Appends bean name with index in brackets for tags with
     *  'true' value in 'indexed' attribute.
     *  @param handlers The StringBuffer that output will be appended to.
     *  @exception JspException if 'indexed' tag used outside of iterate tag.
     */
    protected void prepareIndex(StringBuffer handlers, String name) throws JspException {
	int index = 0;
	boolean found = false;

        // look for outer iterate tag
        IterateTag iterateTag = (IterateTag) findAncestorWithClass(this, IterateTag.class);
	// Look for JSTL loops
	if (iterateTag == null) {
	    Integer i = getJstlLoopIndex();
	    if (i != null) {
		index = i.intValue();
		found = true;
	    }
	} else {
	    index = iterateTag.getIndex();
	    found = true;
	}
        if (!found) {
            // this tag should only be nested in iteratetag, if it's not, throw exception
            JspException e = new JspException(messages.getMessage("indexed.noEnclosingIterate"));
            RequestUtils.saveException(pageContext, e);
            throw e;
        }
        if (name != null)
            handlers.append(name);
        handlers.append("[");
        handlers.append(index);
        handlers.append("]");
        if (name != null)
            handlers.append(".");
    }

    /**
     * Prepares the style attributes for inclusion in the component's HTML tag.
     * @return The prepared String for inclusion in the HTML tag.
     * @exception JspException if invalid attributes are specified
     */
    protected String prepareStyles() throws JspException {
        String value = null;
        StringBuffer styles = new StringBuffer();
        if (style != null) {
            styles.append(" style=\"");
            styles.append(getStyle());
            styles.append("\"");
        }
        if (styleClass != null) {
            styles.append(" class=\"");
            styles.append(getStyleClass());
            styles.append("\"");
        }
        if (styleId != null) {
            styles.append(" id=\"");
            styles.append(getStyleId());
            styles.append("\"");
        }
        value = message(title, titleKey);
        if (value != null) {
            styles.append(" title=\"");
            styles.append(value);
            styles.append("\"");
        }
        value = message(alt, altKey);
        if (value != null) {
            styles.append(" alt=\"");
            styles.append(value);
            styles.append("\"");
        }
        return styles.toString();
    }

    /**
     * Prepares the event handlers for inclusion in the component's HTML tag.
     * @return The prepared String for inclusion in the HTML tag.
     */
    protected String prepareEventHandlers() {
        StringBuffer handlers = new StringBuffer();
        prepareMouseEvents(handlers);
        prepareKeyEvents(handlers);
        prepareTextEvents(handlers);
        prepareFocusEvents(handlers);
        return handlers.toString();
    }

    /**
     * Prepares the mouse event handlers, appending them to the the given
     * StringBuffer.
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareMouseEvents(StringBuffer handlers) {
        if (onclick != null) {
            handlers.append(" onclick=\"");
            handlers.append(getOnclick());
            handlers.append("\"");
        }

        if (ondblclick != null) {
            handlers.append(" ondblclick=\"");
            handlers.append(getOndblclick());
            handlers.append("\"");
        }

        if (onmouseover != null) {
            handlers.append(" onmouseover=\"");
            handlers.append(getOnmouseover());
            handlers.append("\"");
        }

        if (onmouseout != null) {
            handlers.append(" onmouseout=\"");
            handlers.append(getOnmouseout());
            handlers.append("\"");
        }

        if (onmousemove != null) {
            handlers.append(" onmousemove=\"");
            handlers.append(getOnmousemove());
            handlers.append("\"");
        }

        if (onmousedown != null) {
            handlers.append(" onmousedown=\"");
            handlers.append(getOnmousedown());
            handlers.append("\"");
        }

        if (onmouseup != null) {
            handlers.append(" onmouseup=\"");
            handlers.append(getOnmouseup());
            handlers.append("\"");
        }
    }

    /**
     * Prepares the keyboard event handlers, appending them to the the given
     * StringBuffer.
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareKeyEvents(StringBuffer handlers) {

        if (onkeydown != null) {
            handlers.append(" onkeydown=\"");
            handlers.append(getOnkeydown());
            handlers.append("\"");
        }

        if (onkeyup != null) {
            handlers.append(" onkeyup=\"");
            handlers.append(getOnkeyup());
            handlers.append("\"");
        }

        if (onkeypress != null) {
            handlers.append(" onkeypress=\"");
            handlers.append(getOnkeypress());
            handlers.append("\"");
        }
    }

    /**
     * Prepares the text event handlers, appending them to the the given
     * StringBuffer.
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareTextEvents(StringBuffer handlers) {

        if (onselect != null) {
            handlers.append(" onselect=\"");
            handlers.append(getOnselect());
            handlers.append("\"");
        }

        if (onchange != null) {
            handlers.append(" onchange=\"");
            handlers.append(getOnchange());
            handlers.append("\"");
        }
    }

    /**
     * Prepares the focus event handlers, appending them to the the given
     * StringBuffer.
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareFocusEvents(StringBuffer handlers) {

        if (onblur != null) {
            handlers.append(" onblur=\"");
            handlers.append(getOnblur());
            handlers.append("\"");
        }

        if (onfocus != null) {
            handlers.append(" onfocus=\"");
            handlers.append(getOnfocus());
            handlers.append("\"");
        }

        if (disabled) {
            handlers.append(" disabled=\"disabled\"");
        }

        if (readonly) {
            handlers.append(" readonly=\"readonly\"");
        }

    }

    /**
     * Allows HTML tags to find out if they're nested within an %lt;html:html&gt; tag that
     * has xhtml set to true.
     * @return true if the tag is nested within an html tag with xhtml set to true, false
     * otherwise.
     * @since Struts 1.1
     */
    protected boolean isXhtml() {
        String xhtml =
            (String) this.pageContext.getAttribute(Globals.XHTML_KEY, PageContext.PAGE_SCOPE);
        
        if ("true".equalsIgnoreCase(xhtml)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the closing brace for an input element depending on xhtml status.  The tag
     * must be nested within an %lt;html:html&gt; tag that has xhtml set to true.
     * @return String - &gt; if xhtml is false, /&gt; if xhtml is true
     * @since Struts 1.1
     */
    protected String getElementClose() {
        if (this.isXhtml()) {
            return " />";
        } else {
            return ">";
        }
    }

    /**
     * Searches all scopes for the bean and calls BeanUtils.getProperty() with the 
     * given arguments and converts any exceptions into JspException.
     * 
     * @param beanName The name of the object to get the property from.
     * @param property The name of the property to get.
     * @return The value of the property.
     * @throws JspException
     * @since Struts 1.1
     */
    protected String lookupProperty(String beanName, String property)
        throws JspException {
            
        Object bean = RequestUtils.lookup(this.pageContext, beanName, null);
        if (bean == null) {
            throw new JspException(messages.getMessage("getter.bean", beanName));
        }

        try {
            return BeanUtils.getProperty(bean, property);

        } catch (IllegalAccessException e) {
            throw new JspException(
                messages.getMessage("getter.access", property, beanName));

        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            throw new JspException(
                messages.getMessage("getter.result", property, t.toString()));

        } catch (NoSuchMethodException e) {
            throw new JspException(
                messages.getMessage("getter.method", property, beanName));
        }
    }
    /**
     * get the HttpSession from the JSP pageCOntext;
     * @return
     */
    private HttpSession getSession(){
        HttpSession retval=null;
        PageContext pagecontext= this.pageContext;
        ServletRequest requ=pagecontext.getRequest();
        if(requ instanceof HttpServletRequest)
        {
            HttpServletRequest request=(HttpServletRequest)requ;
            retval=request.getSession();
            request=null;
        }
        pagecontext=null;
        requ=null;
        return retval;
    }
    /**
     * get the context path of this application
     * @return
     */
    protected String getContext()
    {
        String retval="";
        PageContext pagecontext= this.pageContext;
        ServletRequest requ=pagecontext.getRequest();
        if(requ instanceof HttpServletRequest)
        {
            HttpServletRequest request=(HttpServletRequest)requ;
            retval=request.getContextPath();
            request=null;
        }
        pagecontext=null;
        requ=null;
        return retval;
 
    }
    /**
     * get the user name from current session
     * @return user name
     */	
    protected String getCurrentUserName()
    {
        String retval=null;
        HttpSession session=getSession();
        if(session!=null){
            Object username=session.getAttribute("username");
            if(username!=null)
                retval=(String)username;
        }
        session=null;
        return retval;
    }
    /**
     * Get the role name from current session;
     * @return role name
     */
    protected String getCurrentRoleName(){
        String retval=null;
        HttpSession session=getSession();
        if(session!=null){
            Object jsname=session.getAttribute("jsname");
            if(jsname!=null)
                retval=(String)jsname;
        }
        session=null;
        return retval;
    }
    /**
     * Indicate weather the current user can read this property
     * @return
     */
    protected boolean isPropertyReader() throws Exception
    {
        boolean retval=true;
        String lock=null;
        try{
            lock=this.getFormLock();
        }catch(Exception e){
            throw e;
        }
        if(lock.equals("none"))
        {
            retval=true;
        }else if(lock.equals("all"))
        {
            retval=true;
        }else if(lock.equals("field")){
            
	        String username=getCurrentUserName();
	        String rolename=getCurrentRoleName();
	        if(username==null&&rolename==null)
	        {
	            //the user not login yet
	            retval=false;
	        }else
	        {
	            if(this.reader==null)
	                retval=true;  // not set reader, default to everyone
	            else if(this.reader.trim().length()==0)
	                retval=true;  //user set empty property of reader, equals not set
	            else
	            {
	                String[] roles=null;
	                String[] users=null;
	                parseACL(reader,roles,users);
	                if(IsInRoles(rolename,roles))
	                {
	                    retval=true;
	                }else if(IsInUsers(username,users))
	                    retval=true;
	                else{
	                    try{
	                        retval=isPropertyAuthor();
	                    }catch(Exception e){
	                        throw e;
	                    }
	                }
	                
	            }
	        }
        }
        return retval;
    }
    /**
     * Indicate weather the current user can modify this property
     * @return
     */
    protected boolean isPropertyAuthor() throws Exception
    {
        boolean retval=true;
        String lock=null;
        try{
            lock=this.getFormLock();
        }catch(Exception e){
            throw e;
        }
        if(lock.equals("none"))
        {
            retval=true;
        }else if(lock.equals("all"))
        {
            retval=false;
        }else if(lock.equals("field")){
	        String username=getCurrentUserName();
	        String rolename=getCurrentRoleName();
	        if(username==null&&rolename==null)
	        {
	            // the user not login yet
	            retval=false;
	        }else
	        {
	            if(this.author==null)
	                retval=true;  // not set reader, default to everyone
	            else if(this.author.trim().length()==0)
	                retval=true;  //user set empty property of reader, equals not set
	            else
	            {
	                String[] roles=null;
	                String[] users=null;
	                parseACL(this.author,roles,users);
	                if(IsInRoles(rolename,roles))
	                {
	                    retval=true;
	                }else if(IsInUsers(username,users))
	                    retval=true;
	                else
	                    retval=false;
	                
	            }
	        }
        }
        return retval;
    }
    /**
     * Indicate weather the given user is in the users
     * @param user
     * @param users
     * @return
     */
    private boolean IsInUsers(String user,String[] users){
        boolean retval=false;
        if(users!=null&&user!=null){
            for(int num=0;num<users.length;num++)
            {
                if(users[num].equals(user))
                {
                    retval=true;
                    break;
                }
            }
        }
        return retval;
    }
    /**
     * Indicate weather the given role is in the roles
     * @param role
     * @param roles
     * @return
     */
    private boolean IsInRoles(String role,String[] roles){
        boolean retval=false;
        if(role!=null&&roles!=null){
            for(int num=0;num<roles.length;num++)
            {
                if(roles[num].equals(role))
                {
                    retval=true;
                    break;
                }
            }
        }
        return retval;
    }
    /**
     * Parse the Access Control List (ACL) into String arrays
     * the ACL include reader and author property vlaue 
     * @param acl
     * @param roles returned roles
     * @param users returned users
     */
    private void parseACL(String acl,String[] roles,String[] users)
    {
        String[] pairs=acl.split(",");
        StringBuffer strroles=new StringBuffer("");
        StringBuffer strusers=new StringBuffer("");
        for(int num=0;num<pairs.length;num++){
            String pair=pairs[num].trim();
            if(pair.length()!=0){
                String[] pairmap=pair.split("=");
                if(pairmap.length>=2){
                    String name=pairmap[0].trim();
                    if(name.toLowerCase().equals("role"))
                        strroles.append(pairmap[1]).append(",");
                    else if (name.toLowerCase().equals("user"))
                        strusers.append(pairmap[1]).append(",");
                    name=null;
                }else{
                   strroles.append(pairmap[0]).append(",");
                }
                pairmap=null;
                    
            }
            pair=null;
        }
        strroles.append("everyone");
        strusers.append("everyone");
        
        roles=strroles.toString().split(",");
        users=strusers.toString().split(",");
        
        strroles=null;
        strusers=null;
        pairs=null;
        
    }
    
    protected String getFormLock() throws Exception
    {
        String retval="none";
        Tag tag=TagSupport.findAncestorWithClass(this,new FormTag().getClass());
        if(tag!=null){
            if(tag instanceof FormTag)
            {
                FormTag form=(FormTag)tag;
                try{
                    retval=form.getLockType();
                }catch(Exception e)
                {
                    throw e;
                }
                form=null;
            }
            tag=null;
        }
        return retval;
    }
    
}
